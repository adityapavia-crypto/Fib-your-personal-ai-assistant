package com.fib.jarvis

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat

object Router {
    private const val NEW = Intent.FLAG_ACTIVITY_NEW_TASK
    private val I = RegexOption.IGNORE_CASE

    private val SMS = Regex(
        "^(?:send (?:a )?(?:text message|text|message|sms) to|text|sms|message|env[ií]a (?:un )?(?:mensaje|sms) a|envoie (?:un )?(?:message|sms) [aà]|manda (?:un )?(?:messaggio|sms) a|invia (?:un )?(?:messaggio|sms) a)\\s+(.+?)\\s+(?:saying|that says|say|diciendo|que diga|disant|en disant|dicendo|che dice)\\s+(.+)$", I)
    private val CALL = Regex("^(?:call|phone|ring|llama a|llama|appelle|appeler|chiama)\\s+(.+)$", I)

    fun handle(c: Context, raw: String): String? {
        val said = raw.trim().trimEnd('.', '!', '?')
        val t = FibState.norm(raw)

        SMS.find(said)?.let { return sms(c, it.groupValues[1].trim(), it.groupValues[2].trim()) }
        CALL.find(said)?.let { return call(c, it.groupValues[1].trim()) }

        if (Regex("flashlight|torch|linterna|lampe de poche|lampe torche|torcia|手电筒|टॉर्च|مصباح").containsMatchIn(t)) {
            val off = Regex("\\boff\\b|apaga|éteins|spegni|关|बंद|اطفئ").containsMatchIn(t)
            return try {
                val m = c.getSystemService(CameraManager::class.java)
                m.setTorchMode(m.cameraIdList[0], !off); "OK"
            } catch (e: Exception) { null }
        }
        if (Regex("alarm|timer|alarma|réveil|minuteur|sveglia|闹钟|अलार्म|منبه").containsMatchIn(t)) {
            launch(c, Intent(AlarmClock.ACTION_SHOW_ALARMS)); return "OK"
        }
        Regex("^(?:navigate to|directions to|navega a|navigue vers|naviga verso|naviga a)\\s+(.+)$").find(t)?.let {
            launch(c, Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + Uri.encode(it.groupValues[1])))); return "OK"
        }
        Regex("^(?:open|abre|ouvre|apri)\\s+(.+)$").find(t)?.let { return openApp(c, it.groupValues[1]) }
        Regex("^(?:google|search for|busca|cherche|cerca)\\s+(.+)$").find(t)?.let {
            launch(c, Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(it.groupValues[1])))); return "OK"
        }
        return null
    }

    private fun lookup(c: Context, name: String): Pair<String, String>? {
        val digits = name.filter { it.isDigit() || it == '+' }
        if (digits.length >= 5 && name.count { it.isLetter() } == 0) return digits to digits
        if (c.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return null
        var res: Pair<String, String>? = null
        c.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?", arrayOf("%$name%"),
            "${ContactsContract.CommonDataKinds.Phone.IS_PRIMARY} DESC"
        )?.use { if (it.moveToFirst()) res = it.getString(0) to it.getString(1).replace(" ", "").replace("-", "") }
        return res
    }

    private fun call(c: Context, name: String): String {
        val (shown, number) = lookup(c, name) ?: return c.getString(R.string.no_contact, name)
        if (c.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                c.getSystemService(TelecomManager::class.java).placeCall(Uri.fromParts("tel", number, null), Bundle())
                return c.getString(R.string.calling, shown)
            } catch (e: Exception) { }
        }
        launch(c, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
        return c.getString(R.string.calling, shown)
    }

    private fun sms(c: Context, name: String, body: String): String {
        val (shown, number) = lookup(c, name) ?: return c.getString(R.string.no_contact, name)
        if (c.checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val sm = if (Build.VERSION.SDK_INT >= 31) c.getSystemService(SmsManager::class.java) else @Suppress("DEPRECATION") SmsManager.getDefault()
                sm.sendMultipartTextMessage(number, null, sm.divideMessage(body), null, null)
                return c.getString(R.string.sms_sent, shown)
            } catch (e: Exception) { }
        }
        launch(c, Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number")).putExtra("sms_body", body))
        return c.getString(R.string.sms_opened, shown)
    }

    private fun openApp(c: Context, label: String): String {
        val pm = c.packageManager
        val apps = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        val hit = apps.firstOrNull { it.loadLabel(pm).toString().lowercase().contains(label) } ?: return c.getString(R.string.nothing)
        val i = pm.getLaunchIntentForPackage(hit.activityInfo.packageName) ?: return c.getString(R.string.nothing)
        launch(c, i); return "OK"
    }

    private fun launch(c: Context, i: Intent) {
        i.addFlags(NEW)
        try { c.startActivity(i) } catch (_: Exception) {}
        val nm = c.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("fib_action", "Fib actions", NotificationManager.IMPORTANCE_HIGH))
        val pi = PendingIntent.getActivity(c, System.currentTimeMillis().toInt(), i, PendingIntent.FLAG_IMMUTABLE)
        nm.notify(3, NotificationCompat.Builder(c, "fib_action").setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Fib").setContentText(c.getString(R.string.tap_to_open))
            .setContentIntent(pi).setAutoCancel(true).setTimeoutAfter(30_000).build())
    }
}

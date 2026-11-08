package com.fib.jarvis

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.os.*
import android.speech.*
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.util.Locale

class ListenService : Service() {
    private var sr: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var awakeUntil = 0L
    private val h = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val wake = Regex("\\bhey,? ?(fib|fip|fibb|phib)\\b")

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        if (FibState.isKilled(this)) { stopSelf(); return START_NOT_STICKY }
        FibState.setStoppedBySystem(this, false)
        val ch = "fib"
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel(ch, "Fib", NotificationManager.IMPORTANCE_LOW))
        val kill = PendingIntent.getBroadcast(this, 0, Intent(this, KillReceiver::class.java).setPackage(packageName), PendingIntent.FLAG_IMMUTABLE)
        val open = PendingIntent.getActivity(this, 1, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(this, ch).setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Fib").setContentText(getString(R.string.listening)).setOngoing(true)
            .setContentIntent(open).addAction(0, getString(R.string.kill), kill).build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        else startForeground(1, n)
        if (tts == null) tts = TextToSpeech(this) { tts?.language = Locale.getDefault() }
        listen()
        return START_STICKY
    }

    private fun listen() {
        if (FibState.isKilled(this)) return
        sr?.destroy()
        sr = SpeechRecognizer.createSpeechRecognizer(this).also { r ->
            r.setRecognitionListener(object : RecognitionListener {
                override fun onResults(b: Bundle?) {
                    b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { process(it) }
                    h.postDelayed({ listen() }, 250)
                }
                override fun onError(e: Int) { h.postDelayed({ listen() }, 1000) }
                override fun onReadyForSpeech(p: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(v: Float) {}
                override fun onBufferReceived(b: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(b: Bundle?) {}
                override fun onEvent(t: Int, b: Bundle?) {}
            })
            r.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag()))
        }
    }

    private fun process(text: String) {
        val said = text.trim()
        val t = FibState.norm(said)
        if (FibState.isKillPhrase(t)) { FibState.setKilled(this, true); return }
        val m = wake.find(t)
        if (m != null) {
            val cmd = Regex("(?i)^.*?hey,?\\s*(?:fib|fip|fibb|phib)\\b[\\s,.:;!-]*").replace(said, "").trim()
            if (cmd.isEmpty()) { awakeUntil = System.currentTimeMillis() + 8000; say("…") } else run(cmd)
        } else if (System.currentTimeMillis() < awakeUntil) {
            awakeUntil = 0; run(said)
        }
    }

    private fun run(cmd: String) = scope.launch { say(Brain.reply(this@ListenService, cmd)) }

    private fun say(s: String) { tts?.speak(s.take(600), TextToSpeech.QUEUE_FLUSH, null, "fib") }

    override fun onDestroy() {
        sr?.destroy(); tts?.shutdown(); scope.cancel()
        if (!FibState.isKilled(this)) {
            FibState.setStoppedBySystem(this, true)
            val open = PendingIntent.getActivity(this, 2, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
            getSystemService(NotificationManager::class.java).notify(2,
                NotificationCompat.Builder(this, "fib").setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentText(getString(R.string.wl_stopped)).setContentIntent(open).setAutoCancel(true).build())
        }
        super.onDestroy()
    }
}

class KillReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) = FibState.setKilled(c, true)
}

package com.fib.jarvis

import android.content.*
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject

data class Msg(val user: Boolean, val text: String)

object FibState {
    private fun p(c: Context) = c.getSharedPreferences("fib", Context.MODE_PRIVATE)

    fun isKilled(c: Context) = p(c).getBoolean("killed", false)
    fun isPremium(c: Context) = p(c).getBoolean("premium", false)
    fun setPremium(c: Context, v: Boolean) = p(c).edit().putBoolean("premium", v).apply()
    fun wasStoppedBySystem(c: Context) = p(c).getBoolean("stopped", false)
    fun setStoppedBySystem(c: Context, v: Boolean) = p(c).edit().putBoolean("stopped", v).apply()

    fun setKilled(c: Context, v: Boolean) {
        p(c).edit().putBoolean("killed", v).apply()
        if (v) c.stopService(Intent(c, ListenService::class.java))
        else startListening(c)
    }

    fun startListening(c: Context) {
        if (isKilled(c)) return
        if (c.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        ContextCompat.startForegroundService(c, Intent(c, ListenService::class.java))
    }

    fun norm(s: String) = s.lowercase().replace(Regex("[^\\p{L}\\p{N} ]"), " ").replace(Regex("\\s+"), " ").trim()
    fun isKillPhrase(s: String) = norm(s).contains("kill fib") || norm(s).contains("kill fip")

    fun remember(c: Context, m: Msg) {
        val arr = JSONArray(p(c).getString("mem", "[]"))
        arr.put(JSONObject().put("u", m.user).put("t", m.text))
        val keep = 500
        val out = JSONArray()
        for (i in maxOf(0, arr.length() - keep) until arr.length()) out.put(arr.get(i))
        p(c).edit().putString("mem", out.toString()).apply()
    }
    fun history(c: Context, limit: Int = if (isPremium(c)) 500 else 10): List<Msg> {
        val arr = JSONArray(p(c).getString("mem", "[]"))
        return (maxOf(0, arr.length() - limit) until arr.length()).map {
            val o = arr.getJSONObject(it); Msg(o.getBoolean("u"), o.getString("t"))
        }
    }
    fun clearMemory(c: Context) = p(c).edit().remove("mem").apply()
}

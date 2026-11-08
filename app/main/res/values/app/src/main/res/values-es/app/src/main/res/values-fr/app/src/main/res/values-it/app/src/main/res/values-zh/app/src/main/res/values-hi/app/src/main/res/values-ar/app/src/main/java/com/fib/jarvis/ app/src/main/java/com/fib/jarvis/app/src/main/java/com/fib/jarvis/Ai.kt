package com.fib.jarvis

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

object Ai {
    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8").replace("+", "%20")

    private fun http(url: String, body: String? = null): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 8000; c.readTimeout = 15000
        c.setRequestProperty("User-Agent", "FibAssistant/1.0 (Android)")
        if (body != null) {
            c.requestMethod = "POST"; c.doOutput = true
            c.setRequestProperty("Content-Type", "application/json")
            c.outputStream.use { it.write(body.toByteArray()) }
        }
        return (if (c.responseCode < 400) c.inputStream else c.errorStream).bufferedReader().use { it.readText() }
    }

    suspend fun answer(ctx: Context, q: String): String = withContext(Dispatchers.IO) {
        val lang = Locale.getDefault().language.ifEmpty { "en" }
        try {
            if (FibState.isPremium(ctx) && BuildConfig.GEMINI_API_KEY.isNotEmpty()) gemini(ctx, q, lang)
            else wikipedia(ctx, q, lang)
        } catch (e: Exception) { ctx.getString(R.string.nothing) }
    }

    private fun wikipedia(ctx: Context, q: String, lang: String): String {
        val base = "https://$lang.wikipedia.org"
        val s = JSONArray(http("$base/w/api.php?action=opensearch&limit=1&format=json&search=${enc(q)}"))
        val title = s.getJSONArray(1).optString(0)
        if (title.isEmpty()) return ctx.getString(R.string.nothing)
        val r = JSONObject(http("$base/api/rest_v1/page/summary/${enc(title)}"))
        return r.optString("extract", ctx.getString(R.string.nothing))
    }

    private fun gemini(ctx: Context, q: String, lang: String): String {
        val contents = JSONArray()
        FibState.history(ctx).forEach {
            contents.put(JSONObject().put("role", if (it.user) "user" else "model")
                .put("parts", JSONArray().put(JSONObject().put("text", it.text))))
        }
        contents.put(JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", q))))
        val body = JSONObject().put("contents", contents).put("systemInstruction",
            JSONObject().put("parts", JSONArray().put(JSONObject().put("text",
                "You are Fib, a fast, helpful JARVIS-style voice assistant. Be concise. Reply in the language with code '$lang'."))))
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}"
        return JSONObject(http(url, body.toString())).getJSONArray("candidates").getJSONObject(0)
            .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
    }
}

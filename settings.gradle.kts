# Fib for Android – complete project (copy‑paste ready)

Native Android (Kotlin + Jetpack Compose). Languages: English, 中文, हिन्दी, Español, Français, العربية, Italiano.
**Wake word:** "Hey Fib" · **Kill switch:** "kill Fib" · **Free:** Wikipedia · **Premium €10/month:** Gemini + longer memory + faster.

## How to build it (10 minutes)
1. Android Studio → *New Project → Empty Activity (Compose)* → name **Fib**, package **com.fib.jarvis**, min SDK 26.
2. Replace/create each file below at the path shown (paths relative to project root).
3. In `local.properties` add: `GEMINI_API_KEY=your_key_later`
4. Run on a real phone (speech recognition does not work well on emulators). **Build → Generate Signed Bundle** for the Play Store.

---

## `settings.gradle.kts`
```kotlin
pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositories { google(); mavenCentral() } }
rootProject.name = "Fib"
include(":app")
```

## `build.gradle.kts` (root)
```kotlin
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
}
```

## `app/build.gradle.kts`
```kotlin
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val props = Properties().apply { rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) } }

android {
    namespace = "com.fib.jarvis"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.fib.jarvis"
        minSdk = 26; targetSdk = 35
        versionCode = 1; versionName = "1.0"
        buildConfigField("String", "GEMINI_API_KEY", "\"${props.getProperty("GEMINI_API_KEY", "")}\"")
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildTypes { release { isMinifyEnabled = false } }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.android.billingclient:billing-ktx:7.1.1")
}
```

---

## `app/src/main/AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE"/>
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/>
    <uses-permission android:name="android.permission.READ_CONTACTS"/>
    <uses-permission android:name="android.permission.CALL_PHONE"/>
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.SET_ALARM"/>

    <queries>
        <intent><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.LAUNCHER"/></intent>
    </queries>

    <application
        android:label="@string/app_name"
        android:icon="@android:drawable/ic_btn_speak_now"
        android:supportsRtl="true"
        android:localeConfig="@xml/locales_config"
        android:theme="@style/Theme.AppCompat.DayNight.NoActionBar">

        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>

        <service android:name=".ListenService" android:exported="false"
            android:foregroundServiceType="microphone"/>
        <receiver android:name=".KillReceiver" android:exported="false"/>
    </application>
</manifest>
```

## `app/src/main/res/xml/locales_config.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<locale-config xmlns:android="http://schemas.android.com/apk/res/android">
    <locale android:name="en"/><locale android:name="zh"/><locale android:name="hi"/>
    <locale android:name="es"/><locale android:name="fr"/><locale android:name="ar"/><locale android:name="it"/>
</locale-config>
```

---

## Strings (7 languages). Every permission text ends with the "kill Fib" reminder via `%1$s`.

### `res/values/strings.xml` (English)
```xml
<resources>
    <string name="app_name">Fib</string>
    <string name="kill_hint">You can always say “kill Fib”.</string>
    <string name="perm_mic">Fib needs the microphone to hear “Hey Fib” and your commands. %1$s</string>
    <string name="perm_notif">Fib needs notifications to show that it is listening. %1$s</string>
    <string name="perm_contacts">Fib needs your contacts to call people by name. %1$s</string>
    <string name="perm_phone">Fib needs phone access to place calls. %1$s</string>
    <string name="perm_camera">Fib needs the camera for photos. %1$s</string>
    <string name="perm_location">Fib needs your location for navigation. %1$s</string>
    <string name="allow">Allow</string>
    <string name="not_now">Not now</string>
    <string name="wl_title">Please whitelist Fib</string>
    <string name="wl_msg">Your phone may block Fib from always listening. Please whitelist Fib (battery: Unrestricted, and allow auto-start) so “Hey Fib” always works. %1$s</string>
    <string name="wl_stopped">Your phone stopped Fib. Tap to whitelist it.</string>
    <string name="hint">Message Fib…</string>
    <string name="greeting">Hi, I’m Fib. Say “Hey Fib” or type below. Free: powered by Wikipedia.</string>
    <string name="premium">⭐ Premium €10/mo</string>
    <string name="premium_on">⭐ Premium</string>
    <string name="killed">Fib is disabled.</string>
    <string name="enable">Enable Fib</string>
    <string name="listening">Listening for “Hey Fib”</string>
    <string name="kill">Kill Fib</string>
    <string name="nothing">I couldn’t find anything for that.</string>
</resources>
```

### `res/values-es/strings.xml`
```xml
<resources>
    <string name="kill_hint">Siempre puedes decir “kill Fib”.</string>
    <string name="perm_mic">Fib necesita el micrófono para oír “Hey Fib” y tus órdenes. %1$s</string>
    <string name="perm_notif">Fib necesita notificaciones para mostrar que está escuchando. %1$s</string>
    <string name="perm_contacts">Fib necesita tus contactos para llamar por nombre. %1$s</string>
    <string name="perm_phone">Fib necesita acceso al teléfono para hacer llamadas. %1$s</string>
    <string name="perm_camera">Fib necesita la cámara para hacer fotos. %1$s</string>
    <string name="perm_location">Fib necesita tu ubicación para navegar. %1$s</string>
    <string name="allow">Permitir</string>
    <string name="not_now">Ahora no</string>
    <string name="wl_title">Añade Fib a la lista blanca</string>
    <string name="wl_msg">Tu teléfono puede impedir que Fib escuche siempre. Añade Fib a la lista blanca (batería: sin restricciones y permitir inicio automático) para que “Hey Fib” funcione siempre. %1$s</string>
    <string name="wl_stopped">Tu teléfono detuvo Fib. Toca para añadirlo a la lista blanca.</string>
    <string name="hint">Escribe a Fib…</string>
    <string name="greeting">Hola, soy Fib. Di “Hey Fib” o escribe abajo. Gratis: con Wikipedia.</string>
    <string name="premium">⭐ Premium 10 €/mes</string>
    <string name="premium_on">⭐ Premium</string>
    <string name="killed">Fib está desactivado.</string>
    <string name="enable">Activar Fib</string>
    <string name="listening">Escuchando “Hey Fib”</string>
    <string name="kill">Apagar Fib</string>
    <string name="nothing">No encontré nada.</string>
</resources>
```

### `res/values-fr/strings.xml`
```xml
<resources>
    <string name="kill_hint">Vous pouvez toujours dire « kill Fib ».</string>
    <string name="perm_mic">Fib a besoin du micro pour entendre « Hey Fib » et vos commandes. %1$s</string>
    <string name="perm_notif">Fib a besoin des notifications pour montrer qu’il écoute. %1$s</string>
    <string name="perm_contacts">Fib a besoin de vos contacts pour appeler par nom. %1$s</string>
    <string name="perm_phone">Fib a besoin d’accéder au téléphone pour appeler. %1$s</string>
    <string name="perm_camera">Fib a besoin de l’appareil photo. %1$s</string>
    <string name="perm_location">Fib a besoin de votre position pour la navigation. %1$s</string>
    <string name="allow">Autoriser</string>
    <string name="not_now">Pas maintenant</string>
    <string name="wl_title">Ajoutez Fib à la liste blanche</string>
    <string name="wl_msg">Votre téléphone peut empêcher Fib d’écouter en permanence. Ajoutez Fib à la liste blanche (batterie : sans restriction, et autoriser le démarrage auto) pour que « Hey Fib » fonctionne toujours. %1$s</string>
    <string name="wl_stopped">Votre téléphone a arrêté Fib. Touchez pour l’autoriser.</string>
    <string name="hint">Écrire à Fib…</string>
    <string name="greeting">Salut, je suis Fib. Dites « Hey Fib » ou écrivez ci-dessous. Gratuit : propulsé par Wikipédia.</string>
    <string name="premium">⭐ Premium 10 €/mois</string>
    <string name="premium_on">⭐ Premium</string>
    <string name="killed">Fib est désactivé.</string>
    <string name="enable">Activer Fib</string>
    <string name="listening">À l’écoute de « Hey Fib »</string>
    <string name="kill">Arrêter Fib</string>
    <string name="nothing">Je n’ai rien trouvé.</string>
</resources>
```

### `res/values-it/strings.xml`
```xml
<resources>
    <string name="kill_hint">Puoi sempre dire “kill Fib”.</string>
    <string name="perm_mic">Fib ha bisogno del microfono per sentire “Hey Fib” e i tuoi comandi. %1$s</string>
    <string name="perm_notif">Fib ha bisogno delle notifiche per mostrare che sta ascoltando. %1$s</string>
    <string name="perm_contacts">Fib ha bisogno dei contatti per chiamare per nome. %1$s</string>
    <string name="perm_phone">Fib ha bisogno del telefono per fare chiamate. %1$s</string>
    <string name="perm_camera">Fib ha bisogno della fotocamera. %1$s</string>
    <string name="perm_location">Fib ha bisogno della posizione per la navigazione. %1$s</string>
    <string name="allow">Consenti</string>
    <string name="not_now">Non ora</string>
    <string name="wl_title">Aggiungi Fib alla whitelist</string>
    <string name="wl_msg">Il telefono potrebbe impedire a Fib di ascoltare sempre. Aggiungi Fib alla whitelist (batteria: senza restrizioni e avvio automatico consentito) così “Hey Fib” funziona sempre. %1$s</string>
    <string name="wl_stopped">Il telefono ha fermato Fib. Tocca per aggiungerlo alla whitelist.</string>
    <string name="hint">Scrivi a Fib…</string>
    <string name="greeting">Ciao, sono Fib. Di’ “Hey Fib” o scrivi qui sotto. Gratis: con Wikipedia.</string>
    <string name="premium">⭐ Premium 10 €/mese</string>
    <string name="premium_on">⭐ Premium</string>
    <string name="killed">Fib è disattivato.</string>
    <string name="enable">Attiva Fib</string>
    <string name="listening">In ascolto di “Hey Fib”</string>
    <string name="kill">Spegni Fib</string>
    <string name="nothing">Non ho trovato nulla.</string>
</resources>
```

### `res/values-zh/strings.xml`
```xml
<resources>
    <string name="kill_hint">你随时可以说“kill Fib”。</string>
    <string name="perm_mic">Fib需要麦克风来听“Hey Fib”和你的指令。%1$s</string>
    <string name="perm_notif">Fib需要通知权限来显示正在聆听。%1$s</string>
    <string name="perm_contacts">Fib需要通讯录来按姓名打电话。%1$s</string>
    <string name="perm_phone">Fib需要电话权限来拨打电话。%1$s</string>
    <string name="perm_camera">Fib需要相机权限来拍照。%1$s</string>
    <string name="perm_location">Fib需要位置权限来导航。%1$s</string>
    <string name="allow">允许</string>
    <string name="not_now">暂不</string>
    <string name="wl_title">请将Fib加入白名单</string>
    <string name="wl_msg">你的手机可能会阻止Fib持续聆听。请将Fib加入白名单（电池：无限制，并允许自启动），这样“Hey Fib”才能一直有效。%1$s</string>
    <string name="wl_stopped">手机已停止Fib。点按以加入白名单。</string>
    <string name="hint">给Fib发消息…</string>
    <string name="greeting">你好，我是Fib。说“Hey Fib”或在下方输入。免费版由维基百科驱动。</string>
    <string name="premium">⭐ 高级版 €10/月</string>
    <string name="premium_on">⭐ 高级版</string>
    <string name="killed">Fib已停用。</string>
    <string name="enable">启用Fib</string>
    <string name="listening">正在聆听“Hey Fib”</string>
    <string name="kill">关闭Fib</string>
    <string name="nothing">没有找到相关内容。</string>
</resources>
```

### `res/values-hi/strings.xml`
```xml
<resources>
    <string name="kill_hint">आप हमेशा “kill Fib” कह सकते हैं।</string>
    <string name="perm_mic">“Hey Fib” और आपके आदेश सुनने के लिए Fib को माइक्रोफ़ोन चाहिए। %1$s</string>
    <string name="perm_notif">सुनते समय दिखाने के लिए Fib को नोटिफ़िकेशन चाहिए। %1$s</string>
    <string name="perm_contacts">नाम से कॉल करने के लिए Fib को संपर्क चाहिए। %1$s</string>
    <string name="perm_phone">कॉल करने के लिए Fib को फ़ोन की अनुमति चाहिए। %1$s</string>
    <string name="perm_camera">फ़ोटो के लिए Fib को कैमरा चाहिए। %1$s</string>
    <string name="perm_location">नेविगेशन के लिए Fib को लोकेशन चाहिए। %1$s</string>
    <string name="allow">अनुमति दें</string>
    <string name="not_now">अभी नहीं</string>
    <string name="wl_title">कृपया Fib को व्हाइटलिस्ट करें</string>
    <string name="wl_msg">आपका फ़ोन Fib को हमेशा सुनने से रोक सकता है। कृपया Fib को व्हाइटलिस्ट करें (बैटरी: अप्रतिबंधित, और ऑटो-स्टार्ट की अनुमति दें) ताकि “Hey Fib” हमेशा काम करे। %1$s</string>
    <string name="wl_stopped">आपके फ़ोन ने Fib को रोक दिया। व्हाइटलिस्ट करने के लिए टैप करें।</string>
    <string name="hint">Fib को संदेश भेजें…</string>
    <string name="greeting">नमस्ते, मैं Fib हूँ। “Hey Fib” कहें या नीचे लिखें। फ्री: विकिपीडिया से।</string>
    <string name="premium">⭐ प्रीमियम €10/माह</string>
    <string name="premium_on">⭐ प्रीमियम</string>
    <string name="killed">Fib बंद है।</string>
    <string name="enable">Fib चालू करें</string>
    <string name="listening">“Hey Fib” सुन रहा है</string>
    <string name="kill">Fib बंद करें</string>
    <string name="nothing">कुछ नहीं मिला।</string>
</resources>
```

### `res/values-ar/strings.xml`
```xml
<resources>
    <string name="kill_hint">يمكنك دائمًا أن تقول “kill Fib”.</string>
    <string name="perm_mic">يحتاج Fib إلى الميكروفون لسماع “Hey Fib” وأوامرك. %1$s</string>
    <string name="perm_notif">يحتاج Fib إلى الإشعارات لإظهار أنه يستمع. %1$s</string>
    <string name="perm_contacts">يحتاج Fib إلى جهات الاتصال للاتصال بالأسماء. %1$s</string>
    <string name="perm_phone">يحتاج Fib إلى إذن الهاتف لإجراء المكالمات. %1$s</string>
    <string name="perm_camera">يحتاج Fib إلى الكاميرا لالتقاط الصور. %1$s</string>
    <string name="perm_location">يحتاج Fib إلى موقعك للملاحة. %1$s</string>
    <string name="allow">السماح</string>
    <string name="not_now">ليس الآن</string>
    <string name="wl_title">يرجى إضافة Fib إلى القائمة البيضاء</string>
    <string name="wl_msg">قد يمنع هاتفك Fib من الاستماع دائمًا. يرجى إضافة Fib إلى القائمة البيضاء (البطارية: غير مقيّد، والسماح بالتشغيل التلقائي) ليعمل “Hey Fib” دائمًا. %1$s</string>
    <string name="wl_stopped">أوقف هاتفك Fib. اضغط لإضافته إلى القائمة البيضاء.</string>
    <string name="hint">اكتب إلى Fib…</string>
    <string name="greeting">مرحبًا، أنا Fib. قل “Hey Fib” أو اكتب أدناه. مجانًا: بواسطة ويكيبيديا.</string>
    <string name="premium">⭐ بريميوم 10€/شهر</string>
    <string name="premium_on">⭐ بريميوم</string>
    <string name="killed">تم تعطيل Fib.</string>
    <string name="enable">تفعيل Fib</string>
    <string name="listening">يستمع إلى “Hey Fib”</string>
    <string name="kill">إيقاف Fib</string>
    <string name="nothing">لم أجد شيئًا.</string>
</resources>
```

---

## Kotlin sources — folder `app/src/main/java/com/fib/jarvis/`

### `FibState.kt` – kill switch, premium flag, memory
```kotlin
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

    /** Kill Fib: persists, stops the listening service. Stays off (even after restart) until enabled again. */
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

    // ---- memory: Free = last 10 messages, Premium = last 500 ----
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
```

### `Ai.kt` – Wikipedia (free) / Gemini (premium)
```kotlin
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
```

### `Router.kt` – device commands (return a reply if handled, else null → AI)
```kotlin
package com.fib.jarvis

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.ContactsContract

object Router {
    private const val NEW = Intent.FLAG_ACTIVITY_NEW_TASK

    fun handle(c: Context, raw: String): String? {
        val t = FibState.norm(raw)
        // flashlight
        if (Regex("flashlight|torch|linterna|lampe de poche|lampe torche|torcia|手电筒|टॉर्च|مصباح").containsMatchIn(t)) {
            val off = Regex("\\boff\\b|apaga|éteins|spegni|关|बंद|اطفئ").containsMatchIn(t)
            return try {
                val m = c.getSystemService(CameraManager::class.java)
                m.setTorchMode(m.cameraIdList[0], !off); "✓"
            } catch (e: Exception) { null }
        }
        // call
        Regex("^(?:call|phone|llama a|llama|appelle|appeler|chiama)\\s+(.+)$").find(t)?.let { return dial(c, it.groupValues[1]) }
        // alarm / timer
        if (Regex("alarm|timer|alarma|réveil|minuteur|sveglia|闹钟|अलार्म|منبه").containsMatchIn(t)) {
            c.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(NEW)); return "✓"
        }
        // navigation
        Regex("^(?:navigate to|directions to|navega a|navigue vers|naviga verso|naviga a)\\s+(.+)$").find(t)?.let {
            c.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + Uri.encode(it.groupValues[1]))).addFlags(NEW)); return "✓"
        }
        // open app
        Regex("^(?:open|abre|ouvre|apri)\\s+(.+)$").find(t)?.let { return openApp(c, it.groupValues[1]) }
        // web search
        Regex("^(?:google|search for|busca|cherche|cerca)\\s+(.+)$").find(t)?.let {
            c.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(it.groupValues[1]))).addFlags(NEW)); return "✓"
        }
        return null
    }

    private fun dial(c: Context, name: String): String {
        var number: String? = null
        if (c.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            c.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?", arrayOf("%$name%"), null)
                ?.use { if (it.moveToFirst()) number = it.getString(0) }
        }
        val target = number ?: name.filter { it.isDigit() || it == '+' }
        if (target.isEmpty()) return c.getString(R.string.nothing)
        val action = if (c.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) Intent.ACTION_CALL else Intent.ACTION_DIAL
        c.startActivity(Intent(action, Uri.parse("tel:$target")).addFlags(NEW)); return "✓"
    }

    private fun openApp(c: Context, label: String): String {
        val pm = c.packageManager
        val apps = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        val hit = apps.firstOrNull { it.loadLabel(pm).toString().lowercase().contains(label) } ?: return c.getString(R.string.nothing)
        val i = pm.getLaunchIntentForPackage(hit.activityInfo.packageName) ?: return c.getString(R.string.nothing)
        c.startActivity(i.addFlags(NEW)); return "✓"
    }
}
```

### `Brain.kt`
```kotlin
package com.fib.jarvis

import android.content.Context

object Brain {
    /** One entry point for typed and spoken input. */
    suspend fun reply(c: Context, text: String): String {
        FibState.remember(c, Msg(true, text))
        val r = Router.handle(c, text) ?: Ai.answer(c, text)
        FibState.remember(c, Msg(false, r))
        return r
    }
}
```

### `ListenService.kt` – always listening for "Hey Fib"
```kotlin
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
    private var awake = false
    private val h = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val wake = Regex("hey,? ?fi(b|p|bb)|hay fib|a fib")

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
        val t = FibState.norm(text)
        if (FibState.isKillPhrase(t)) { FibState.setKilled(this, true); return }   // kill works any time
        val m = wake.find(t)
        when {
            m != null -> {
                val cmd = t.substring(m.range.last + 1).trim()
                if (cmd.isEmpty()) { awake = true; say("…") } else run(cmd)
            }
            awake -> { awake = false; run(t) }
        }
    }

    private fun run(cmd: String) = scope.launch { say(Brain.reply(this@ListenService, cmd)) }

    private fun say(s: String) { tts?.speak(s.take(600), TextToSpeech.QUEUE_FLUSH, null, "fib") }

    override fun onDestroy() {
        sr?.destroy(); tts?.shutdown(); scope.cancel()
        if (!FibState.isKilled(this)) {   // stopped by the system, not by the user → ask to whitelist
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
```

### `Whitelist.kt`
```kotlin
package com.fib.jarvis

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

object Whitelist {
    fun isBlocked(c: Context) =
        !c.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(c.packageName)

    fun request(a: Activity) {
        try { a.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${a.packageName}"))) }
        catch (e: Exception) { a.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
    }

    /** Extra auto-start screens on aggressive phone brands. */
    fun openAutostart(a: Activity) {
        val t = listOf(
            "com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity",
            "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
            "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
            "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
            "com.samsung.android.lool" to "com.samsung.android.sm.battery.ui.BatteryActivity")
        for ((p, cl) in t) try { a.startActivity(Intent().setComponent(ComponentName(p, cl))); return } catch (_: Exception) {}
    }
}
```

### `Billing.kt` – Premium €10/month (create product `fib_premium_monthly` in Play Console)
```kotlin
package com.fib.jarvis

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*

class Billing(private val ctx: Context, private val onChange: (Boolean) -> Unit) : PurchasesUpdatedListener {
    private val client = BillingClient.newBuilder(ctx).setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).build()

    fun connect() = client.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(r: BillingResult) { refresh() }
        override fun onBillingServiceDisconnected() {}
    })

    fun buy(a: Activity) {
        val q = QueryProductDetailsParams.newBuilder().setProductList(listOf(
            QueryProductDetailsParams.Product.newBuilder().setProductId("fib_premium_monthly")
                .setProductType(BillingClient.ProductType.SUBS).build())).build()
        client.queryProductDetailsAsync(q) { _, list ->
            val d = list.firstOrNull() ?: return@queryProductDetailsAsync
            val token = d.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return@queryProductDetailsAsync
            val p = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(d).setOfferToken(token).build()
            client.launchBillingFlow(a, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(p)).build())
        }
    }

    override fun onPurchasesUpdated(r: BillingResult, p: MutableList<Purchase>?) {
        p?.forEach { if (!it.isAcknowledged) client.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(it.purchaseToken).build()) {} }
        refresh()
    }

    fun refresh() = client.queryPurchasesAsync(
        QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { _, l ->
        val on = l.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        FibState.setPremium(ctx, on); onChange(on)
    }
}
```

### `MainActivity.kt` – ChatGPT-style UI + permission onboarding with the "kill Fib" reminder
```kotlin
package com.fib.jarvis

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var billing: Billing
    private val premium = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        premium.value = FibState.isPremium(this)
        billing = Billing(this) { premium.value = it }.also { it.connect() }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF38BDF8))) {
                App(this, premium.value) { billing.buy(this) }
            }
        }
    }
}

private val LANGS = listOf("en" to "English", "zh" to "中文", "hi" to "हिन्दी", "es" to "Español",
    "fr" to "Français", "ar" to "العربية", "it" to "Italiano")

private fun steps(): List<Pair<String, Int>> = buildList {
    add(Manifest.permission.RECORD_AUDIO to R.string.perm_mic)
    if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS to R.string.perm_notif)
    add(Manifest.permission.READ_CONTACTS to R.string.perm_contacts)
    add(Manifest.permission.CALL_PHONE to R.string.perm_phone)
    add(Manifest.permission.CAMERA to R.string.perm_camera)
    add(Manifest.permission.ACCESS_FINE_LOCATION to R.string.perm_location)
}

private fun nextMissing(a: Activity, s: List<Pair<String, Int>>, from: Int): Int {
    var i = from
    while (i < s.size && a.checkSelfPermission(s[i].first) == android.content.pm.PackageManager.PERMISSION_GRANTED) i++
    return i
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(act: Activity, premium: Boolean, onBuy: () -> Unit) {
    val s = remember { steps() }
    var i by remember { mutableIntStateOf(nextMissing(act, s, 0)) }
    var wlAsked by remember { mutableStateOf(false) }
    var killed by remember { mutableStateOf(FibState.isKilled(act)) }
    val msgs = remember { mutableStateListOf<Msg>().apply { addAll(FibState.history(act)) } }
    var input by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val list = rememberLazyListState()
    val greeting = stringResource(R.string.greeting)

    // keep UI in sync with the notification "Kill Fib" button / voice kill
    LaunchedEffect(Unit) { while (true) { killed = FibState.isKilled(act); delay(700) } }

    // 1) Permissions, one at a time, each with the "kill Fib" reminder
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        i = nextMissing(act, s, i + 1)
    }
    if (i < s.size) {
        AlertDialog(
            onDismissRequest = {},
            text = { Text(act.getString(s[i].second, act.getString(R.string.kill_hint))) },
            confirmButton = { TextButton({ launcher.launch(s[i].first) }) { Text(stringResource(R.string.allow)) } },
            dismissButton = { TextButton({ i = nextMissing(act, s, i + 1) }) { Text(stringResource(R.string.not_now)) } })
    }
    // 2) Whitelist request (also shown again if the phone stopped Fib)
    else if (!wlAsked && (Whitelist.isBlocked(act) || FibState.wasStoppedBySystem(act))) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.wl_title)) },
            text = { Text(act.getString(R.string.wl_msg, act.getString(R.string.kill_hint))) },
            confirmButton = { TextButton({ wlAsked = true; Whitelist.request(act); Whitelist.openAutostart(act) }) { Text(stringResource(R.string.allow)) } },
            dismissButton = { TextButton({ wlAsked = true }) { Text(stringResource(R.string.not_now)) } })
    } else {
        LaunchedEffect(Unit) { FibState.startListening(act) }
    }

    fun send(text: String) {
        if (text.isBlank()) return
        msgs.add(Msg(true, text)); input = ""
        if (FibState.isKillPhrase(text)) { FibState.setKilled(act, true); killed = true; msgs.add(Msg(false, act.getString(R.string.killed))); return }
        scope.launch { msgs.add(Msg(false, Brain.reply(act, text))); list.animateScrollToItem(msgs.size) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Fib") }, actions = {
                TextButton(onClick = { if (!premium) onBuy() }) {
                    Text(stringResource(if (premium) R.string.premium_on else R.string.premium)) }
                Box {
                    TextButton({ menu = true }) { Text("🌐") }
                    DropdownMenu(menu, { menu = false }) {
                        LANGS.forEach { (code, name) ->
                            DropdownMenuItem(text = { Text(name) }, onClick = {
                                menu = false
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
                            })
                        }
                    }
                }
            })
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                if (killed) {
                    Row(Modifier.fillMaxWidth().background(Color(0xFF7F1D1D), RoundedCornerShape(12.dp)).padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.killed), Modifier.weight(1f))
                        Button({ FibState.setKilled(act, false); killed = false }) { Text(stringResource(R.string.enable)) }
                    }
                } else Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(input, { input = it }, Modifier.weight(1f), shape = RoundedCornerShape(24.dp),
                        placeholder = { Text(stringResource(R.string.hint)) }, singleLine = true)
                    Spacer(Modifier.width(8.dp))
                    Button({ send(input) }) { Text("➤") }
                }
            }
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize().padding(horizontal = 16.dp), state = list,
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Bubble(Msg(false, greeting)) }
            items(msgs) { Bubble(it) }
        }
    }
}

@Composable
fun Bubble(m: Msg) {
    Box(Modifier.fillMaxWidth(), contentAlignment = if (m.user) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(shape = RoundedCornerShape(16.dp), color = if (m.user) Color(0xFF1E3A8A) else Color(0xFF1A2233),
            modifier = Modifier.widthIn(max = 320.dp)) { Text(m.text, Modifier.padding(12.dp)) }
    }
}
```

---

## Known Android limits (so nothing surprises you)
- **Always-listening** uses a foreground service with a permanent notification (required by Android). It works until the phone's battery manager stops it – that's why Fib asks to be whitelisted, and re-asks (notification + dialog) if the system stops it.
- **Boot:** Android 14+ forbids starting a microphone service at boot, so after a restart the user opens Fib once to resume listening.
- **Background launching:** on Android 10+, voice commands that open other screens (dialer, maps, apps) may be blocked while Fib is in the background. The flashlight works anyway. To widen this, ask for "Display over other apps" (`SYSTEM_ALERT_WINDOW`).
- **Speech recogniser** restarts continuously; some phones beep or use more battery. For a better "Hey Fib" use Picovoice Porcupine (custom keyword "Hey Fib", on-device) and call `Brain.reply` from its callback.
- **Google Play review:** needs a microphone foreground-service declaration (with demo video), a justification for battery-optimisation exemption, a Data Safety form and the privacy policy from `Fib-Android-App.md`. If the battery exemption is rejected, remove that permission – the dialog then falls back to the settings screen.
- The Gemini key in `BuildConfig` can be extracted from the APK; use a backend proxy before launch.
- Web ↔ app: the web page's **Install** button should point to your Play Store URL (`PLAY_URL` in `Fib-web.html`).

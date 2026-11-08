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
    add(Manifest.permission.SEND_SMS to R.string.perm_sms)
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

    LaunchedEffect(Unit) { while (true) { killed = FibState.isKilled(act); delay(700) } }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        i = nextMissing(act, s, i + 1)
    }
    if (i < s.size) {
        AlertDialog(
            onDismissRequest = {},
            text = { Text(act.getString(s[i].second, act.getString(R.string.kill_hint))) },
            confirmButton = { TextButton({ launcher.launch(s[i].first) }) { Text(stringResource(R.string.allow)) } },
            dismissButton = { TextButton({ i = nextMissing(act, s, i + 1) }) { Text(stringResource(R.string.not_now)) } })
    } else if (!wlAsked && (Whitelist.isBlocked(act) || FibState.wasStoppedBySystem(act))) {
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

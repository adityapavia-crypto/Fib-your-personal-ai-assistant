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

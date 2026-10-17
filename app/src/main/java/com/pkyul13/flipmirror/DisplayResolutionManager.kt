package com.pkyul13.flipmirror

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DisplayState(
    val physicalWidth: Int = 1080,
    val physicalHeight: Int = 2640,
    val overrideWidth: Int? = null,
    val overrideHeight: Int? = null,
    val physicalDensity: Int = 420,
    val overrideDensity: Int? = null,
    val is16x9Active: Boolean = false,
    val statusText: String = "초기화 중..."
) {
    val currentWidth: Int get() = overrideWidth ?: physicalWidth
    val currentHeight: Int get() = overrideHeight ?: physicalHeight
    val currentDensity: Int get() = overrideDensity ?: physicalDensity
    val isOverrideApplied: Boolean get() = overrideWidth != null || overrideHeight != null || overrideDensity != null
}

object DisplayResolutionManager {
    private const val NOTIFICATION_ID = 3001
    const val CHANNEL_ID = "flipmirror_status_channel"

    const val ACTION_RESTORE_DEFAULT = "com.pkyul13.flipmirror.ACTION_RESTORE_DEFAULT"
    const val ACTION_APPLY_16_9 = "com.pkyul13.flipmirror.ACTION_APPLY_16_9"

    private val _displayState = MutableStateFlow(DisplayState())
    val displayState: StateFlow<DisplayState> = _displayState

    private val scope = CoroutineScope(Dispatchers.IO)

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "미러링 상태 알림"
            val descriptionText = "16:9 미러링 모드 활성화 상태 및 복구 제어"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun refreshState(context: Context, onComplete: ((DisplayState) -> Unit)? = null) {
        scope.launch {
            val sizeOutput = ShizukuManager.executeShellCommand("wm size")
            val densityOutput = ShizukuManager.executeShellCommand("wm density")

            var pWidth = 1080
            var pHeight = 2640
            var oWidth: Int? = null
            var oHeight: Int? = null

            // Parse size
            sizeOutput.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("Physical size:", ignoreCase = true)) {
                    val parts = trimmed.substringAfter(":").trim().split("x")
                    if (parts.size == 2) {
                        pWidth = parts[0].toIntOrNull() ?: pWidth
                        pHeight = parts[1].toIntOrNull() ?: pHeight
                    }
                } else if (trimmed.startsWith("Override size:", ignoreCase = true)) {
                    val parts = trimmed.substringAfter(":").trim().split("x")
                    if (parts.size == 2) {
                        oWidth = parts[0].toIntOrNull()
                        oHeight = parts[1].toIntOrNull()
                    }
                }
            }

            var pDensity = 420
            var oDensity: Int? = null

            // Parse density
            densityOutput.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("Physical density:", ignoreCase = true)) {
                    pDensity = trimmed.substringAfter(":").trim().toIntOrNull() ?: pDensity
                } else if (trimmed.startsWith("Override density:", ignoreCase = true)) {
                    oDensity = trimmed.substringAfter(":").trim().toIntOrNull()
                }
            }

            val is16x9 = if (oWidth != null && oHeight != null) {
                val ratio = oHeight.toDouble() / oWidth.toDouble()
                val isPort16x9 = Math.abs(ratio - (16.0 / 9.0)) < 0.05
                val isLand16x9 = Math.abs((1.0 / ratio) - (16.0 / 9.0)) < 0.05
                isPort16x9 || isLand16x9
            } else {
                false
            }

            val statusText = when {
                oWidth != null && oHeight != null -> {
                    if (oHeight > oWidth) "16:9 세로 모드 (${oWidth}x${oHeight})"
                    else "16:9 가로 모드 (${oWidth}x${oHeight})"
                }
                else -> "기본 화면 비율 (${pWidth}x${pHeight})"
            }

            val newState = DisplayState(
                physicalWidth = pWidth,
                physicalHeight = pHeight,
                overrideWidth = oWidth,
                overrideHeight = oHeight,
                physicalDensity = pDensity,
                overrideDensity = oDensity,
                is16x9Active = is16x9,
                statusText = statusText
            )

            _displayState.value = newState

            if (newState.isOverrideApplied) {
                MonitorWatcherService.start(context, newState.statusText)
            } else {
                MonitorWatcherService.stop(context)
                dismissNotification(context)
            }

            onComplete?.invoke(newState)
        }
    }

    private const val PREFS_NAME = "flipmirror_prefs"
    private const val KEY_PREFERRED_DPI = "preferred_dpi"
    private const val KEY_AUTO_RESTORE = "auto_restore_on_disconnect"
    const val DEFAULT_DPI = 240
    const val MIN_DPI = 100
    const val MAX_DPI = 400

    fun getPreferredDpi(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_PREFERRED_DPI, DEFAULT_DPI).coerceIn(MIN_DPI, MAX_DPI)
    }

    fun setPreferredDpi(context: Context, dpi: Int) {
        val coerced = dpi.coerceIn(MIN_DPI, MAX_DPI)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_PREFERRED_DPI, coerced).apply()
    }

    fun isAutoRestoreEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_RESTORE, true)
    }

    fun setAutoRestoreEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_RESTORE, enabled).apply()
    }

    fun apply16x9Portrait(
        context: Context,
        width: Int = 1080,
        height: Int = 1920,
        density: Int = getPreferredDpi(context),
        onDone: (() -> Unit)? = null
    ) {
        scope.launch {
            ShizukuManager.executeShellCommand("wm size ${width}x${height}")
            ShizukuManager.executeShellCommand("wm density $density")
            refreshState(context) { onDone?.invoke() }
        }
    }

    fun apply16x9Landscape(
        context: Context,
        width: Int = 1920,
        height: Int = 1080,
        density: Int = getPreferredDpi(context),
        onDone: (() -> Unit)? = null
    ) {
        scope.launch {
            ShizukuManager.executeShellCommand("wm size ${width}x${height}")
            ShizukuManager.executeShellCommand("wm density $density")
            refreshState(context) { onDone?.invoke() }
        }
    }

    fun applyDensityOnly(context: Context, density: Int, onDone: (() -> Unit)? = null) {
        scope.launch {
            setPreferredDpi(context, density)
            ShizukuManager.executeShellCommand("wm density $density")
            refreshState(context) { onDone?.invoke() }
        }
    }

    fun applyCustom(context: Context, width: Int, height: Int, density: Int, onDone: (() -> Unit)? = null) {
        scope.launch {
            setPreferredDpi(context, density)
            ShizukuManager.executeShellCommand("wm size ${width}x${height}")
            ShizukuManager.executeShellCommand("wm density $density")
            refreshState(context) { onDone?.invoke() }
        }
    }

    fun resetToDefault(context: Context, onDone: (() -> Unit)? = null) {
        scope.launch {
            ShizukuManager.executeShellCommand("wm size reset")
            ShizukuManager.executeShellCommand("wm density reset")
            MonitorWatcherService.stop(context)
            refreshState(context) {
                dismissNotification(context)
                onDone?.invoke()
            }
        }
    }

    fun toggle16x9(context: Context, onDone: (() -> Unit)? = null) {
        if (_displayState.value.isOverrideApplied) {
            resetToDefault(context, onDone)
        } else {
            apply16x9Portrait(context, onDone = onDone)
        }
    }

    fun dismissNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}

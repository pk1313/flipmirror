package com.pkyul13.flipmirror

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
    private const val TAG = "DisplayResManager"
    private const val NOTIFICATION_ID = 3001
    const val CHANNEL_ID = "flipmirror_status_channel"

    const val ACTION_RESTORE_DEFAULT = "com.pkyul13.flipmirror.ACTION_RESTORE_DEFAULT"
    const val ACTION_APPLY_16_9 = "com.pkyul13.flipmirror.ACTION_APPLY_16_9"

    private val _displayState = MutableStateFlow(DisplayState())
    val displayState: StateFlow<DisplayState> = _displayState

    private val scope = CoroutineScope(Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var isDisplayListenerRegistered = false
    private var hadExternalDisplay = false
    private var isNotificationShowing = false

    fun init(context: Context) {
        initNotificationChannel(context)
        initDisplayListener(context)
    }

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

    private fun initDisplayListener(context: Context) {
        if (isDisplayListenerRegistered) return
        val appContext = context.applicationContext
        val dm = appContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

        hadExternalDisplay = DisplayHelper.isExternalDisplayConnected(appContext)

        dm.registerDisplayListener(object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) {
                if (DisplayHelper.isExternalDisplayConnected(appContext)) {
                    hadExternalDisplay = true
                    Log.d(TAG, "External display connected (id=$displayId)")
                }
            }

            override fun onDisplayChanged(displayId: Int) {}

            override fun onDisplayRemoved(displayId: Int) {
                val isConnectedNow = DisplayHelper.isExternalDisplayConnected(appContext)
                Log.d(TAG, "Display removed: was=$hadExternalDisplay, now=$isConnectedNow")

                // When external display is unplugged while an override (16:9) is applied:
                if (hadExternalDisplay && !isConnectedNow) {
                    hadExternalDisplay = false
                    if (isAutoRestoreEnabled(appContext) && _displayState.value.isOverrideApplied) {
                        Log.i(TAG, "Auto-restoring resolution to native due to disconnect...")
                        resetToDefault(appContext) {
                            mainHandler.post {
                                Toast.makeText(
                                    appContext,
                                    appContext.getString(R.string.toast_auto_restored),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }, mainHandler)

        isDisplayListenerRegistered = true
        Log.d(TAG, "DisplayListener registered successfully.")
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

            val stateChanged = _displayState.value != newState
            _displayState.value = newState

            // Update persistent ongoing notification safely only when state changed
            if (newState.isOverrideApplied) {
                if (stateChanged || !isNotificationShowing) {
                    showPersistentNotification(context, newState)
                    isNotificationShowing = true
                }
            } else {
                if (isNotificationShowing) {
                    dismissNotification(context)
                    isNotificationShowing = false
                }
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

    private fun showPersistentNotification(context: Context, state: DisplayState) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val restoreIntent = Intent(context, RestoreReceiver::class.java).apply {
            action = ACTION_RESTORE_DEFAULT
        }
        val pendingRestoreIntent = PendingIntent.getBroadcast(
            context,
            1,
            restoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tile_mirror)
            .setContentTitle(context.getString(R.string.notification_active_title))
            .setContentText("현재: ${state.currentWidth}x${state.currentHeight} (${state.currentDensity} DPI)")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingContentIntent)
            .addAction(R.drawable.ic_tile_mirror, context.getString(R.string.btn_restore_short), pendingRestoreIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun dismissNotification(context: Context) {
        isNotificationShowing = false
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}

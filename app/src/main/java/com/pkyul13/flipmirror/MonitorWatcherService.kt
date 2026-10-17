package com.pkyul13.flipmirror

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Foreground Service that stays active while 16:9 mode is enabled.
 * Listens to display connection events and automatically reverts to native
 * resolution when the external display is unplugged.
 */
class MonitorWatcherService : Service() {

    companion object {
        private const val TAG = "MonitorWatcherService"
        const val NOTIFICATION_ID = 3001
        const val ACTION_START = "com.pkyul13.flipmirror.ACTION_START_MONITORING"
        const val ACTION_STOP = "com.pkyul13.flipmirror.ACTION_STOP_MONITORING"
        const val EXTRA_STATUS_TEXT = "extra_status_text"

        fun start(context: Context, statusText: String? = null) {
            val intent = Intent(context, MonitorWatcherService::class.java).apply {
                action = ACTION_START
                if (statusText != null) {
                    putExtra(EXTRA_STATUS_TEXT, statusText)
                }
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start MonitorWatcherService", e)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MonitorWatcherService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop MonitorWatcherService", e)
            }
        }
    }

    private lateinit var displayManager: DisplayManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Main)

    private var wasExternalConnected = false

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            if (DisplayHelper.isExternalDisplayConnected(this@MonitorWatcherService)) {
                wasExternalConnected = true
                Log.d(TAG, "External display added (id=$displayId)")
            }
        }

        override fun onDisplayChanged(displayId: Int) {}

        override fun onDisplayRemoved(displayId: Int) {
            Log.d(TAG, "Display removed (id=$displayId). Checking connection status...")
            checkDisconnectAndRestore()
        }
    }

    override fun onCreate() {
        super.onCreate()
        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(displayListener, mainHandler)
        wasExternalConnected = DisplayHelper.isExternalDisplayConnected(this)
        Log.d(TAG, "Service created. Initial external display connected: $wasExternalConnected")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val statusText = intent?.getStringExtra(EXTRA_STATUS_TEXT) ?: "16:9 미러링 모드 활성"
        val notification = buildNotification(statusText)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            } else {
                0
            }
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, serviceType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (DisplayHelper.isExternalDisplayConnected(this)) {
            wasExternalConnected = true
        }

        return START_STICKY
    }

    private fun checkDisconnectAndRestore() {
        if (!DisplayResolutionManager.isAutoRestoreEnabled(this)) {
            Log.d(TAG, "Auto-restore is disabled in preferences. Skipping.")
            return
        }

        val isCurrentlyConnected = DisplayHelper.isExternalDisplayConnected(this)
        Log.d(TAG, "wasExternalConnected=$wasExternalConnected, isCurrentlyConnected=$isCurrentlyConnected")

        if (wasExternalConnected && !isCurrentlyConnected) {
            Log.i(TAG, "External display disconnected! Triggering auto-restore...")
            scope.launch {
                DisplayResolutionManager.resetToDefault(this@MonitorWatcherService) {
                    mainHandler.post {
                        val msg = getString(R.string.toast_auto_restored)
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                    }
                    stopSelf()
                }
            }
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingContentIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val restoreIntent = Intent(this, RestoreReceiver::class.java).apply {
            action = DisplayResolutionManager.ACTION_RESTORE_DEFAULT
        }
        val pendingRestoreIntent = PendingIntent.getBroadcast(
            this,
            1,
            restoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DisplayResolutionManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tile_mirror)
            .setContentTitle(getString(R.string.notification_active_title))
            .setContentText(statusText)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingContentIntent)
            .addAction(R.drawable.ic_tile_mirror, getString(R.string.btn_restore_short), pendingRestoreIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            displayManager.unregisterDisplayListener(displayListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering displayListener", e)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        Log.d(TAG, "Service destroyed.")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

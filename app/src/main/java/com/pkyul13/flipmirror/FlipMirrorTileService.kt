package com.pkyul13.flipmirror

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class FlipMirrorTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        ShizukuManager.checkStatus()

        if (!ShizukuManager.isShizukuAvailable.value || !ShizukuManager.hasPermission.value) {
            Toast.makeText(this, "Shizuku 권한이 필요합니다. 앱을 실행합니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = android.app.PendingIntent.getActivity(
                    this, 0, intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        val state = DisplayResolutionManager.displayState.value
        if (state.isOverrideApplied) {
            DisplayResolutionManager.resetToDefault(this) {
                updateTileState()
                Toast.makeText(this, "기본 22:9 화면으로 복구되었습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            DisplayResolutionManager.apply16x9Portrait(this) {
                updateTileState()
                Toast.makeText(this, "16:9 미러링 화면으로 전환되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        ShizukuManager.checkStatus()

        if (!ShizukuManager.isShizukuAvailable.value || !ShizukuManager.hasPermission.value) {
            tile.state = Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Shizuku 필요"
            }
            tile.updateTile()
            return
        }

        DisplayResolutionManager.refreshState(this) { state ->
            val active = state.isOverrideApplied
            tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (active) "16:9 적용됨" else "기본 22:9"
            }
            tile.updateTile()
        }
    }
}

package com.pkyul13.flipmirror

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display

data class ConnectedDisplay(
    val id: Int,
    val name: String,
    val width: Int,
    val height: Int,
    val isExternal: Boolean
)

object DisplayHelper {
    fun getExternalDisplay(context: Context): ConnectedDisplay? {
        val dm = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        for (display in dm.displays) {
            // 0 is internal phone display, 1 is Z Flip cover screen
            if (display.displayId != Display.DEFAULT_DISPLAY && display.displayId != 1) {
                return ConnectedDisplay(
                    id = display.displayId,
                    name = display.name ?: "외부 모니터",
                    width = display.mode.physicalWidth,
                    height = display.mode.physicalHeight,
                    isExternal = true
                )
            }
        }
        return null
    }

    fun isExternalDisplayConnected(context: Context): Boolean {
        return getExternalDisplay(context) != null
    }
}

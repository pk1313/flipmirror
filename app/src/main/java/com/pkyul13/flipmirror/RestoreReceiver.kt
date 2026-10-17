package com.pkyul13.flipmirror

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class RestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            DisplayResolutionManager.ACTION_RESTORE_DEFAULT -> {
                DisplayResolutionManager.resetToDefault(context) {
                    Toast.makeText(context, "원래 화면 비율(22:9)로 복구되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            DisplayResolutionManager.ACTION_APPLY_16_9 -> {
                DisplayResolutionManager.apply16x9Portrait(context) {
                    Toast.makeText(context, "16:9 미러링 비율이 적용되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

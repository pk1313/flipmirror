package com.pkyul13.flipmirror

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuManager {
    private const val REQUEST_CODE = 2001

    private val _isShizukuAvailable = MutableStateFlow(false)
    val isShizukuAvailable: StateFlow<Boolean> = _isShizukuAvailable

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == REQUEST_CODE) {
            _hasPermission.value = grantResult == PackageManager.PERMISSION_GRANTED
        }
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _isShizukuAvailable.value = false
        _hasPermission.value = false
    }

    fun init() {
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionListener)
        checkStatus()
    }

    fun destroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionListener)
    }

    fun checkStatus() {
        val available = try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
        _isShizukuAvailable.value = available

        if (available) {
            val granted = try {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            } catch (e: Throwable) {
                false
            }
            _hasPermission.value = granted
        } else {
            _hasPermission.value = false
        }
    }

    fun requestPermission() {
        if (_isShizukuAvailable.value) {
            try {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(REQUEST_CODE)
                } else {
                    _hasPermission.value = true
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    suspend fun executeShellCommand(cmd: String): String = withContext(Dispatchers.IO) {
        if (!_hasPermission.value) {
            checkStatus()
            if (!_hasPermission.value) {
                return@withContext "ERROR: Shizuku permission not granted"
            }
        }
        withTimeoutOrNull(5000) {
            try {
                val clazz = Class.forName("rikka.shizuku.Shizuku")
                val method = clazz.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                )
                method.isAccessible = true
                val process = method.invoke(null, arrayOf("sh", "-c", "$cmd 2>&1"), null, null) as Process
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = StringBuilder()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }

                process.waitFor()
                output.toString().trim()
            } catch (e: Throwable) {
                "ERROR: ${e.message}"
            }
        } ?: "ERROR: Shell command timed out"
    }
}

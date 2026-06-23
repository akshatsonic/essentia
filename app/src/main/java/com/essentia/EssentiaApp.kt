package com.essentia

import android.app.Application
import android.util.Log
import com.essentia.crash.CrashLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EssentiaApp : Application() {

    val appScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CrashLogger.coroutineExceptionHandler()
    )

    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
        Log.i(TAG, "onCreate start")
        CrashLogger.install(this)
        Log.i(TAG, "CrashLogger installed; dir=${CrashLogger.directoryPath()}")

        // Warm up the DataStore + repository on a background coroutine so any data-loading
        // errors are caught by the coroutine exception handler instead of crashing the UI thread.
        appScope.launch {
            runCatching { AppModule.userPreferences }
                .onSuccess { Log.i(TAG, "UserPreferences ready") }
                .onFailure { Log.e(TAG, "UserPreferences warmup failed", it) }
            runCatching { AppModule.secureStore }
                .onSuccess { Log.i(TAG, "SecureStore ready") }
                .onFailure { Log.e(TAG, "SecureStore warmup failed", it) }
        }
        Log.i(TAG, "onCreate done")
    }

    companion object {
        private const val TAG = "EssentiaApp"
    }
}

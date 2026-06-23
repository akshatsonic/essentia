package com.essentia.crash

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Global crash logger. Captures uncaught exceptions from any thread and any coroutine,
 * writes a structured report to a file in the app's internal storage, then re-throws
 * to the previous handler so the OS can still kill the process (or whatever it normally does).
 *
 * Logs are written to: `<filesDir>/crash_logs/crash_<timestamp>.txt`
 *
 * View the most recent log from Settings → Diagnostics → View Crash Log.
 */
object CrashLogger {

    private const val TAG = "EssentiaCrash"
    private const val DIR_NAME = "crash_logs"
    private const val MAX_LOGS = 20

    @Volatile
    private var initialized = false

    @Volatile
    private var appContext: Context? = null

    fun install(context: Context) {
        if (initialized) return
        initialized = true
        appContext = context.applicationContext

        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching { writeCrashLog(thread, throwable) }
                .onFailure { Log.e(TAG, "Failed to write crash log", it) }
            // Delegate to the previous handler so the OS still terminates the process
            previous?.uncaughtException(thread, throwable)
                ?: kotlin.system.exitProcess(2)
        }
        Log.i(TAG, "CrashLogger installed. Directory: ${directoryPath()}")
    }

    /**
     * Handler for uncaught coroutine exceptions. Use as a top-level supervisor on
     * app-scoped coroutine contexts.
     */
    fun coroutineExceptionHandler(): CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            runCatching {
                val log = buildReport(
                    threadName = Thread.currentThread().name,
                    throwable = throwable,
                    contextLabel = "coroutine"
                )
                writeLog(log, label = "coroutine_crash")
            }
        }

    /** Returns the most recent crash log file, or null if none exist. */
    fun latestLog(): File? = logFiles().maxByOrNull { it.lastModified() }

    /** Returns all crash log files, newest first. */
    fun logFiles(): List<File> {
        val dir = directory() ?: return emptyList()
        return dir.listFiles { f -> f.isFile && f.name.endsWith(".txt") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    /** Path printed at startup so the user can adb-pull it. */
    fun directoryPath(): String? = directory()?.absolutePath

    fun clearAll() {
        logFiles().forEach { runCatching { it.delete() } }
    }

    private fun directory(): File? {
        val ctx = appContext ?: return null
        val dir = File(ctx.filesDir, DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun writeCrashLog(thread: Thread, throwable: Throwable) {
        val log = buildReport(thread.name, throwable, contextLabel = "thread")
        writeLog(log, label = "crash")
    }

    private fun buildReport(
        threadName: String,
        throwable: Throwable,
        contextLabel: String
    ): String {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
            .format(Date())
        val isoTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            .format(Date())

        return buildString {
            appendLine("====================================================")
            appendLine("ESSENTIA CRASH REPORT")
            appendLine("====================================================")
            appendLine("Timestamp (local): $timestamp")
            appendLine("Timestamp (ISO):   $isoTime")
            appendLine("Context:           $contextLabel")
            appendLine("Thread:            $threadName")
            appendLine()
            appendLine("--- DEVICE ---")
            appendLine("Manufacturer: ${Build.MANUFACTURER}")
            appendLine("Model:        ${Build.MODEL}")
            appendLine("Brand:        ${Build.BRAND}")
            appendLine("Device:       ${Build.DEVICE}")
            appendLine("Product:      ${Build.PRODUCT}")
            appendLine("Hardware:     ${Build.HARDWARE}")
            appendLine("Android SDK:  ${Build.VERSION.SDK_INT}")
            appendLine("Android Rel:  ${Build.VERSION.RELEASE}")
            appendLine("Fingerprint:  ${Build.FINGERPRINT}")
            appendLine()
            appendLine("--- APP ---")
            runCatching {
                val ctx = appContext
                if (ctx != null) {
                    val pm = ctx.packageManager
                    val info = pm.getPackageInfo(ctx.packageName, 0)
                    appendLine("Package:      ${ctx.packageName}")
                    appendLine("Version:      ${info.versionName}")
                    appendLine("Version code: ${info.longVersionCode}")
                }
            }
            appendLine()
            appendLine("--- EXCEPTION ---")
            appendLine("Type:     ${throwable.javaClass.name}")
            appendLine("Message:  ${throwable.message ?: "(no message)"}")
            appendLine("Localized: ${throwable.localizedMessage ?: "(none)"}")
            appendLine()
            appendLine("--- STACK TRACE ---")
            appendLine(sw.toString())
            appendLine("====================================================")
            appendLine("END OF REPORT")
            appendLine("====================================================")
        }
    }

    private fun writeLog(content: String, label: String) {
        val dir = directory() ?: run {
            Log.e(TAG, "Could not resolve crash log directory")
            return
        }
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        val file = File(dir, "${label}_$ts.txt")
        runCatching {
            file.writeText(content)
            Log.e(TAG, "Wrote crash log: ${file.absolutePath}")
            // Trim old logs
            logFiles().drop(MAX_LOGS).forEach { runCatching { it.delete() } }
        }.onFailure {
            Log.e(TAG, "Failed to write crash log", it)
        }
    }
}

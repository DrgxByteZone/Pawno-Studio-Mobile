package com.pawno.studio

import android.app.Application
import android.util.Log
import com.pawno.studio.data.compiler.CompilerSettingsRepository
import com.pawno.studio.data.compiler.PawnCompilerEngine
import com.pawno.studio.data.database.PawnoDatabase
import com.pawno.studio.data.includes.IncludeManager
import com.pawno.studio.data.project.ProjectManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class PawnoApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: PawnoDatabase
        private set

    lateinit var includeManager: IncludeManager
        private set

    lateinit var compilerEngine: PawnCompilerEngine
        private set

    lateinit var settingsRepository: CompilerSettingsRepository
        private set

    lateinit var projectManager: ProjectManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 1. Global crash handler to catch and record any fatal exception
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "FATAL EXCEPTION in thread: ${thread.name}", throwable)
                val crashLog = File(filesDir, "crash.log")
                crashLog.writeText(
                    "Timestamp: ${System.currentTimeMillis()}\nThread: ${thread.name}\nException:\n${throwable.stackTraceToString()}"
                )
            } catch (_: Exception) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // 2. Safe, guaranteed initialization of all core services
        try {
            database = PawnoDatabase.getInstance(this)
        } catch (e: Exception) {
            Log.e(TAG, "Database initialization warning", e)
        }

        try {
            includeManager = IncludeManager.getInstance(this)
        } catch (e: Exception) {
            Log.e(TAG, "IncludeManager initialization warning", e)
            includeManager = IncludeManager(this)
        }

        try {
            compilerEngine = PawnCompilerEngine.getInstance(this)
        } catch (e: Exception) {
            Log.e(TAG, "CompilerEngine initialization warning", e)
            compilerEngine = PawnCompilerEngine(this)
        }

        try {
            settingsRepository = CompilerSettingsRepository(this)
        } catch (e: Exception) {
            Log.e(TAG, "SettingsRepository initialization warning", e)
        }

        try {
            projectManager = ProjectManager(this)
        } catch (e: Exception) {
            Log.e(TAG, "ProjectManager initialization warning", e)
            projectManager = ProjectManager()
        }

        // 3. Extract bundled includes in background
        applicationScope.launch(Dispatchers.IO) {
            try {
                val success = includeManager.ensureBundledIncludesExtracted()
                Log.i(TAG, "Include extraction status: $success")
            } catch (e: Exception) {
                Log.e(TAG, "Background include extraction exception", e)
            }
        }
    }

    companion object {
        private const val TAG = "PawnoApp"

        lateinit var instance: PawnoApp
            private set
    }
}

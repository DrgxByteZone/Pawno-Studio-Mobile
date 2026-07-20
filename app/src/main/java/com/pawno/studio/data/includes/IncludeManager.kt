package com.pawno.studio.data.includes

import android.content.Context
import android.util.Log
import com.pawno.studio.core.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class IncludeManager(private val context: Context) {

    val baseIncludesDir: File
        get() = File(context.filesDir, Constants.DEFAULT_INCLUDES_DIR_NAME)

    val sampIncludesDir: File
        get() = File(baseIncludesDir, "samp")

    val ompIncludesDir: File
        get() = File(baseIncludesDir, "omp")

    val communityIncludesDir: File
        get() = File(baseIncludesDir, "community")

    val ysiIncludesDir: File
        get() = File(baseIncludesDir, "ysi")

    suspend fun ensureBundledIncludesExtracted(): Boolean = withContext(Dispatchers.IO) {
        val legacyForeach = File(communityIncludesDir, "foreach.inc")
        if (legacyForeach.exists()) {
            legacyForeach.delete()
        }

        val markerFile = File(baseIncludesDir, ".extracted_v${Constants.APP_VERSION}")
        if (markerFile.exists()) {
            return@withContext true
        }

        try {
            baseIncludesDir.mkdirs()
            extractAssetDirectory("includes", baseIncludesDir)
            markerFile.createNewFile()
            Log.i(TAG, "Bundled includes extracted successfully to: ${baseIncludesDir.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract bundled includes", e)
            false
        }
    }

    private fun extractAssetDirectory(assetPath: String, targetDir: File) {
        val assetManager = context.assets
        val list = assetManager.list(assetPath) ?: return

        if (list.isEmpty()) {
            // It's a file
            val targetFile = File(targetDir, File(assetPath).name)
            copyAssetFile(assetPath, targetFile)
        } else {
            // It's a directory
            targetDir.mkdirs()
            for (item in list) {
                val subAssetPath = if (assetPath.isEmpty()) item else "$assetPath/$item"
                val subList = assetManager.list(subAssetPath)
                if (subList != null && subList.isNotEmpty()) {
                    val subTargetDir = File(targetDir, item)
                    extractAssetDirectory(subAssetPath, subTargetDir)
                } else {
                    val targetFile = File(targetDir, item)
                    copyAssetFile(subAssetPath, targetFile)
                }
            }
        }
    }

    private fun copyAssetFile(assetFilePath: String, targetFile: File) {
        try {
            targetFile.parentFile?.mkdirs()
            context.assets.open(assetFilePath).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error copying asset file: $assetFilePath", e)
        }
    }

    fun getDefaultIncludePaths(): List<String> {
        val paths = mutableListOf<String>()
        if (sampIncludesDir.exists()) paths.add(sampIncludesDir.absolutePath)
        if (communityIncludesDir.exists()) paths.add(communityIncludesDir.absolutePath)
        if (ompIncludesDir.exists()) paths.add(ompIncludesDir.absolutePath)
        if (ysiIncludesDir.exists()) paths.add(ysiIncludesDir.absolutePath)
        return paths
    }

    fun getIncludeDirectories(): List<String> = getDefaultIncludePaths()

    fun getAllIncludeFiles(): List<File> {
        if (!baseIncludesDir.exists()) return emptyList()
        return baseIncludesDir.walkTopDown()
            .filter { it.isFile && it.extension.equals("inc", ignoreCase = true) }
            .toList()
    }

    fun getIncludePackages(): List<IncludePackage> = getInstalledPackages()

    fun getInstalledPackages(): List<IncludePackage> {
        val packages = mutableListOf<IncludePackage>()

        packages.add(
            IncludePackage(
                id = "samp-stdlib",
                name = "SA-MP Standard Library",
                version = "0.3.7 R3 / 0.3.DL",
                author = "SA-MP Team",
                description = "Core includes (a_samp, a_players, a_vehicles, a_objects, core, float)",
                isBundled = true,
                fileCount = sampIncludesDir.listFiles()?.count { it.extension == "inc" } ?: 12,
                relativePath = "samp"
            )
        )

        packages.add(
            IncludePackage(
                id = "community-pack",
                name = "Community Essentials",
                version = "Latest",
                author = "Incognito, Y_Less, Zeex, urandom",
                description = "Streamer, sscanf2, zcmd, Pawn.CMD, foreach, crashdetect",
                isBundled = true,
                fileCount = communityIncludesDir.listFiles()?.count { it.extension == "inc" } ?: 6,
                relativePath = "community"
            )
        )

        packages.add(
            IncludePackage(
                id = "omp-stdlib",
                name = "open.mp Standard Library",
                version = "v1.2.0",
                author = "open.mp Team",
                description = "Modern open.mp replacement headers and server natives",
                isBundled = true,
                fileCount = ompIncludesDir.walkTopDown().count { it.extension == "inc" },
                relativePath = "omp"
            )
        )

        packages.add(
            IncludePackage(
                id = "ysi-suite",
                name = "YSI Library Suite",
                version = "v5.x",
                author = "Y_Less",
                description = "Advanced modular Pawn library framework (y_iterate, y_timers, y_commands, y_hooks)",
                isBundled = true,
                fileCount = ysiIncludesDir.walkTopDown().count { it.extension == "inc" },
                relativePath = "ysi"
            )
        )

        return packages
    }

    companion object {
        private const val TAG = "IncludeManager"

        @Volatile
        private var INSTANCE: IncludeManager? = null

        fun getInstance(context: Context): IncludeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: IncludeManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

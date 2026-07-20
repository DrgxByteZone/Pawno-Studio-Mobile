package com.pawno.studio.data.project

import android.content.Context
import com.pawno.studio.PawnoApp
import com.pawno.studio.data.templates.GamemodeTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ProjectManager(private val context: Context? = null) {

    private val baseDir: File
        get() {
            val ctx = context ?: PawnoApp.instance
            return File(ctx.filesDir, "projects")
        }

    suspend fun getDefaultProject(): Project = withContext(Dispatchers.IO) {
        val defaultDir = File(baseDir, "GrandLarceny")
        defaultDir.mkdirs()
        val gmDir = File(defaultDir, "gamemodes")
        gmDir.mkdirs()
        val filterscriptsDir = File(defaultDir, "filterscripts")
        filterscriptsDir.mkdirs()
        val includeDir = File(defaultDir, "pawno/include")
        includeDir.mkdirs()

        Project(
            id = "default_grandlarc",
            name = "Grand Larceny",
            rootDir = defaultDir,
            mainGamemodeFile = File(gmDir, "grandlarc.pwn"),
            lastOpenedTimestamp = System.currentTimeMillis()
        )
    }

    suspend fun openProjectFromDirectory(dir: File): Project = withContext(Dispatchers.IO) {
        if (!dir.exists() || !dir.isDirectory) {
            return@withContext getDefaultProject()
        }

        // Auto-detect main gamemode file
        val gmDir = File(dir, "gamemodes")
        val mainCandidate = when {
            File(gmDir, "main.pwn").exists() -> File(gmDir, "main.pwn")
            File(dir, "main.pwn").exists() -> File(dir, "main.pwn")
            gmDir.exists() -> gmDir.listFiles()?.firstOrNull { it.extension.equals("pwn", ignoreCase = true) }
            else -> dir.walkTopDown().firstOrNull { it.extension.equals("pwn", ignoreCase = true) }
        }

        val totalPwnFiles = dir.walkTopDown().count { it.extension.equals("pwn", ignoreCase = true) || it.extension.equals("inc", ignoreCase = true) }

        Project(
            id = "proj_" + dir.name.hashCode().toString(),
            name = dir.name,
            rootDir = dir,
            mainGamemodeFile = mainCandidate,
            lastOpenedTimestamp = System.currentTimeMillis(),
            totalFiles = totalPwnFiles
        )
    }

    suspend fun getProjectFiles(project: Project): List<ProjectFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<ProjectFile>()
        if (project.rootDir.exists()) {
            project.rootDir.walkTopDown().filter { it.isFile && (it.extension == "pwn" || it.extension == "inc") }.forEach { f ->
                val isMain = project.mainGamemodeFile?.absolutePath == f.absolutePath
                files.add(
                    ProjectFile(
                        file = f,
                        name = f.name,
                        relativePath = f.relativeTo(project.rootDir).path,
                        absolutePath = f.absolutePath,
                        isGamemode = f.extension.equals("pwn", ignoreCase = true),
                        isInclude = f.extension.equals("inc", ignoreCase = true),
                        isMainEntrypoint = isMain
                    )
                )
            }
        }
        files
    }

    suspend fun scanDirectory(dir: File, mainEntrypointPath: String? = null): ProjectFile = withContext(Dispatchers.IO) {
        buildFileNode(dir, dir, mainEntrypointPath)
    }

    private fun buildFileNode(file: File, rootDir: File, mainEntrypointPath: String?): ProjectFile {
        val isMain = mainEntrypointPath != null && file.absolutePath == mainEntrypointPath
        val relPath = if (file == rootDir) file.name else file.relativeTo(rootDir).path

        if (!file.isDirectory) {
            return ProjectFile(
                file = file,
                name = file.name,
                relativePath = relPath,
                absolutePath = file.absolutePath,
                isGamemode = file.extension.equals("pwn", ignoreCase = true),
                isInclude = file.extension.equals("inc", ignoreCase = true),
                isMainEntrypoint = isMain
            )
        }

        val childFiles = file.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?.map { buildFileNode(it, rootDir, mainEntrypointPath) }
            ?: emptyList()

        return ProjectFile(
            file = file,
            name = file.name,
            relativePath = relPath,
            absolutePath = file.absolutePath,
            children = childFiles,
            isMainEntrypoint = isMain
        )
    }

    fun filterTree(node: ProjectFile, query: String): ProjectFile? {
        if (query.isBlank()) return node

        val matchesSelf = node.name.contains(query, ignoreCase = true)

        if (node.isDirectory) {
            val matchingChildren = node.children.mapNotNull { filterTree(it, query) }
            if (matchesSelf || matchingChildren.isNotEmpty()) {
                return node.copy(children = matchingChildren)
            }
            return null
        }

        return if (matchesSelf) node else null
    }

    suspend fun readFile(file: ProjectFile): String = withContext(Dispatchers.IO) {
        if (file.file.exists()) {
            file.file.readText()
        } else {
            ""
        }
    }

    suspend fun writeFile(file: ProjectFile, content: String): Unit = withContext(Dispatchers.IO) {
        file.file.parentFile?.mkdirs()
        file.file.writeText(content)
    }

    suspend fun createNewFile(parentDir: File, name: String, content: String = ""): ProjectFile = withContext(Dispatchers.IO) {
        parentDir.mkdirs()
        val targetFile = File(parentDir, name)
        targetFile.writeText(content)
        ProjectFile(
            file = targetFile,
            name = targetFile.name,
            relativePath = targetFile.name,
            absolutePath = targetFile.absolutePath,
            isGamemode = targetFile.extension.equals("pwn", ignoreCase = true),
            isInclude = targetFile.extension.equals("inc", ignoreCase = true)
        )
    }

    suspend fun createNewFolder(parentDir: File, name: String): File = withContext(Dispatchers.IO) {
        val newFolder = File(parentDir, name)
        newFolder.mkdirs()
        newFolder
    }

    suspend fun deleteProjectFile(file: File): Boolean = withContext(Dispatchers.IO) {
        if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    suspend fun createFromTemplate(
        project: Project,
        name: String,
        template: GamemodeTemplate
    ): ProjectFile = withContext(Dispatchers.IO) {
        val targetDir = if (name.endsWith(".inc")) {
            File(project.rootDir, "pawno/include")
        } else {
            File(project.rootDir, "gamemodes")
        }
        targetDir.mkdirs()

        val targetFile = File(targetDir, name)
        targetFile.writeText(template.starterCode)

        ProjectFile(
            file = targetFile,
            name = targetFile.name,
            relativePath = targetFile.relativeTo(project.rootDir).path,
            absolutePath = targetFile.absolutePath,
            isGamemode = targetFile.extension.equals("pwn", ignoreCase = true),
            isInclude = targetFile.extension.equals("inc", ignoreCase = true),
            isMainEntrypoint = true
        )
    }
}

package com.pawno.studio.ui.screens.ide

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pawno.studio.data.amx.AmxHeader
import com.pawno.studio.data.amx.AmxInspector
import com.pawno.studio.data.compiler.CompileOptions
import com.pawno.studio.data.compiler.CompileResult
import com.pawno.studio.data.compiler.CompilerSettingsRepository
import com.pawno.studio.data.compiler.CompilerVersion
import com.pawno.studio.data.compiler.PawnCompilerEngine
import com.pawno.studio.data.includes.IncludeManager
import com.pawno.studio.data.project.Project
import com.pawno.studio.data.project.ProjectFile
import com.pawno.studio.data.project.ProjectManager
import com.pawno.studio.data.templates.GamemodeTemplate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MainIdeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val compilerEngine = PawnCompilerEngine.getInstance(application)
    private val compilerSettingsRepo = CompilerSettingsRepository(application)
    private val projectManager = ProjectManager(application)
    private val includeManager = IncludeManager.getInstance(application)
    private val amxInspector = AmxInspector()

    private val _activeProject = MutableStateFlow<Project?>(null)
    val activeProject: StateFlow<Project?> = _activeProject.asStateFlow()

    private val _projectTree = MutableStateFlow<ProjectFile?>(null)
    val projectTree: StateFlow<ProjectFile?> = _projectTree.asStateFlow()

    private val _treeSearchQuery = MutableStateFlow("")
    val treeSearchQuery: StateFlow<String> = _treeSearchQuery.asStateFlow()

    private val _mainGamemodeFile = MutableStateFlow<ProjectFile?>(null)
    val mainGamemodeFile: StateFlow<ProjectFile?> = _mainGamemodeFile.asStateFlow()

    private val _openFiles = MutableStateFlow<List<ProjectFile>>(emptyList())
    val openFiles: StateFlow<List<ProjectFile>> = _openFiles.asStateFlow()

    private val _activeFileIndex = MutableStateFlow(0)
    val activeFileIndex: StateFlow<Int> = _activeFileIndex.asStateFlow()

    private val _activeFileContent = MutableStateFlow("")
    val activeFileContent: StateFlow<String> = _activeFileContent.asStateFlow()

    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

    private val _compileResult = MutableStateFlow<CompileResult?>(null)
    val compileResult: StateFlow<CompileResult?> = _compileResult.asStateFlow()

    private val _amxHeader = MutableStateFlow<AmxHeader?>(null)
    val amxHeader: StateFlow<AmxHeader?> = _amxHeader.asStateFlow()

    private val _cursorLine = MutableStateFlow(1)
    val cursorLine: StateFlow<Int> = _cursorLine.asStateFlow()

    private val _cursorCol = MutableStateFlow(1)
    val cursorCol: StateFlow<Int> = _cursorCol.asStateFlow()

    // Line jump event for editor
    private val _jumpToLineEvent = MutableSharedFlow<Int>()
    val jumpToLineEvent: SharedFlow<Int> = _jumpToLineEvent.asSharedFlow()

    // Insert text event for virtual keyboard
    private val _insertTextEvent = MutableSharedFlow<String>()
    val insertTextEvent: SharedFlow<String> = _insertTextEvent.asSharedFlow()

    private val _projectFiles = MutableStateFlow<List<ProjectFile>>(emptyList())
    val projectFiles: StateFlow<List<ProjectFile>> = _projectFiles.asStateFlow()

    val activeFile: ProjectFile?
        get() {
            val files = _openFiles.value
            val idx = _activeFileIndex.value
            return if (idx in files.indices) files[idx] else null
        }

    init {
        initializeWorkspace()
    }

    private fun initializeWorkspace() {
        viewModelScope.launch {
            try {
                // Check if user previously opened a project or fallback to default
                val defaultProject = projectManager.getDefaultProject()
                loadProject(defaultProject)
            } catch (e: Exception) {
                Log.e("MainIdeViewModel", "Error initializing workspace", e)
            }
        }
    }

    fun openProjectFolder(dir: File) {
        viewModelScope.launch {
            try {
                val project = projectManager.openProjectFromDirectory(dir)
                loadProject(project)
            } catch (e: Exception) {
                Log.e("MainIdeViewModel", "Error opening project directory: ${dir.absolutePath}", e)
            }
        }
    }

    private suspend fun loadProject(project: Project) {
        _activeProject.value = project

        val mainPath = project.mainGamemodeFile?.absolutePath
        val tree = projectManager.scanDirectory(project.rootDir, mainPath)
        _projectTree.value = tree

        val files = projectManager.getProjectFiles(project)
        _projectFiles.value = files

        // Set main gamemode entrypoint
        val mainFile = if (project.mainGamemodeFile != null && project.mainGamemodeFile.exists()) {
            ProjectFile(
                file = project.mainGamemodeFile,
                name = project.mainGamemodeFile.name,
                relativePath = project.mainGamemodeFile.relativeTo(project.rootDir).path,
                absolutePath = project.mainGamemodeFile.absolutePath,
                isGamemode = project.mainGamemodeFile.extension.equals("pwn", ignoreCase = true),
                isInclude = project.mainGamemodeFile.extension.equals("inc", ignoreCase = true),
                isMainEntrypoint = true
            )
        } else {
            files.firstOrNull { it.isGamemode } ?: files.firstOrNull()
        }

        _mainGamemodeFile.value = mainFile

        if (mainFile != null) {
            _openFiles.value = listOf(mainFile)
            _activeFileIndex.value = 0
            _activeFileContent.value = projectManager.readFile(mainFile)
        } else if (files.isNotEmpty()) {
            _openFiles.value = listOf(files.first())
            _activeFileIndex.value = 0
            _activeFileContent.value = projectManager.readFile(files.first())
        } else {
            // Create a starter gamemode from template
            val starterFile = projectManager.createFromTemplate(
                project = project,
                name = "grandlarc.pwn",
                template = GamemodeTemplate.RoleplayStarter
            )
            _mainGamemodeFile.value = starterFile
            _openFiles.value = listOf(starterFile)
            _activeFileIndex.value = 0
            _activeFileContent.value = projectManager.readFile(starterFile)
            _projectFiles.value = projectManager.getProjectFiles(project)
            _projectTree.value = projectManager.scanDirectory(project.rootDir, starterFile.absolutePath)
        }
    }

    fun setTreeSearchQuery(query: String) {
        _treeSearchQuery.value = query
    }

    fun setMainEntrypoint(file: ProjectFile) {
        val updated = file.copy(isMainEntrypoint = true)
        _mainGamemodeFile.value = updated

        // Update in tree
        val currentProject = _activeProject.value ?: return
        viewModelScope.launch {
            _projectTree.value = projectManager.scanDirectory(currentProject.rootDir, file.absolutePath)
        }
    }

    fun refreshProjectTree() {
        val currentProject = _activeProject.value ?: return
        viewModelScope.launch {
            val mainPath = _mainGamemodeFile.value?.absolutePath
            _projectTree.value = projectManager.scanDirectory(currentProject.rootDir, mainPath)
            _projectFiles.value = projectManager.getProjectFiles(currentProject)
        }
    }

    fun openFile(file: ProjectFile) {
        viewModelScope.launch {
            val currentList = _openFiles.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.absolutePath == file.absolutePath }
            if (existingIndex >= 0) {
                _activeFileIndex.value = existingIndex
                _activeFileContent.value = projectManager.readFile(currentList[existingIndex])
            } else {
                currentList.add(file)
                _openFiles.value = currentList
                _activeFileIndex.value = currentList.lastIndex
                _activeFileContent.value = projectManager.readFile(file)
            }
        }
    }

    fun selectTab(index: Int) {
        val files = _openFiles.value
        if (index in files.indices && index != _activeFileIndex.value) {
            saveActiveFile()
            _activeFileIndex.value = index
            viewModelScope.launch {
                _activeFileContent.value = projectManager.readFile(files[index])
            }
        }
    }

    fun closeTab(index: Int) {
        val currentList = _openFiles.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _openFiles.value = currentList
            val nextIndex = when {
                currentList.isEmpty() -> 0
                _activeFileIndex.value >= currentList.size -> currentList.size - 1
                else -> _activeFileIndex.value
            }
            _activeFileIndex.value = nextIndex
            if (currentList.isNotEmpty()) {
                viewModelScope.launch {
                    _activeFileContent.value = projectManager.readFile(currentList[nextIndex])
                }
            } else {
                _activeFileContent.value = ""
            }
        }
    }

    fun onContentChanged(newContent: String) {
        _activeFileContent.value = newContent
        activeFile?.let { it.isDirty = true }
    }

    fun updateCursor(line: Int, col: Int) {
        _cursorLine.value = line
        _cursorCol.value = col
    }

    fun saveActiveFile() {
        val file = activeFile ?: return
        viewModelScope.launch {
            projectManager.writeFile(file, _activeFileContent.value)
            file.isDirty = false
        }
    }

    fun saveAllFiles() {
        val currentFile = activeFile
        if (currentFile != null) {
            viewModelScope.launch {
                projectManager.writeFile(currentFile, _activeFileContent.value)
                currentFile.isDirty = false
            }
        }
    }

    fun compileMainGamemode(overrideVersion: CompilerVersion? = null) {
        val target = _mainGamemodeFile.value ?: activeFile ?: return
        compileSpecificFile(target, overrideVersion)
    }

    fun compileCurrentFile(overrideVersion: CompilerVersion? = null) {
        val file = activeFile ?: return
        compileSpecificFile(file, overrideVersion)
    }

    private fun compileSpecificFile(file: ProjectFile, overrideVersion: CompilerVersion? = null) {
        if (_isCompiling.value) return

        viewModelScope.launch {
            saveAllFiles()

            _isCompiling.value = true
            val currentSettings = compilerSettingsRepo.getSettings()

            val amxFile = File(file.file.parentFile, file.file.nameWithoutExtension + ".amx")

            val includeDirs = mutableListOf<String>()

            // 1. Injected Project directories FIRST
            val projectRoot = _activeProject.value?.rootDir ?: file.file.parentFile
            val candidateDirs = listOf(
                File(projectRoot, "pawno/include"),
                File(projectRoot, "include"),
                File(file.file.parentFile, "pawno/include"),
                File(file.file.parentFile, "include"),
                file.file.parentFile,
                File(file.file.parentFile, "core"),
                File(file.file.parentFile, "utils"),
                projectRoot
            )

            for (dir in candidateDirs) {
                if (dir.exists() && dir.isDirectory) {
                    includeDirs.add(dir.absolutePath)
                }
            }

            // 2. Fallback includes: If project has its own pawno/include, only add base samp to avoid collisions
            val hasProjectIncludes = candidateDirs.take(4).any { it.exists() && it.isDirectory }
            if (hasProjectIncludes) {
                if (includeManager.sampIncludesDir.exists()) {
                    includeDirs.add(includeManager.sampIncludesDir.absolutePath)
                }
            } else {
                includeDirs.addAll(includeManager.getIncludeDirectories())
            }

            val options = CompileOptions(
                sourcePath = file.absolutePath,
                outputAmxPath = amxFile.absolutePath,
                compilerVersion = overrideVersion ?: currentSettings.compilerVersion,
                includePaths = includeDirs.distinct(),
                optimizationLevel = currentSettings.optimizationLevel,
                debugLevel = currentSettings.debugLevel,
                stackReserveBytes = currentSettings.stackReserveBytes,
                compactEncoding = currentSettings.compactEncoding,
                compatibilityMode = currentSettings.compatibilityMode,
                treatWarningsAsErrors = currentSettings.treatWarningsAsErrors,
                customFlags = currentSettings.customFlags
            )

            val result = compilerEngine.compile(options)
            _compileResult.value = result
            _isCompiling.value = false

            if (result.success && amxFile.exists()) {
                inspectAmx(amxFile.absolutePath)
            }
        }
    }

    fun inspectAmx(amxPath: String) {
        viewModelScope.launch {
            val header = amxInspector.inspect(amxPath)
            _amxHeader.value = header
        }
    }

    fun jumpToLine(line: Int) {
        viewModelScope.launch {
            _jumpToLineEvent.emit(line)
        }
    }

    fun insertSymbol(symbol: String) {
        viewModelScope.launch {
            _insertTextEvent.emit(symbol)
        }
    }

    fun createNewFile(name: String, template: GamemodeTemplate = GamemodeTemplate.Blank) {
        viewModelScope.launch {
            val project = _activeProject.value ?: return@launch
            val formattedName = if (name.endsWith(".pwn") || name.endsWith(".inc")) name else "$name.pwn"
            val newFile = projectManager.createFromTemplate(project, formattedName, template)
            refreshProjectTree()
            openFile(newFile)
        }
    }

    fun createNewFileInDirectory(parentDir: File, name: String) {
        viewModelScope.launch {
            val newFile = projectManager.createNewFile(parentDir, name)
            refreshProjectTree()
            openFile(newFile)
        }
    }

    fun createNewFolderInDirectory(parentDir: File, name: String) {
        viewModelScope.launch {
            projectManager.createNewFolder(parentDir, name)
            refreshProjectTree()
        }
    }

    fun deleteFile(file: File) {
        viewModelScope.launch {
            projectManager.deleteProjectFile(file)
            // Close tab if open
            val currentList = _openFiles.value.toMutableList()
            val idx = currentList.indexOfFirst { it.absolutePath == file.absolutePath }
            if (idx >= 0) {
                closeTab(idx)
            }
            refreshProjectTree()
        }
    }

    fun getProjectFiles(): List<ProjectFile> = _projectFiles.value

    fun getIncludeFiles(): List<File> = includeManager.getAllIncludeFiles()
}

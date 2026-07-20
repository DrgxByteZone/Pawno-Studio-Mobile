package com.pawno.studio.ui.screens.ide

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawno.studio.data.project.ProjectFile
import com.pawno.studio.data.templates.GamemodeTemplate
import com.pawno.studio.editor.PawnLanguage
import com.pawno.studio.ui.components.BreadcrumbBar
import com.pawno.studio.ui.components.BuildStatusBar
import com.pawno.studio.ui.components.FileExplorerDrawer
import com.pawno.studio.ui.components.FlatButton
import com.pawno.studio.ui.components.PawnEditorView
import com.pawno.studio.ui.components.TabRowBar
import com.pawno.studio.ui.components.VirtualKeyboardStrip
import com.pawno.studio.ui.theme.AccentSuccess
import com.pawno.studio.ui.theme.AccentWarning
import com.pawno.studio.ui.theme.BgElevated
import com.pawno.studio.ui.theme.BgRoot
import com.pawno.studio.ui.theme.BgSurface
import com.pawno.studio.ui.theme.BorderColor
import com.pawno.studio.ui.theme.BorderSolid
import com.pawno.studio.ui.theme.TextPrimary
import com.pawno.studio.ui.theme.TextSecondary
import io.github.rosemoe.sora.widget.CodeEditor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainIdeScreen(
    viewModel: MainIdeViewModel,
    onOpenFolderPicker: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToAmxInspector: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToLibraries: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val openFiles by viewModel.openFiles.collectAsState()
    val activeIndex by viewModel.activeFileIndex.collectAsState()
    val fileContent by viewModel.activeFileContent.collectAsState()
    val isCompiling by viewModel.isCompiling.collectAsState()
    val compileResult by viewModel.compileResult.collectAsState()
    val cursorLine by viewModel.cursorLine.collectAsState()
    val cursorCol by viewModel.cursorCol.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()
    val projectTree by viewModel.projectTree.collectAsState()
    val treeSearchQuery by viewModel.treeSearchQuery.collectAsState()
    val mainGamemodeFile by viewModel.mainGamemodeFile.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(GamemodeTemplate.Blank) }

    // Reference to Sora CodeEditor instance for imperative operations
    var editorRef by remember { mutableStateOf<CodeEditor?>(null) }

    // Listen for jump to line events
    LaunchedEffect(Unit) {
        viewModel.jumpToLineEvent.collectLatest { line ->
            editorRef?.let { editor ->
                if (line > 0 && line <= editor.lineCount) {
                    editor.jumpToLine(line)
                }
            }
        }
    }

    // Listen for insert text events from virtual keyboard
    LaunchedEffect(Unit) {
        viewModel.insertTextEvent.collectLatest { text ->
            editorRef?.let { editor ->
                editor.text.insert(
                    editor.cursor.leftLine,
                    editor.cursor.leftColumn,
                    text
                )
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = BgSurface,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(310.dp)
                    .border(1.dp, BorderSolid)
            ) {
                FileExplorerDrawer(
                    projectName = activeProject?.name ?: "Pawn Project",
                    projectTree = projectTree,
                    searchQuery = treeSearchQuery,
                    onSearchQueryChange = { viewModel.setTreeSearchQuery(it) },
                    activeFilePath = viewModel.activeFile?.absolutePath,
                    onFileSelected = { file ->
                        viewModel.openFile(file)
                        scope.launch { drawerState.close() }
                    },
                    onSetAsMain = { file ->
                        viewModel.setMainEntrypoint(file)
                    },
                    onCompileFile = { file ->
                        viewModel.openFile(file)
                        viewModel.compileCurrentFile()
                        scope.launch { drawerState.close() }
                    },
                    onOpenFolder = {
                        onOpenFolderPicker()
                        scope.launch { drawerState.close() }
                    },
                    onRefresh = {
                        viewModel.refreshProjectTree()
                    },
                    onNewFile = {
                        showNewFileDialog = true
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgRoot)
        ) {
            // Top Toolbar - Flat 2D Minimalist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(BgSurface)
                    .border(width = 1.dp, color = BorderColor)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Explorer Toggle
                IconButton(
                    onClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Explorer",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Project / App Title & Main Target Badge
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(start = 4.dp, end = 4.dp)
                        .clickable { scope.launch { drawerState.open() } }
                ) {
                    Text(
                        text = activeProject?.name ?: "PAWNO",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    if (mainGamemodeFile != null) {
                        Text(
                            text = "TARGET: ${mainGamemodeFile?.name}",
                            color = AccentWarning,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Save Button
                IconButton(
                    onClick = { viewModel.saveActiveFile() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Diagnostics Button
                IconButton(
                    onClick = onNavigateToDiagnostics,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Diagnostics",
                        tint = if (compileResult?.hasErrors == true) Color(0xFFF85149) else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Primary Compile Button: Compiles Main Entrypoint Gamemode
                Row(
                    modifier = Modifier
                        .height(28.dp)
                        .background(
                            if (isCompiling) AccentWarning else AccentSuccess,
                            RoundedCornerShape(2.dp)
                        )
                        .clickable(enabled = !isCompiling && (mainGamemodeFile != null || viewModel.activeFile != null)) {
                            viewModel.compileMainGamemode()
                        }
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCompiling) Icons.Default.Build else Icons.Default.PlayArrow,
                        contentDescription = "Build",
                        tint = Color.Black,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isCompiling) "BUILDING" else "BUILD",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Overflow Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(BgElevated)
                            .border(1.dp, BorderColor)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "AMX Memory Inspector",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            onClick = {
                                showMenu = false
                                onNavigateToAmxInspector()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Compile Current File Only",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            onClick = {
                                showMenu = false
                                viewModel.compileCurrentFile()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Open Project Folder...",
                                    color = AccentWarning,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            onClick = {
                                showMenu = false
                                onOpenFolderPicker()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Save All Files",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            onClick = {
                                showMenu = false
                                viewModel.saveAllFiles()
                            }
                        )

                        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 4.dp))

                        DropdownMenuItem(
                            text = { Text("Include Libraries", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                            onClick = {
                                showMenu = false
                                onNavigateToLibraries()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Compiler Settings", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                            onClick = {
                                showMenu = false
                                onNavigateToSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("About Pawno Studio", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                            onClick = {
                                showMenu = false
                                onNavigateToAbout()
                            }
                        )
                    }
                }
            }

            // Tab Row Bar
            TabRowBar(
                openFiles = openFiles,
                activeIndex = activeIndex,
                onTabSelected = { viewModel.selectTab(it) },
                onTabClosed = { viewModel.closeTab(it) },
                onNewTab = { showNewFileDialog = true }
            )

            // Breadcrumb Bar
            BreadcrumbBar(
                projectName = activeProject?.name ?: "Gamemode",
                fileName = viewModel.activeFile?.name ?: "untitled.pwn",
                cursorLine = cursorLine,
                cursorCol = cursorCol,
                syntaxMode = "PAWN"
            )

            // Editor Workspace
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(BgRoot)
            ) {
                if (viewModel.activeFile != null) {
                    PawnEditorView(
                        initialContent = fileContent,
                        onContentChange = { newText ->
                            viewModel.onContentChanged(newText)
                        },
                        onCursorChange = { line, col ->
                            viewModel.updateCursor(line, col)
                        },
                        onEditorCreated = { editor ->
                            editorRef = editor
                            editor.setEditorLanguage(PawnLanguage())
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO FILE OPEN\nTAP '+' TO CREATE OR SELECT FROM EXPLORER",
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Virtual Keyboard Strip for fast Pawn scripting
            VirtualKeyboardStrip(
                onSymbolClick = { symbol ->
                    viewModel.insertSymbol(symbol)
                }
            )

            // Build Status Bar
            BuildStatusBar(
                compileResult = compileResult,
                isCompiling = isCompiling,
                onClick = onNavigateToDiagnostics
            )
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            containerColor = BgSurface,
            title = {
                Text(
                    text = "NEW PAWN SCRIPT",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        label = { Text("File Name", color = TextSecondary) },
                        placeholder = { Text("my_gamemode.pwn", color = BorderColor) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "TEMPLATE:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        GamemodeTemplate.Blank,
                        GamemodeTemplate.RoleplayStarter,
                        GamemodeTemplate.FreeroamDm,
                        GamemodeTemplate.Filterscript
                    ).forEach { template ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(
                                    if (selectedTemplate == template) BgElevated else Color.Transparent,
                                    RoundedCornerShape(2.dp)
                                )
                                .border(1.dp, if (selectedTemplate == template) Color.White else BorderColor)
                                .clickable { selectedTemplate = template }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = template.title,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            },
            confirmButton = {
                FlatButton(
                    text = "CREATE",
                    onClick = {
                        val name = if (newFileName.isBlank()) "new_script.pwn" else newFileName.trim()
                        viewModel.createNewFile(name, selectedTemplate)
                        newFileName = ""
                        showNewFileDialog = false
                    },
                    backgroundColor = AccentSuccess,
                    contentColor = Color.Black
                )
            },
            dismissButton = {
                FlatButton(
                    text = "CANCEL",
                    onClick = { showNewFileDialog = false },
                    backgroundColor = BgElevated,
                    contentColor = TextPrimary
                )
            }
        )
    }
}

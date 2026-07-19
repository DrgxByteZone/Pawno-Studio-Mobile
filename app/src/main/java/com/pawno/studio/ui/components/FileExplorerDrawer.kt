package com.pawno.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawno.studio.data.project.ProjectFile
import com.pawno.studio.ui.theme.AccentSuccess
import com.pawno.studio.ui.theme.AccentWarning
import com.pawno.studio.ui.theme.BgElevated
import com.pawno.studio.ui.theme.BgRoot
import com.pawno.studio.ui.theme.BgSurface
import com.pawno.studio.ui.theme.BorderColor
import com.pawno.studio.ui.theme.BorderSolid
import com.pawno.studio.ui.theme.SurfaceInteractive
import com.pawno.studio.ui.theme.SurfacePanel
import com.pawno.studio.ui.theme.TextMuted
import com.pawno.studio.ui.theme.TextPrimary
import com.pawno.studio.ui.theme.TextSecondary
import java.io.File

/**
 * Enterprise Hierarchical Tree-View File Explorer Drawer.
 * Engineered for massive modular SA-MP gamemodes (50+ sub-directories).
 * Swiss 2D Flat Minimalist Discipline (Zero gradients, zero 3D, zero neon).
 * Author: By M.B.A & AXEL - Blackpanther Company
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileExplorerDrawer(
    projectName: String,
    projectTree: ProjectFile?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    activeFilePath: String?,
    onFileSelected: (ProjectFile) -> Unit,
    onSetAsMain: (ProjectFile) -> Unit,
    onCompileFile: (ProjectFile) -> Unit,
    onOpenFolder: () -> Unit,
    onRefresh: () -> Unit,
    onNewFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Keep track of expanded folder states
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(SurfacePanel)
            .border(width = 1.dp, color = BorderSolid)
            .padding(8.dp)
    ) {
        // 1. Drawer Header & Action Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PROJECT EXPLORER",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )
                Text(
                    text = projectName,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = AccentWarning,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Open Folder Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(SurfaceInteractive, RoundedCornerShape(2.dp))
                        .border(1.dp, BorderSolid, RoundedCornerShape(2.dp))
                        .clickable(onClick = onOpenFolder),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Open Folder",
                        tint = TextPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Refresh Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(SurfaceInteractive, RoundedCornerShape(2.dp))
                        .border(1.dp, BorderSolid, RoundedCornerShape(2.dp))
                        .clickable(onClick = onRefresh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = TextPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // New File Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(SurfaceInteractive, RoundedCornerShape(2.dp))
                        .border(1.dp, BorderSolid, RoundedCornerShape(2.dp))
                        .clickable(onClick = onNewFile),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New File",
                        tint = TextPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Search Filter Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            placeholder = {
                Text(
                    text = "Filter files (e.g. account, main)...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onSearchQueryChange("") }
                    )
                }
            },
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TextPrimary
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BgRoot,
                unfocusedContainerColor = BgRoot,
                focusedBorderColor = AccentWarning,
                unfocusedBorderColor = BorderSolid
            ),
            shape = RoundedCornerShape(2.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 1.dp, color = BorderSolid)
        Spacer(modifier = Modifier.height(4.dp))

        // 3. Tree Hierarchy Content
        if (projectTree == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No project folder loaded.\nTap folder icon above to open.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
            ) {
                renderTreeNode(
                    node = projectTree,
                    depth = 0,
                    searchQuery = searchQuery,
                    expandedStates = expandedStates,
                    activeFilePath = activeFilePath,
                    onFileSelected = onFileSelected,
                    onSetAsMain = onSetAsMain,
                    onCompileFile = onCompileFile
                )
            }
        }
    }
}

/**
 * Recursive Tree Node Renderer supporting infinite folder nesting
 */
@OptIn(ExperimentalFoundationApi::class)
private fun androidx.compose.foundation.lazy.LazyListScope.renderTreeNode(
    node: ProjectFile,
    depth: Int,
    searchQuery: String,
    expandedStates: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Boolean>,
    activeFilePath: String?,
    onFileSelected: (ProjectFile) -> Unit,
    onSetAsMain: (ProjectFile) -> Unit,
    onCompileFile: (ProjectFile) -> Unit
) {
    val isMatch = searchQuery.isBlank() || node.name.contains(searchQuery, ignoreCase = true)

    if (node.isDirectory) {
        // By default, expand the root or folders with search matches
        val isExpanded = expandedStates[node.absolutePath] ?: (depth == 0 || searchQuery.isNotBlank())

        item(key = node.absolutePath) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedStates[node.absolutePath] = !isExpanded
                    }
                    .padding(start = (depth * 14).dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = "Expand",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                    contentDescription = "Folder",
                    tint = if (depth == 0) AccentWarning else Color(0xFFE3B341),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = node.name,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (depth == 0) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isExpanded) {
            node.children.forEach { child ->
                renderTreeNode(
                    node = child,
                    depth = depth + 1,
                    searchQuery = searchQuery,
                    expandedStates = expandedStates,
                    activeFilePath = activeFilePath,
                    onFileSelected = onFileSelected,
                    onSetAsMain = onSetAsMain,
                    onCompileFile = onCompileFile
                )
            }
        }
    } else {
        // Render File Row
        if (isMatch) {
            item(key = node.absolutePath) {
                var showContextMenu by remember { mutableStateOf(false) }
                val isSelected = activeFilePath == node.absolutePath

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) BgElevated else Color.Transparent)
                            .combinedClickable(
                                onClick = { onFileSelected(node) },
                                onLongClick = { showContextMenu = true }
                            )
                            .padding(start = (depth * 14 + 18).dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            // File Type Icon
                            val iconColor = when {
                                node.isPawnSource -> AccentWarning // Orange for .pwn
                                node.isPawnInclude -> Color(0xFF58A6FF) // Blue for .inc
                                node.isAmxBinary -> AccentSuccess // Green for .amx
                                else -> TextSecondary
                            }

                            Icon(
                                imageVector = if (node.isPawnSource || node.isPawnInclude) Icons.Default.Code else Icons.Default.Description,
                                contentDescription = "File",
                                tint = iconColor,
                                modifier = Modifier.size(14.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = node.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Badges: [MAIN] or [PWN] or [INC]
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (node.isMainEntrypoint) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF238636), RoundedCornerShape(2.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "MAIN",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        color = Color.White
                                    )
                                }
                            } else if (node.isPawnSource) {
                                Text(
                                    text = "pwn",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = AccentWarning
                                )
                            } else if (node.isPawnInclude) {
                                Text(
                                    text = "inc",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = Color(0xFF58A6FF)
                                )
                            }
                        }
                    }

                    // Context Menu for File Operations
                    DropdownMenu(
                        expanded = showContextMenu,
                        onDismissRequest = { showContextMenu = false },
                        modifier = Modifier
                            .background(BgSurface)
                            .border(1.dp, BorderSolid)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = null,
                                        tint = AccentWarning,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Set as Main Entrypoint",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                }
                            },
                            onClick = {
                                showContextMenu = false
                                onSetAsMain(node)
                            }
                        )

                        if (node.isPawnSource) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = AccentSuccess,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Compile This File Only",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = TextPrimary
                                        )
                                    }
                                },
                                onClick = {
                                    showContextMenu = false
                                    onCompileFile(node)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.pawno.studio.ui.screens.diagnostics

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawno.studio.data.compiler.CompileResult
import com.pawno.studio.data.compiler.DiagnosticItem
import com.pawno.studio.data.compiler.DiagnosticSeverity
import com.pawno.studio.ui.components.FlatButton
import com.pawno.studio.ui.components.FlatCard
import com.pawno.studio.ui.components.FlatChip
import com.pawno.studio.ui.theme.AccentError
import com.pawno.studio.ui.theme.AccentSuccess
import com.pawno.studio.ui.theme.AccentWarning
import com.pawno.studio.ui.theme.BgElevated
import com.pawno.studio.ui.theme.BgRoot
import com.pawno.studio.ui.theme.BgSurface
import com.pawno.studio.ui.theme.BorderColor
import com.pawno.studio.ui.theme.TextPrimary
import com.pawno.studio.ui.theme.TextSecondary
import java.io.File

enum class DiagnosticFilter {
    ALL,
    ERRORS,
    WARNINGS
}

@Composable
fun DiagnosticsScreen(
    compileResult: CompileResult?,
    onNavigateBack: () -> Unit,
    onJumpToLine: (Int) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(DiagnosticFilter.ALL) }
    var showRawOutput by remember { mutableStateOf(false) }

    val diagnostics = compileResult?.diagnostics ?: emptyList()
    val errorCount = diagnostics.count { it.severity.isError }
    val warningCount = diagnostics.count { it.severity == DiagnosticSeverity.WARNING }

    val filteredList = when (selectedFilter) {
        DiagnosticFilter.ALL -> diagnostics
        DiagnosticFilter.ERRORS -> diagnostics.filter { it.severity.isError }
        DiagnosticFilter.WARNINGS -> diagnostics.filter { it.severity == DiagnosticSeverity.WARNING }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgRoot)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(BgSurface)
                .border(1.dp, BorderColor)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "COMPILER DIAGNOSTICS",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = compileResult?.let { "Target: ${File(it.outputAmxPath ?: "build").name}" } ?: "No active build",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (errorCount > 0) BgElevated else BgSurface)
                .border(1.dp, if (errorCount > 0) AccentError else AccentSuccess)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (errorCount > 0) Icons.Default.Error else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (errorCount > 0) AccentError else AccentSuccess,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (compileResult == null) {
                            "NO BUILD EXECUTED YET"
                        } else if (compileResult.success) {
                            "BUILD SUCCEEDED (${compileResult.elapsedMs}ms)"
                        } else {
                            "BUILD FAILED - $errorCount ERROR(S), $warningCount WARNING(S)"
                        },
                        color = if (errorCount > 0) AccentError else AccentSuccess,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = compileResult?.let { "Compiler: Pawn ${it.compilerVersion.displayName} • ${it.headerSizeBytes} B header" } ?: "Execute compile (F5) to generate diagnostics",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSurface)
                .border(1.dp, BorderColor)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FlatChip(
                text = "ALL (${diagnostics.size})",
                selected = selectedFilter == DiagnosticFilter.ALL,
                onClick = { selectedFilter = DiagnosticFilter.ALL }
            )
            FlatChip(
                text = "ERRORS ($errorCount)",
                selected = selectedFilter == DiagnosticFilter.ERRORS,
                onClick = { selectedFilter = DiagnosticFilter.ERRORS },
                color = AccentError
            )
            FlatChip(
                text = "WARNINGS ($warningCount)",
                selected = selectedFilter == DiagnosticFilter.WARNINGS,
                onClick = { selectedFilter = DiagnosticFilter.WARNINGS },
                color = AccentWarning
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = if (showRawOutput) "[HIDE LOG]" else "[RAW LOG]",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .clickable { showRawOutput = !showRawOutput }
                    .padding(vertical = 4.dp)
            )
        }

        // Content Area
        if (showRawOutput) {
            // Raw Compiler Output Monospace Log
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .background(BgSurface)
                    .border(1.dp, BorderColor)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RAW COMPILER CONSOLE OUTPUT:",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("pawn_log", compileResult?.rawOutput ?: ""))
                            Toast.makeText(context, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Log",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = compileResult?.rawOutput?.ifBlank { "No output captured." } ?: "No output.",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            // Diagnostics List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (compileResult == null) "NO DIAGNOSTICS AVAILABLE" else "NO ISSUES MATCH CURRENT FILTER",
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList) { item ->
                        DiagnosticItemCard(
                            item = item,
                            onJump = {
                                onJumpToLine(item.lineNumber)
                                onNavigateBack()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticItemCard(
    item: DiagnosticItem,
    onJump: () -> Unit
) {
    val isError = item.severity.isError
    val severityColor = if (isError) AccentError else AccentWarning
    val severityTitle = if (isError) "ERROR" else "WARNING"

    FlatCard(
        borderColor = BorderColor,
        backgroundColor = BgSurface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Severity & Code Badge + Jump Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(BgElevated)
                            .border(1.dp, severityColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$severityTitle ${item.errorCode}",
                            color = severityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Line ${item.lineNumber}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Jump Button
                FlatButton(
                    text = "JUMP ->",
                    onClick = onJump,
                    backgroundColor = BgElevated,
                    contentColor = Color.White,
                    modifier = Modifier.height(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Message
            Text(
                text = item.message,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )

            // File path
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${item.fileName}:${item.lineNumber}",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            // Code Snippet Preview (if available)
            if (item.sourceSnippet != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgRoot)
                        .border(1.dp, BorderColor)
                        .padding(8.dp)
                ) {
                    Text(
                        text = item.sourceSnippet,
                        color = Color(0xFFE6EDF3),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Suggestion / Fix (if available)
            if (item.suggestion != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "💡 ${item.suggestion}",
                    color = AccentWarning,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

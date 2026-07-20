package com.pawno.studio.ui.screens.amx

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawno.studio.data.amx.AmxHeader
import com.pawno.studio.data.amx.AmxSymbol
import com.pawno.studio.ui.components.FlatCard
import com.pawno.studio.ui.components.FlatChip
import com.pawno.studio.ui.theme.AccentPrimary
import com.pawno.studio.ui.theme.AccentSuccess
import com.pawno.studio.ui.theme.BgElevated
import com.pawno.studio.ui.theme.BgRoot
import com.pawno.studio.ui.theme.BgSurface
import com.pawno.studio.ui.theme.BorderColor
import com.pawno.studio.ui.theme.TextPrimary
import com.pawno.studio.ui.theme.TextSecondary
import java.io.File

enum class SymbolTab {
    PUBLICS,
    NATIVES
}

@Composable
fun AmxInspectorScreen(
    amxHeader: AmxHeader?,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(SymbolTab.PUBLICS) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgRoot)
    ) {
        // Header Bar
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
                    text = "AMX MEMORY INSPECTOR",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = amxHeader?.let { "binary: ${File(it.filePath).name}" } ?: "No .amx binary loaded",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (amxHeader == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO AMX BINARY LOADED\nCOMPILE YOUR SCRIPT (F5) TO INSPECT MEMORY LAYOUT",
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 20.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 2x2 Memory Layout Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MemoryStatCard(
                                title = "CODE SEGMENT",
                                primaryValue = formatBytes(amxHeader.codeSize),
                                secondaryValue = "${amxHeader.codeSize / 4} OpCodes",
                                subtitle = "EXECUTION BYTECODE",
                                modifier = Modifier.weight(1f)
                            )
                            MemoryStatCard(
                                title = "DATA SEGMENT",
                                primaryValue = formatBytes(amxHeader.dataSize),
                                secondaryValue = "${amxHeader.dataSize / 4} Cells (32-bit)",
                                subtitle = "STATIC & GLOBAL ARRAYS",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MemoryStatCard(
                                title = "HEAP / STACK",
                                primaryValue = formatBytes(amxHeader.heapStackReserve),
                                secondaryValue = "Dynamic Depth",
                                subtitle = "RUNTIME CALL STACK",
                                modifier = Modifier.weight(1f)
                            )
                            MemoryStatCard(
                                title = "SYMBOLS",
                                primaryValue = "${amxHeader.publics.size} Publics",
                                secondaryValue = "${amxHeader.natives.size} Natives",
                                subtitle = "SA-MP INTEROP TABLE",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Dynamic Memory Budget Bar
                item {
                    FlatCard(
                        borderColor = BorderColor,
                        backgroundColor = BgSurface
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "MEMORY BUDGET UTILIZATION",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "MAGIC: 0x${amxHeader.magic.toString(16).uppercase()}",
                                    color = AccentSuccess,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Segmented Bar
                            val totalBudget = (amxHeader.codeSize + amxHeader.dataSize + amxHeader.heapStackReserve).coerceAtLeast(1L)
                            val codeFraction = (amxHeader.codeSize.toFloat() / totalBudget).coerceIn(0.02f, 0.9f)
                            val dataFraction = (amxHeader.dataSize.toFloat() / totalBudget).coerceIn(0.02f, 0.9f)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .background(BgElevated)
                                    .border(1.dp, BorderColor)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(codeFraction)
                                        .fillMaxSize()
                                        .background(AccentPrimary)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(dataFraction)
                                        .fillMaxSize()
                                        .background(AccentSuccess)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight((1f - codeFraction - dataFraction).coerceAtLeast(0.05f))
                                        .fillMaxSize()
                                        .background(BgElevated)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(AccentPrimary))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Code", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(modifier = Modifier.size(8.dp).background(AccentSuccess))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Data", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(modifier = Modifier.size(8.dp).background(BorderColor))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Available Stack", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }

                                Text(
                                    text = "STABLE",
                                    color = AccentSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Symbol Browser Section
                item {
                    // Tab Selector & Search
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgSurface)
                                .border(1.dp, BorderColor)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FlatChip(
                                text = "PUBLICS (${amxHeader.publics.size})",
                                selected = selectedTab == SymbolTab.PUBLICS,
                                onClick = { selectedTab = SymbolTab.PUBLICS }
                            )
                            FlatChip(
                                text = "NATIVES (${amxHeader.natives.size})",
                                selected = selectedTab == SymbolTab.NATIVES,
                                onClick = { selectedTab = SymbolTab.NATIVES }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search symbol name...", color = BorderColor, fontSize = 12.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = BgSurface,
                                unfocusedContainerColor = BgSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )
                    }
                }

                // Symbols List
                val symbols = if (selectedTab == SymbolTab.PUBLICS) amxHeader.publics else amxHeader.natives
                val filteredSymbols = if (searchQuery.isBlank()) {
                    symbols
                } else {
                    symbols.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }

                items(filteredSymbols) { symbol ->
                    SymbolItemRow(symbol = symbol, isPublic = selectedTab == SymbolTab.PUBLICS)
                }
            }
        }
    }
}

@Composable
fun MemoryStatCard(
    title: String,
    primaryValue: String,
    secondaryValue: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    FlatCard(
        borderColor = BorderColor,
        backgroundColor = BgSurface,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = primaryValue,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = secondaryValue,
                color = AccentPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun SymbolItemRow(
    symbol: AmxSymbol,
    isPublic: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSurface)
            .border(1.dp, BorderColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = symbol.name,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = if (isPublic) "public callback" else "native function",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Box(
            modifier = Modifier
                .background(BgElevated)
                .border(1.dp, BorderColor)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "0x" + symbol.address.toString(16).padStart(8, '0').uppercase(),
                color = if (isPublic) AccentSuccess else AccentPrimary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes.toDouble() / (1024 * 1024))
        bytes >= 1024 -> String.format("%.1f KB", bytes.toDouble() / 1024)
        else -> "$bytes B"
    }
}

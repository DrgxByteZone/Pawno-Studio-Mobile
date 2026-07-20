package com.pawno.studio.ui.screens.settings

import android.app.Application
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawno.studio.data.compiler.CompilerSettings
import com.pawno.studio.data.compiler.CompilerSettingsRepository
import com.pawno.studio.data.compiler.CompilerVersion
import com.pawno.studio.ui.components.FlatCard
import com.pawno.studio.ui.components.FlatChip
import com.pawno.studio.ui.theme.AccentSuccess
import com.pawno.studio.ui.theme.BgElevated
import com.pawno.studio.ui.theme.BgRoot
import com.pawno.studio.ui.theme.BgSurface
import com.pawno.studio.ui.theme.BorderColor
import com.pawno.studio.ui.theme.TextPrimary
import com.pawno.studio.ui.theme.TextSecondary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = CompilerSettingsRepository(application)
    val settings = repo.settingsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CompilerSettings()
    )

    fun updateSettings(newSettings: CompilerSettings) {
        viewModelScope.launch {
            repo.saveSettings(newSettings)
        }
    }
}

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val currentSettings by viewModel.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgRoot)
    ) {
        // Top Bar
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

            Text(
                text = "COMPILER & IDE SETTINGS",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Compiler Engine Version
            item {
                FlatCard(
                    borderColor = BorderColor,
                    backgroundColor = BgSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "COMPILER VERSION",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        listOf(
                            CompilerVersion.PAWN_3_10_11 to "Pawn 3.10.11 (Zeex) - Recommended",
                            CompilerVersion.PAWN_3_10_7 to "Pawn 3.10.7 (Legacy SA-MP 0.3.7)"
                        ).forEach { (version, label) ->
                            val isSelected = currentSettings.compilerVersion == version
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(if (isSelected) BgElevated else Color.Transparent)
                                    .border(1.dp, if (isSelected) Color.White else BorderColor)
                                    .clickable {
                                        viewModel.updateSettings(currentSettings.copy(compilerVersionId = version.id))
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = AccentSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Optimization Level
            item {
                FlatCard(
                    borderColor = BorderColor,
                    backgroundColor = BgSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "OPTIMIZATION LEVEL (-O)",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                0 to "-O0 None",
                                1 to "-O1 Standard",
                                2 to "-O2 Aggressive"
                            ).forEach { (opt, title) ->
                                FlatChip(
                                    text = title,
                                    selected = currentSettings.optimizationLevel == opt,
                                    onClick = {
                                        viewModel.updateSettings(currentSettings.copy(optimizationLevel = opt))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Debug Level
            item {
                FlatCard(
                    borderColor = BorderColor,
                    backgroundColor = BgSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DEBUG SYMBOLS (-d)",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                0 to "-d0 None",
                                1 to "-d1 Line",
                                2 to "-d2 Full",
                                3 to "-d3 Max"
                            ).forEach { (dbg, title) ->
                                FlatChip(
                                    text = title,
                                    selected = currentSettings.debugLevel == dbg,
                                    onClick = {
                                        viewModel.updateSettings(currentSettings.copy(debugLevel = dbg))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Flags & Switches
            item {
                FlatCard(
                    borderColor = BorderColor,
                    backgroundColor = BgSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "COMPILER FLAGS",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        SettingSwitchRow(
                            title = "Compact Bytecode (-C)",
                            description = "Reduces output AMX binary size",
                            checked = currentSettings.compactEncoding,
                            onCheckedChange = {
                                viewModel.updateSettings(currentSettings.copy(compactEncoding = it))
                            }
                        )

                        SettingSwitchRow(
                            title = "Compatibility Mode (-Z)",
                            description = "Enables legacy syntax compatibility",
                            checked = currentSettings.compatibilityMode,
                            onCheckedChange = {
                                viewModel.updateSettings(currentSettings.copy(compatibilityMode = it))
                            }
                        )

                        SettingSwitchRow(
                            title = "Treat Warnings as Errors",
                            description = "Halts compilation when any warning occurs",
                            checked = currentSettings.treatWarningsAsErrors,
                            onCheckedChange = {
                                viewModel.updateSettings(currentSettings.copy(treatWarningsAsErrors = it))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentSuccess,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = BgElevated
            )
        )
    }
}

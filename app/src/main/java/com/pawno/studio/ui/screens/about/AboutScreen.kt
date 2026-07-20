package com.pawno.studio.ui.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawno.studio.ui.components.BlackpantherLogoView
import com.pawno.studio.ui.components.FlatCard
import com.pawno.studio.ui.theme.AccentPrimary
import com.pawno.studio.ui.theme.AccentSuccess
import com.pawno.studio.ui.theme.BgElevated
import com.pawno.studio.ui.theme.BgRoot
import com.pawno.studio.ui.theme.BgSurface
import com.pawno.studio.ui.theme.BorderColor
import com.pawno.studio.ui.theme.TextPrimary
import com.pawno.studio.ui.theme.TextSecondary

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
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
                text = "ABOUT PAWNO STUDIO",
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
            // Header Card with 2D Minimalist Panther Logo
            item {
                FlatCard(
                    borderColor = BorderColor,
                    backgroundColor = BgSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BlackpantherLogoView(
                            size = 80.dp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "PAWNO STUDIO MOBILE",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Version 1.0.0 (Native ARM64/ARMv7/x86_64)",
                            color = AccentSuccess,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Professional Grand Theft Auto: San Andreas Multiplayer (SA-MP) & open.mp Pawn IDE for Android",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Explicit Required Credits: By M.B.A & AXEL
            item {
                FlatCard(
                    borderColor = Color.White,
                    backgroundColor = BgSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "AUTHOR & CREDITS",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgElevated)
                                .border(1.dp, Color.White)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "By M.B.A & AXEL",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Blackpanther Company",
                                    color = AccentPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Dirancang dan dibangun untuk menghadirkan pengalaman pengembangan Pawn serta kompilasi gamemode SA-MP & open.mp terbaik di Android.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Conceptual Reference
            item {
                FlatCard(
                    borderColor = BorderColor,
                    backgroundColor = BgSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "ACKNOWLEDGEMENT",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Referensi konsep diambil dari Pawn-MC (novusr).",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // System Specifications
            item {
                FlatCard(
                    borderColor = BorderColor,
                    backgroundColor = BgSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "SYSTEM ARCHITECTURE",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SpecRow("Native Compiler Engine", "Pawn 3.10.11 & 3.10.7")
                        SpecRow("Stack Allocation", "16 MB Dedicated Thread Stack")
                        SpecRow("Buffer Sizing", "2 MB Structured Output Capture")
                        SpecRow("Gamemode Capacity", "50,000 - 150,000+ Lines")
                        SpecRow("Bundled Headers", "433 Authentic .inc Files")
                        SpecRow("AMX Inspector", "32-bit Memory Layout Analyzer")
                        SpecRow("Editor Engine", "Sora Editor (Virtualized Canvas)")
                        SpecRow("Design System", "Flat 2D Minimalist (0 Gradients)")
                    }
                }
            }

            // Open Source License Disclosures
            item {
                FlatCard(
                    borderColor = BorderColor,
                    backgroundColor = BgSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "OPEN SOURCE LICENSES",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LicenseRow("Pawn Compiler (pawnc)", "Zeex / ITB CompuPhase / SA-MP Team", "Zlib / Apache 2.0")
                        LicenseRow("open.mp Standard Library", "open.mp Team", "MPL-2.0")
                        LicenseRow("YSI Library Suite", "Y_Less & Contributors", "GPL-3.0")
                        LicenseRow("Sora Code Editor", "Rosemoe", "LGPL-2.1")
                        LicenseRow("Android Jetpack & Compose", "Google LLC", "Apache 2.0")
                    }
                }
            }
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun LicenseRow(name: String, author: String, license: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = license,
                color = AccentSuccess,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = author,
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}

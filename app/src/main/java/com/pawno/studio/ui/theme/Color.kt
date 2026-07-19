package com.pawno.studio.ui.theme

import androidx.compose.ui.graphics.Color

// Stitch Engineered Precision: 2D Flat Minimalist Design System
// ZERO gradients, ZERO 3D effects, ZERO neon luminescence.
val SurfaceBase = Color(0xFF0E1117)
val SurfacePanel = Color(0xFF161B22)
val SurfaceInteractive = Color(0xFF21262D)
val SurfaceContainerHigh = Color(0xFF272A31)
val SurfaceContainerHighest = Color(0xFF32353C)

// Borders (1px solid)
val BorderSolid = Color(0xFF30363D)
val BorderFocus = Color(0xFF58A6FF)

// Actions
val ActionPrimary = Color(0xFF238636)       // RUN / Compile trigger
val ActionPrimaryHover = Color(0xFF2EA043)
val ActionSecondary = Color(0xFF1F6FEB)     // System action
val ActionSecondaryHover = Color(0xFF388BFD)

// Status
val StatusError = Color(0xFFDA3633)
val StatusErrorContainer = Color(0xFF93000A)
val StatusWarning = Color(0xFFD29922)
val StatusWarningContainer = Color(0xFF1377CD)
val StatusSuccess = Color(0xFF2EA043)

// Text Tiers
val TextPrimary = Color(0xFFF0F6FC)
val TextSecondary = Color(0xFF8B949E)
val TextMuted = Color(0xFF6E7681)
val TextOnAction = Color(0xFFF9FFF3)

// Canonical Aliases
val BgRoot = SurfaceBase
val BgSurface = SurfacePanel
val BgElevated = SurfaceInteractive
val BorderColor = BorderSolid
val BorderSubtle = TextMuted
val AccentPrimary = ActionSecondary
val AccentSuccess = StatusSuccess
val AccentWarning = StatusWarning
val AccentError = StatusError

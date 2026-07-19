package com.pawno.studio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ActionPrimary,
    onPrimary = TextOnAction,
    primaryContainer = ActionPrimary,
    onPrimaryContainer = TextOnAction,
    secondary = ActionSecondary,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceInteractive,
    onSecondaryContainer = TextPrimary,
    background = SurfaceBase,
    onBackground = TextPrimary,
    surface = SurfaceBase,
    onSurface = TextPrimary,
    surfaceVariant = SurfacePanel,
    onSurfaceVariant = TextSecondary,
    outline = BorderSolid,
    outlineVariant = BorderSolid,
    error = StatusError,
    onError = TextPrimary,
    errorContainer = StatusErrorContainer,
    onErrorContainer = TextPrimary
)

@Composable
fun PawnoStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = PawnoTypography,
        content = content
    )
}

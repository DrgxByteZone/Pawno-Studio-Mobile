package com.pawno.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawno.studio.ui.theme.ActionPrimary
import com.pawno.studio.ui.theme.BorderSolid
import com.pawno.studio.ui.theme.SurfaceInteractive
import com.pawno.studio.ui.theme.TextOnAction
import com.pawno.studio.ui.theme.TextPrimary

enum class FlatButtonVariant {
    PRIMARY,    // Solid Green #238636 (Run / Compile)
    SECONDARY,  // Solid Blue #1F6FEB (Tools / Actions)
    GHOST       // Panel Gray with 1px border
}

@Composable
fun FlatButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: FlatButtonVariant = FlatButtonVariant.PRIMARY,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    val bgColor = backgroundColor ?: when (variant) {
        FlatButtonVariant.PRIMARY -> ActionPrimary
        FlatButtonVariant.SECONDARY -> Color(0xFF1F6FEB)
        FlatButtonVariant.GHOST -> SurfaceInteractive
    }

    val textColor = contentColor ?: when (variant) {
        FlatButtonVariant.PRIMARY -> TextOnAction
        FlatButtonVariant.SECONDARY -> TextPrimary
        FlatButtonVariant.GHOST -> TextPrimary
    }

    val borderModifier = if (variant == FlatButtonVariant.GHOST) {
        Modifier.border(width = 1.dp, color = BorderSolid, shape = RoundedCornerShape(4.dp))
    } else Modifier

    Box(
        modifier = modifier
            .height(36.dp)
            .then(borderModifier)
            .background(if (enabled) bgColor else bgColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = textColor
            )
        }
    }
}

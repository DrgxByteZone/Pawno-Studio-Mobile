package com.pawno.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawno.studio.ui.theme.BorderSolid
import com.pawno.studio.ui.theme.SurfaceInteractive
import com.pawno.studio.ui.theme.TextPrimary

@Composable
fun FlatChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    color: Color? = null,
    backgroundColor: Color = if (selected) (color ?: Color(0xFF58A6FF)).copy(alpha = 0.25f) else SurfaceInteractive,
    textColor: Color = if (selected) (color ?: Color.White) else TextPrimary,
    borderColor: Color = if (selected) (color ?: Color.White) else BorderSolid,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
            .then(clickModifier)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

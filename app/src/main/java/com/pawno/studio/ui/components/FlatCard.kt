package com.pawno.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pawno.studio.ui.theme.BorderSolid
import com.pawno.studio.ui.theme.SurfacePanel

@Composable
fun FlatCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfacePanel,
    borderColor: Color = BorderSolid,
    cornerRadius: Dp = 4.dp,
    padding: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(cornerRadius))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(cornerRadius))
            .padding(padding),
        content = content
    )
}

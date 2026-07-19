package com.pawno.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawno.studio.ui.theme.ActionPrimary
import com.pawno.studio.ui.theme.BorderSolid
import com.pawno.studio.ui.theme.SurfaceContainerHigh
import com.pawno.studio.ui.theme.SurfacePanel
import com.pawno.studio.ui.theme.TextPrimary

private val QUICK_SYMBOLS = listOf(
    "{", "}", "(", ")", "[", "]", ";", ":", "\"", "'",
    "_", "&", "!", "|", "<", ">", "=", "+", "-", "TAB"
)

@Composable
fun VirtualKeyboardStrip(
    modifier: Modifier = Modifier,
    onSymbolClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfacePanel)
            .border(width = 1.dp, color = BorderSolid)
            .padding(horizontal = 6.dp, vertical = 5.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QUICK_SYMBOLS.forEach { symbol ->
            val isTab = symbol == "TAB"
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .widthIn(min = if (isTab) 44.dp else 32.dp)
                    .background(SurfaceContainerHigh, RoundedCornerShape(4.dp))
                    .border(width = 1.dp, color = BorderSolid, RoundedCornerShape(4.dp))
                    .clickable {
                        if (isTab) {
                            onSymbolClick("    ") // 4 spaces
                        } else {
                            onSymbolClick(symbol)
                        }
                    }
                    .padding(horizontal = if (isTab) 8.dp else 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isTab) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = if (isTab) 11.sp else 13.sp,
                    color = if (isTab) ActionPrimary else TextPrimary
                )
            }
        }
    }
}

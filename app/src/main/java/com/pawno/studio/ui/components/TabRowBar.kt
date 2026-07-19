package com.pawno.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawno.studio.data.project.ProjectFile
import com.pawno.studio.ui.theme.ActionPrimary
import com.pawno.studio.ui.theme.BorderFocus
import com.pawno.studio.ui.theme.BorderSolid
import com.pawno.studio.ui.theme.SurfaceBase
import com.pawno.studio.ui.theme.SurfacePanel
import com.pawno.studio.ui.theme.TextPrimary
import com.pawno.studio.ui.theme.TextSecondary
import java.io.File

@Composable
fun TabRowBar(
    openFiles: List<ProjectFile>,
    activeIndex: Int = 0,
    activeFileIndex: Int = activeIndex,
    onTabSelected: (Int) -> Unit,
    onTabClosed: (Int) -> Unit,
    onNewTab: () -> Unit = {},
    onNewTabClick: () -> Unit = onNewTab,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val activeIdx = if (activeIndex != 0) activeIndex else activeFileIndex

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(SurfacePanel)
            .border(width = 1.dp, color = BorderSolid)
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically
    ) {
        openFiles.forEachIndexed { index, file ->
            val isActive = index == activeIdx

            val tabBg = if (isActive) SurfaceBase else SurfacePanel
            val textColor = if (isActive) TextPrimary else TextSecondary
            val borderModifier = if (isActive) {
                Modifier.border(width = 1.dp, color = BorderFocus)
            } else Modifier

            Row(
                modifier = Modifier
                    .height(36.dp)
                    .background(tabBg)
                    .then(borderModifier)
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = if (isActive) ActionPrimary else TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = file.name + if (file.isDirty) " *" else "",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onTabClosed(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close tab",
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // New Tab (+) Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clickable { onNewTabClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Tab",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

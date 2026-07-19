package com.pawno.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawno.studio.data.compiler.CompileResult
import com.pawno.studio.ui.theme.ActionPrimary
import com.pawno.studio.ui.theme.BorderSolid
import com.pawno.studio.ui.theme.StatusError
import com.pawno.studio.ui.theme.SurfacePanel
import com.pawno.studio.ui.theme.TextMuted
import com.pawno.studio.ui.theme.TextSecondary

@Composable
fun BuildStatusBar(
    compileResult: CompileResult?,
    isCompiling: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        isCompiling -> ActionPrimary
        compileResult == null -> TextMuted
        compileResult.isSuccess -> ActionPrimary
        else -> StatusError
    }

    val titleText = when {
        isCompiling -> "Compiling pawn script..."
        compileResult == null -> "Ready to compile"
        compileResult.isSuccess -> "Build Succeeded"
        else -> "Build Failed"
    }

    val detailText = when {
        isCompiling -> ""
        compileResult == null -> "Press RUN to compile"
        else -> "(${compileResult.errorCount} Errors, ${compileResult.warningCount} Warnings)"
    }

    val amxText = if (compileResult?.amxSizeBytes != null && compileResult.amxSizeBytes > 0) {
        String.format("%.1f KB", compileResult.amxSizeBytes / 1024.0)
    } else null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfacePanel, RoundedCornerShape(4.dp))
            .border(width = 1.dp, color = BorderSolid, shape = RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = titleText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = statusColor
            )
            if (detailText.isNotBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = detailText,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (amxText != null) {
                Text(
                    text = "AMX: $amxText",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

package com.pawno.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawno.studio.ui.theme.BorderFocus
import com.pawno.studio.ui.theme.BorderSolid
import com.pawno.studio.ui.theme.SurfaceBase
import com.pawno.studio.ui.theme.SurfaceInteractive
import com.pawno.studio.ui.theme.TextMuted
import com.pawno.studio.ui.theme.TextSecondary

@Composable
fun BreadcrumbBar(
    folderName: String = "",
    fileName: String = "",
    scopeName: String = "",
    compilerVersion: String = "Pawn 3.10",
    projectName: String = folderName,
    cursorLine: Int = 1,
    cursorCol: Int = 1,
    syntaxMode: String = "PAWN",
    modifier: Modifier = Modifier
) {
    val displayFolder = if (projectName.isNotBlank()) projectName else folderName

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
            .background(SurfaceBase)
            .border(width = 1.dp, color = BorderSolid)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = displayFolder,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "/", fontSize = 10.sp, color = TextMuted)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = fileName,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = BorderFocus
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ln $cursorLine, Col $cursorCol",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = TextMuted
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(SurfaceInteractive)
                    .border(width = 1.dp, color = BorderSolid)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(text = syntaxMode, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextMuted)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .background(SurfaceInteractive)
                    .border(width = 1.dp, color = BorderSolid)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(text = compilerVersion, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextMuted)
            }
        }
    }
}

package com.pawno.studio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Minimalist Geometric 2D Flat Panther Logo
 * Zero gradient, zero 3D, zero neon.
 * Author: By M.B.A & AXEL - Blackpanther Company
 */
@Composable
fun BlackpantherLogoView(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    pantherColor: Color = Color.White,
    color: Color = Color.White,
    cutoutColor: Color = Color(0xFF0E1117)
) {
    val activePantherColor = if (color != Color.White) color else pantherColor
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Scale factors relative to 200x200 viewBox
        val sx = w / 200f
        val sy = h / 200f

        // 1. Outer Panther Head Silhouette
        val outerPath = Path().apply {
            moveTo(40f * sx, 40f * sy)
            lineTo(70f * sx, 70f * sy)
            lineTo(90f * sx, 50f * sy)
            lineTo(110f * sx, 50f * sy)
            lineTo(130f * sx, 70f * sy)
            lineTo(160f * sx, 40f * sy)
            lineTo(150f * sx, 95f * sy)
            lineTo(165f * sx, 120f * sy)
            lineTo(145f * sx, 150f * sy)
            lineTo(100f * sx, 175f * sy)
            lineTo(55f * sx, 150f * sy)
            lineTo(35f * sx, 120f * sy)
            lineTo(50f * sx, 95f * sy)
            close()
        }
        drawPath(outerPath, color = activePantherColor)

        // 2. Forehead cutout
        val forehead = Path().apply {
            moveTo(100f * sx, 65f * sy)
            lineTo(85f * sx, 85f * sy)
            lineTo(100f * sx, 98f * sy)
            lineTo(115f * sx, 85f * sy)
            close()
        }
        drawPath(forehead, color = cutoutColor)

        // 3. Left eye facet
        val leftEye = Path().apply {
            moveTo(65f * sx, 95f * sy)
            lineTo(85f * sx, 100f * sy)
            lineTo(75f * sx, 125f * sy)
            lineTo(55f * sx, 115f * sy)
            close()
        }
        drawPath(leftEye, color = cutoutColor)

        // 4. Right eye facet
        val rightEye = Path().apply {
            moveTo(135f * sx, 95f * sy)
            lineTo(145f * sx, 115f * sy)
            lineTo(125f * sx, 125f * sy)
            lineTo(115f * sx, 100f * sy)
            close()
        }
        drawPath(rightEye, color = cutoutColor)

        // 5. Snout / Nose triangle
        val nose = Path().apply {
            moveTo(90f * sx, 120f * sy)
            lineTo(110f * sx, 120f * sy)
            lineTo(100f * sx, 145f * sy)
            close()
        }
        drawPath(nose, color = cutoutColor)

        // 6. Chin divider line
        drawLine(
            color = cutoutColor,
            start = Offset(100f * sx, 145f * sy),
            end = Offset(100f * sx, 162f * sy),
            strokeWidth = 4f * sx,
            cap = StrokeCap.Round
        )
    }
}

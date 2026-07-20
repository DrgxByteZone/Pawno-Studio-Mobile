package com.pawno.studio.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawno.studio.ui.components.BlackpantherLogoView
import com.pawno.studio.ui.theme.BgRoot
import com.pawno.studio.ui.theme.BorderSubtle
import com.pawno.studio.ui.theme.TextPrimary
import com.pawno.studio.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val creditAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1: Fade in 2D Minimalist Panther Logo
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = LinearEasing)
        )
        // Step 2: Fade in Blackpanther Company Title
        titleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )
        // Step 3: Fade in Authors and Tagline
        creditAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        )
        // Hold for reading
        delay(1200)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgRoot)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onSplashComplete()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // 2D Minimalist Aesthetic Panther Logo
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                BlackpantherLogoView(
                    size = 112.dp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Blackpanther Company Title
            Text(
                text = "BLACKPANTHER COMPANY",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "PRESENTS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // App Name & Tagline
            Text(
                text = "PAWNO STUDIO MOBILE",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(creditAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Grand Theft Auto: San Andreas Multiplayer Pawn IDE",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(creditAlpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Explicit Required Credits: By M.B.A & AXEL
            Text(
                text = "By M.B.A & AXEL",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(creditAlpha.value)
            )
        }

        // Bottom version & architecture indicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .alpha(creditAlpha.value)
        ) {
            Text(
                text = "v1.0.0 • Native pawnc 3.10.11 / 3.10.7 • 16MB Stack",
                color = BorderSubtle,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NairaGuardSplashScreen(
    onSplashFinished: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "halo_pulse")
    
    // Pulsing halo scale and alpha
    val haloPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloScale"
    )
    val haloPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    // Entrance animation states
    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(20f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        // Run logo reveal & text entrance in parallel
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(500))
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            textAlpha.animateTo(1f, animationSpec = tween(500))
            textOffsetY.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy))
        }
        // Progress bar animates across 4.8 seconds
        launch {
            progress.animateTo(1f, animationSpec = tween(4800, easing = LinearEasing))
        }
        // Delay the load to the homepage / home screen by exactly 5 seconds
        val elapsed = System.currentTimeMillis() - startTime
        val remaining = (5000L - elapsed).coerceAtLeast(0L)
        delay(remaining)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF071B11),
                        Color(0xFF09140F),
                        Color(0xFF050706)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Logo Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(175.dp)
            ) {
                // Expanding golden ash halo
                Box(
                    modifier = Modifier
                        .size(145.dp)
                        .scale(haloPulseScale)
                        .alpha(haloPulseAlpha)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFEAB308).copy(alpha = 0.35f),
                                    Color(0xFFA8B2BC).copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Secondary soft glow border
                Box(
                    modifier = Modifier
                        .size(125.dp)
                        .scale(logoScale.value)
                        .clip(CircleShape)
                        .border(
                            width = 1.5.dp,
                            color = Color(0xFFEAB308).copy(alpha = 0.4f * logoAlpha.value),
                            shape = CircleShape
                        )
                )

                // Brand Shield Logo (from uploaded asset)
                Image(
                    painter = painterResource(id = R.drawable.ic_brand_logo),
                    contentDescription = "NairaGuard Logo",
                    modifier = Modifier
                        .size(110.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Brand Typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textOffsetY.value.dp)
            ) {
                // Naira in Ash/Gray and Guard in Yellow/Gold
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Naira",
                        fontWeight = FontWeight.Black,
                        fontSize = 34.sp,
                        letterSpacing = 1.sp,
                        color = Color(0xFFA8B2BC) // Ash / Gray
                    )
                    Text(
                        text = "Guard",
                        fontWeight = FontWeight.Black,
                        fontSize = 34.sp,
                        letterSpacing = 1.sp,
                        color = Color(0xFFEAB308) // Yellow / Gold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Replaced subtitle text with "Smarter Sales, Safer Margins" in lower text font size
                Text(
                    text = "Smarter Sales, Safer Margins",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Sleek loading progress bar
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.value)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFA8B2BC),
                                        Color(0xFFEAB308),
                                        Color(0xFF008B54)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "V1.19",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SettingsEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    settings: SettingsEntity?,
    onTimeout: () -> Unit
) {
    var animateStarted by remember { mutableStateOf(false) }
    val scaleAnim = remember { Animatable(0.6f) }
    val alphaAnim = remember { Animatable(0f) }

    val primaryBgColor = remember(settings) {
        val colorHex = settings?.warnaUtama ?: "#064E3B"
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFF064E3B)
        }
    }
    
    val secondaryBgColor = remember(settings) {
        val colorHex = settings?.warnaSekunder ?: "#115E59"
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFF115E59)
        }
    }

    LaunchedEffect(key1 = true) {
        animateStarted = true
        // Fire parallel scale and fade animations
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200)
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200)
            )
        }
        // Let user see splash for 2.5 seconds
        delay(2500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(primaryBgColor, secondaryBgColor)
                )
            )
            .testTag("splash_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Animating Logo Frame
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Color(0xFFD97706), CircleShape) // Gold border
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "Pondok Pesantren Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name & Subtitle
            AnimatedVisibility(
                visible = animateStarted,
                enter = fadeIn(animationSpec = tween(1200)) + scaleIn(animationSpec = tween(1000, delayMillis = 200))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (settings?.namaPondok ?: "PESANTREN AL-HIDAYAH").uppercase(),
                        color = Color(0xFFFCD34D), // Soft gold
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "Sistem Informasi & Pusat Data Santri",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Visual M3 Progress Indicator
            CircularProgressIndicator(
                color = Color(0xFFFCD34D),
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }

        // Branding footer aligned to the bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pondok Modern Al-Hidayah e-Data",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "v1.0.0 Stable Build",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

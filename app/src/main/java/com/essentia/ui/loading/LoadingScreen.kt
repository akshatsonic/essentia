package com.essentia.ui.loading

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.essentia.ui.theme.EssentiaTypography
import com.essentia.ui.theme.InkBlack
import com.essentia.ui.theme.InkMuted
import com.essentia.ui.theme.PaperWhite
import com.essentia.ui.theme.Rule

@Composable
fun LoadingScreen() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing)
        ),
        label = "shimmer-progress"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE4E0D8),
            Color(0xFFEFEBE1),
            Color(0xFFE4E0D8)
        ),
        start = Offset(x = -300f + progress * 600f, y = 0f),
        end = Offset(x = 0f + progress * 600f, y = 0f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            // Pulse dot
            PulseDot(progress = progress)

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Fetching your stories",
                style = EssentiaTypography.headlineMedium.copy(fontSize = 24.sp),
                color = InkBlack,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(1.dp)
                        .background(InkMuted)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "CURATING THE ESSENTIALS",
                    style = EssentiaTypography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 2.sp
                    ),
                    color = InkMuted
                )
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(1.dp)
                        .background(InkMuted)
                )
            }

            Spacer(Modifier.height(48.dp))

            // Shimmer skeleton placeholders
            repeat(3) { i ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (i == 0) 1f else if (i == 1) 0.92f else 0.78f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(shimmerBrush)
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.weight(1.2f))
        }
    }
}

@Composable
private fun PulseDot(progress: Float) {
    val scale = 0.8f + (kotlin.math.sin(progress * Math.PI * 2).toFloat()) * 0.2f
    Box(
        modifier = Modifier
            .size((48 * scale).dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFB8410F).copy(alpha = 0.7f + 0.3f * (1f - progress))),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((20 * scale).dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFB8410F))
        )
    }
}

package com.essentia.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.essentia.AppModule
import com.essentia.data.model.Category
import com.essentia.data.model.DigestItem
import com.essentia.llm.DigestState
import com.essentia.ui.components.Byline
import com.essentia.ui.components.CategoryTag
import com.essentia.ui.components.EditorialRule
import com.essentia.ui.components.ReadingTimeLabel
import com.essentia.ui.components.SourceRow
import com.essentia.ui.components.TopicChip
import com.essentia.ui.theme.Accent
import com.essentia.ui.theme.AnalysisBlue
import com.essentia.ui.theme.BreakingRed
import com.essentia.ui.theme.CategoryTagStyle
import com.essentia.ui.theme.EssentiaTypography
import com.essentia.ui.theme.InkBlack
import com.essentia.ui.theme.InkMuted
import com.essentia.ui.theme.InkSoft
import com.essentia.ui.theme.OpinionViolet
import com.essentia.ui.theme.PaperWhite
import com.essentia.ui.theme.Rule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    onItemClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val repo = remember { AppModule.repository }
    val engine = remember { AppModule.digestEngine }
    val today by repo.today.collectAsState()
    val engineState by engine.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // We do NOT auto-trigger a digest on first launch anymore — the user must
    // explicitly tap "Run Digest Now" in Settings. This way the first digest
    // happens after they've configured their key + sources + topic.

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperWhite)
    ) {
        val response = today
        when {
            engineState is DigestState.Loading -> {
                EngineLoadingState(state = engineState as DigestState.Loading)
            }
            engineState is DigestState.Error -> {
                EmptyTodayState(
                    title = "Couldn't build digest",
                    message = (engineState as DigestState.Error).message,
                    cta = "Open Settings",
                    onCta = onSettingsClick
                )
            }
            response == null || response.items.isEmpty() -> {
                EmptyTodayState(
                    title = "No digest yet",
                    message = "Open Settings → Maintenance → Run Digest Now to generate your first edition.",
                    cta = "Open Settings",
                    onCta = onSettingsClick
                )
            }
            else -> {
                TodayContent(
                    digestItems = response.items,
                    isLoading = false,
                    onItemClick = onItemClick,
                    onSettingsClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
private fun TodayContent(
    digestItems: List<DigestItem>,
    isLoading: Boolean,
    onItemClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val featured = digestItems.firstOrNull { it.isFeatured } ?: digestItems.first()
    val rest = digestItems.filter { it.id != featured.id }
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            TodayHeader(onSettingsClick = onSettingsClick)
        }
        item {
            HeroFeaturedCard(
                item = featured,
                onClick = { onItemClick(featured.id) }
            )
        }
        item {
            Spacer(Modifier.height(8.dp))
            EditorialRule(modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(20.dp))
            SectionLabel(text = "Today's Edition", modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(8.dp))
        }
        items(
            items = rest,
            key = { it.id }
        ) { item ->
            DigestCardAnimated(
                item = item,
                onClick = { onItemClick(item.id) }
            )
        }
    }
}

@Composable
private fun TodayHeader(onSettingsClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 56.dp, bottom = 16.dp)) {
        // Settings gear at top right
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = InkBlack,
                modifier = Modifier.size(22.dp)
            )
        }
        // Centered title block
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Essentia",
                style = EssentiaTypography.displayLarge,
                color = InkBlack
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tuesday, October 24",
                style = EssentiaTypography.labelMedium.copy(letterSpacing = 1.2.sp),
                color = InkMuted
            )
        }
    }
}

@Composable
private fun HeroFeaturedCard(item: DigestItem, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        // Hero block — v1: tinted gradient placeholder (no image yet)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A2A2A),
                            Color(0xFF0F0F0F)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ESSENTIA",
                color = PaperWhite.copy(alpha = 0.18f),
                style = EssentiaTypography.displayLarge,
                fontSize = 38.sp
            )
        }
        Spacer(Modifier.height(16.dp))
        CategoryTag(category = item.category)
        Spacer(Modifier.height(10.dp))
        Text(
            text = item.title,
            style = EssentiaTypography.headlineLarge.copy(fontSize = 30.sp, lineHeight = 34.sp),
            color = InkBlack
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.summary,
            style = EssentiaTypography.bodyLarge,
            color = InkSoft
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SourceRow(sources = item.sources)
                item.byline?.let { Byline(text = "· $it") }
            }
            ReadingTimeLabel(minutes = item.readTimeMinutes)
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = CategoryTagStyle.copy(fontSize = 11.sp, letterSpacing = 2.sp),
        color = InkMuted,
        modifier = modifier,
        textAlign = TextAlign.Start
    )
}

@Composable
private fun DigestCardAnimated(item: DigestItem, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(item.id) {
        delay(60L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(420)) +
                slideInVertically(animationSpec = tween(420)) { it / 6 },
        exit = fadeOut(animationSpec = tween(220)) +
                slideOutVertically(animationSpec = tween(220)) { it / 6 }
    ) {
        DigestCard(item = item, onClick = onClick)
    }
}

@Composable
private fun DigestCard(item: DigestItem, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        CategoryTag(category = item.category)
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.title,
            style = EssentiaTypography.headlineSmall,
            color = InkBlack
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = item.summary,
            style = EssentiaTypography.bodyMedium,
            color = InkSoft,
            maxLines = 3
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SourceRow(sources = item.sources)
            ReadingTimeLabel(minutes = item.readTimeMinutes)
        }
    }
    EditorialRule(modifier = Modifier.padding(horizontal = 24.dp))
}

@Composable
private fun TodayLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading your edition…",
            style = EssentiaTypography.bodyLarge,
            color = InkMuted
        )
    }
}

@Composable
private fun EngineLoadingState(state: DigestState.Loading) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing)
        ),
        label = "shimmer-progress"
    )
    val topicName: String = state.topic
    val progressValue: Float = state.progress
    val pulseScale: Float = 0.8f + 0.2f * kotlin.math.sin((shimmerProgress * Math.PI * 2).toDouble()).toFloat()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size((48 * pulseScale).dp)
                .clip(RoundedCornerShape(50))
                .background(Accent.copy(alpha = 0.7f + 0.3f * (1f - shimmerProgress))),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((20 * pulseScale).dp)
                    .clip(RoundedCornerShape(50))
                    .background(Accent)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Building your digest",
            style = EssentiaTypography.headlineMedium,
            color = InkBlack
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "for \"$topicName\"",
            style = EssentiaTypography.bodyLarge,
            color = InkMuted
        )
        if (progressValue > 0f) {
            Spacer(Modifier.height(16.dp))
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier.fillMaxWidth(),
                color = Accent,
                trackColor = Color(0xFFE4E0D8)
            )
        }
    }
}

@Composable
private fun EmptyTodayState(
    title: String,
    message: String,
    cta: String,
    onCta: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = EssentiaTypography.headlineMedium,
            color = InkBlack
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = EssentiaTypography.bodyLarge,
            color = InkMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(InkBlack)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCta
                )
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(
                text = cta.uppercase(),
                style = CategoryTagStyle.copy(fontSize = 12.sp, letterSpacing = 2.sp),
                color = PaperWhite
            )
        }
    }
}

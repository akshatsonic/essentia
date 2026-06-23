package com.essentia.ui.archive

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.essentia.AppModule
import com.essentia.data.model.DigestItem
import com.essentia.ui.components.EditorialRule
import com.essentia.ui.components.ReadingTimeLabel
import com.essentia.ui.theme.EssentiaTypography
import com.essentia.ui.theme.InkBlack
import com.essentia.ui.theme.InkMuted
import com.essentia.ui.theme.PaperWhite
import kotlinx.coroutines.delay

@Composable
fun ArchiveScreen(onItemClick: (String) -> Unit) {
    val repo = remember { AppModule.repository }
    val items by repo.savedForLater.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperWhite)
            .statusBarsPadding()
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Saved for Later",
            style = EssentiaTypography.displayMedium,
            color = InkBlack,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(20.dp))
        EditorialRule()

        if (items.isEmpty()) {
            EmptyArchiveState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 24.dp)
            ) {
                items(items = items, key = { it.id }) { item ->
                    ArchiveRowAnimated(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onUnsave = { repo.toggleArchive(item.id) }
                    )
                    EditorialRule()
                }
            }
        }
    }
}

@Composable
private fun EmptyArchiveState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                tint = InkMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Nothing saved yet",
                style = EssentiaTypography.titleLarge,
                color = InkBlack
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tap the bookmark on any story to save it for later.",
                style = EssentiaTypography.bodyMedium,
                color = InkMuted
            )
        }
    }
}

@Composable
private fun ArchiveRowAnimated(
    item: DigestItem,
    onClick: () -> Unit,
    onUnsave: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(item.id) {
        delay(60L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(380)) + slideInVertically(tween(380)) { it / 8 },
        exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 8 }
    ) {
        ArchiveRow(item = item, onClick = onClick, onUnsave = onUnsave)
    }
}

@Composable
private fun ArchiveRow(item: DigestItem, onClick: () -> Unit, onUnsave: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail — v1: tinted gradient block (image placeholder)
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 64.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A2A2A),
                            Color(0xFF1A1A1A)
                        )
                    )
                )
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = EssentiaTypography.titleMedium.copy(fontSize = 16.sp, lineHeight = 22.sp),
                color = InkBlack,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            ReadingTimeLabel(minutes = item.readTimeMinutes)
        }
        IconButton(onClick = onUnsave) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = "Unsave",
                tint = InkMuted
            )
        }
    }
}

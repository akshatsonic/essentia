package com.essentia.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.essentia.AppModule
import com.essentia.data.model.DigestItem
import com.essentia.data.model.Source
import com.essentia.ui.components.Byline
import com.essentia.ui.components.CategoryTag
import com.essentia.ui.components.EditorialRule
import com.essentia.ui.components.SourceIcon
import com.essentia.ui.theme.Accent
import com.essentia.ui.theme.EssentiaTypography
import com.essentia.ui.theme.InkBlack
import com.essentia.ui.theme.InkMuted
import com.essentia.ui.theme.InkSoft
import com.essentia.ui.theme.PaperWhite
import com.essentia.ui.theme.Rule

@Composable
fun DetailScreen(itemId: String) {
    val repo = remember { AppModule.repository }
    val today by repo.today.collectAsState()
    val saved by repo.savedForLater.collectAsState()

    val item = remember(itemId) {
        today?.items?.firstOrNull { it.id == itemId }
            ?: saved.firstOrNull { it.id == itemId }
            ?: repo.getItem(itemId)
    }
    val isArchived = repo.isArchived(itemId)
    val context = LocalContext.current

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(itemId) { visible = true }

    if (item == null) {
        Box(modifier = Modifier.fillMaxSize().background(PaperWhite), contentAlignment = Alignment.Center) {
            Text("Item not found", style = EssentiaTypography.bodyLarge, color = InkMuted)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperWhite)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(420)) +
                    slideInVertically(animationSpec = tween(420)) { it / 8 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 88.dp)
            ) {
                DetailHero(item = item)
                DetailBody(item = item)
                DetailSources(item = item)
                Spacer(Modifier.height(24.dp))
            }
        }

        DetailTopBar(
            onBack = { AppModule.nav { it.popBackStack() } },
            onSave = { repo.toggleArchive(itemId) },
            onShare = { shareItem(context, item) },
            isSaved = isArchived
        )

        DetailBottomBar(
            onBack = { AppModule.nav { it.popBackStack() } },
            onSave = { repo.toggleArchive(itemId) },
            onShare = { shareItem(context, item) },
            isSaved = isArchived,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun DetailHero(item: DigestItem) {
    Column {
        // v1: tinted gradient hero (image placeholder)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
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
                color = PaperWhite.copy(alpha = 0.16f),
                style = EssentiaTypography.displayLarge,
                fontSize = 36.sp
            )
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            CategoryTag(category = item.category)
            Spacer(Modifier.height(14.dp))
            Text(
                text = item.title,
                style = EssentiaTypography.displaySmall.copy(fontSize = 30.sp, lineHeight = 34.sp),
                color = InkBlack
            )
            Spacer(Modifier.height(12.dp))
            item.byline?.let { Byline(text = it) }
            Spacer(Modifier.height(16.dp))
            EditorialRule()
        }
    }
}

@Composable
private fun DetailBody(item: DigestItem) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        // Lead summary
        Text(
            text = item.summary,
            style = EssentiaTypography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 28.sp),
            color = InkBlack
        )
        Spacer(Modifier.height(20.dp))

        // Detail paragraphs
        item.detailSummary.split("\n\n").forEach { paragraph ->
            Text(
                text = paragraph.trim(),
                style = EssentiaTypography.bodyLarge,
                color = InkSoft
            )
            Spacer(Modifier.height(16.dp))
        }

        // Pull quote
        item.pullQuote?.let { quote ->
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(Accent)
                        .align(Alignment.CenterStart)
                )
                Text(
                    text = "\u201C$quote\u201D",
                    style = EssentiaTypography.headlineSmall.copy(
                        fontSize = 22.sp,
                        lineHeight = 30.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = InkBlack,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailSources(item: DigestItem) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(8.dp))
        EditorialRule()
        Spacer(Modifier.height(20.dp))
        Text(
            text = "SOURCES",
            style = EssentiaTypography.labelMedium.copy(letterSpacing = 2.sp, fontSize = 11.sp),
            color = InkMuted
        )
        Spacer(Modifier.height(12.dp))
        item.sources.forEachIndexed { idx, source ->
            SourceRow(source = source, url = item.sourceUrls.getOrNull(idx).orEmpty())
            if (idx < item.sources.lastIndex) Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SourceRow(source: Source, url: String) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { /* open URL */ }
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SourceIcon(source = source, size = 16)
            Spacer(Modifier.width(12.dp))
            Text(
                text = source.displayName,
                style = EssentiaTypography.bodyMedium,
                color = InkBlack
            )
        }
        Icon(
            imageVector = Icons.Outlined.OpenInNew,
            contentDescription = "Open",
            tint = InkMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun DetailTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    isSaved: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = InkBlack)
        }
        Row {
            IconButton(onClick = onSave) {
                Icon(
                    imageVector = if (isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isSaved) "Unsave" else "Save",
                    tint = InkBlack
                )
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Outlined.Share, contentDescription = "Share", tint = InkBlack)
            }
        }
    }
}

@Composable
private fun DetailBottomBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    isSaved: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PaperWhite)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Rule)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = InkBlack)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onSave) {
                    Icon(
                        imageVector = if (isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isSaved) "Unsave" else "Save",
                        tint = InkBlack
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", tint = InkBlack)
                }
            }
        }
    }
}

private fun shareItem(context: android.content.Context, item: DigestItem) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_SUBJECT, item.title)
        putExtra(android.content.Intent.EXTRA_TEXT, "${item.title}\n\n${item.summary}\n\n${item.sourceUrls.joinToString("\n")}")
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
}

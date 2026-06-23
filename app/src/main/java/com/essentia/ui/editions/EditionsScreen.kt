package com.essentia.ui.editions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.essentia.AppModule
import com.essentia.data.model.Edition
import com.essentia.data.model.EditionType
import com.essentia.llm.mock.MockEditions
import com.essentia.ui.components.EditorialRule
import com.essentia.ui.theme.EssentiaTypography
import com.essentia.ui.theme.InkBlack
import com.essentia.ui.theme.InkMuted
import com.essentia.ui.theme.PaperWhite
import com.essentia.ui.theme.Rule
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EditionsScreen(onEditionClick: (String) -> Unit) {
    val repo = remember { AppModule.repository }
    val editions by repo.pastEditions.collectAsState()

    var selectedTab by remember { mutableStateOf<EditionsTab>(EditionsTab.DAILY) }
    val filtered = editions.filter { it.type.name == selectedTab.name }
        .let { if (it.isEmpty()) editions else it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperWhite)
            .statusBarsPadding()
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Past Editions",
            style = EssentiaTypography.displayMedium,
            color = InkBlack,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(20.dp))

        EditionsSegmentedTabs(
            selected = selectedTab,
            onSelect = { selectedTab = it }
        )

        Spacer(Modifier.height(16.dp))
        EditorialRule()
        Spacer(Modifier.height(8.dp))

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 8 })
                    .togetherWith(fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 8 })
            },
            label = "editions-list"
        ) { tab ->
            val visible = editions.filter { it.type.name == tab.name }
                .let { if (it.isEmpty()) editions else it }
            EditionsGrid(items = visible, onEditionClick = onEditionClick)
        }
    }
}

enum class EditionsTab(val label: String) { DAILY("DAILY"), WEEKEND("WEEKEND") }

@Composable
private fun EditionsSegmentedTabs(selected: EditionsTab, onSelect: (EditionsTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        EditionsTab.values().forEach { tab ->
            val isSelected = tab == selected
            Column(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(tab) }
                    )
            ) {
                Text(
                    text = tab.label,
                    style = EssentiaTypography.labelMedium.copy(
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    ),
                    color = if (isSelected) InkBlack else InkMuted
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(if (isSelected) InkBlack else androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun EditionsGrid(items: List<Edition>, onEditionClick: (String) -> Unit) {
    val pairs = items.chunked(2)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pairs) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { edition ->
                    Box(modifier = Modifier.weight(1f)) {
                        EditionCardAnimated(edition = edition, onClick = { onEditionClick(edition.id) })
                    }
                }
                if (rowItems.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EditionCardAnimated(edition: Edition, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(edition.id) {
        delay(40L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(380)) + slideInVertically(tween(380)) { it / 8 },
        exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 8 }
    ) {
        EditionCard(edition = edition, onClick = onClick)
    }
}

@Composable
private fun EditionCard(edition: Edition, onClick: () -> Unit) {
    val items = remember(edition.id) {
        MockEditions.allItems[edition.id].orEmpty()
    }
    val firstItem = items.firstOrNull()
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Text(
            text = displayDate(edition),
            style = EssentiaTypography.labelMedium.copy(
                fontSize = 10.sp,
                letterSpacing = 1.5.sp
            ),
            color = InkMuted
        )
        Spacer(Modifier.height(8.dp))
        firstItem?.let { item ->
            Text(
                text = item.title,
                style = EssentiaTypography.titleLarge.copy(fontSize = 18.sp, lineHeight = 22.sp),
                color = InkBlack,
                maxLines = 3
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = item.summary,
                style = EssentiaTypography.bodySmall,
                color = InkMuted,
                maxLines = 3
            )
        }
    }
    EditorialRule(color = Rule.copy(alpha = 0.6f))
}

private fun displayDate(edition: Edition): String {
    val parts = edition.date.split("-")
    if (parts.size != 3) return edition.date
    val month = parts[1].toIntOrNull() ?: return edition.date
    val day = parts[2].toIntOrNull() ?: return edition.date
    val monthName = SimpleDateFormat("MMM", Locale.US).format(
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(edition.date) ?: return edition.date
    )
    val suffix = ordinalSuffix(day)
    return "$monthName $day$suffix"
}

private fun ordinalSuffix(day: Int): String = when {
    day in 11..13 -> "th"
    day % 10 == 1 -> "st"
    day % 10 == 2 -> "nd"
    day % 10 == 3 -> "rd"
    else -> "th"
}

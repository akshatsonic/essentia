package com.essentia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.essentia.data.model.Category
import com.essentia.data.model.Source
import com.essentia.ui.theme.AnalysisBlue
import com.essentia.ui.theme.BreakingRed
import com.essentia.ui.theme.BylineStyle
import com.essentia.ui.theme.CategoryTagStyle
import com.essentia.ui.theme.InkBlack
import com.essentia.ui.theme.InkMuted
import com.essentia.ui.theme.OpinionViolet
import com.essentia.ui.theme.PaperWhite
import com.essentia.ui.theme.Rule

@Composable
fun CategoryTag(category: Category, modifier: Modifier = Modifier) {
    val color = when (category) {
        Category.BREAKING -> BreakingRed
        Category.ANALYSIS -> AnalysisBlue
        Category.OPINION -> OpinionViolet
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = 2.dp)
                .background(color)
        )
        Text(
            text = category.label,
            style = CategoryTagStyle,
            color = color,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
fun SourceIcon(source: Source, modifier: Modifier = Modifier, size: Int = 14) {
    val icon = when (source) {
        Source.REDDIT -> Icons.Outlined.Forum
        Source.HN -> Icons.Outlined.Whatshot
        Source.X -> Icons.Outlined.Public
    }
    Icon(
        imageVector = icon,
        contentDescription = source.displayName,
        tint = InkBlack,
        modifier = modifier.size(size.dp)
    )
}

@Composable
fun SourceRow(
    sources: List<Source>,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        sources.forEach { s ->
            SourceIcon(s)
        }
    }
}

@Composable
fun TopicChip(topicTag: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .border(
                width = 1.dp,
                color = InkMuted.copy(alpha = 0.4f),
                shape = RoundedCornerShape(2.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = topicTag.uppercase(),
            style = CategoryTagStyle.copy(fontSize = 9.sp, letterSpacing = 1.2.sp),
            color = InkMuted
        )
    }
}

@Composable
fun Byline(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = BylineStyle,
        color = InkMuted,
        modifier = modifier
    )
}

@Composable
fun ReadingTimeLabel(minutes: Int, modifier: Modifier = Modifier) {
    Text(
        text = "${minutes} MIN READ",
        style = CategoryTagStyle.copy(fontSize = 10.sp, letterSpacing = 1.sp),
        color = InkMuted,
        modifier = modifier
    )
}

@Composable
fun EditorialRule(modifier: Modifier = Modifier, color: Color = Rule) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

@Composable
fun AccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (enabled) InkBlack else InkMuted.copy(alpha = 0.4f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = CategoryTagStyle.copy(fontSize = 13.sp, letterSpacing = 2.sp),
            color = PaperWhite
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(PaperWhite)
            .border(1.dp, InkBlack, RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = CategoryTagStyle.copy(fontSize = 13.sp, letterSpacing = 2.sp),
            color = InkBlack
        )
    }
}

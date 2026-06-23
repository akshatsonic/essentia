package com.essentia.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.essentia.ui.theme.InkBlack
import com.essentia.ui.theme.NavBackground
import com.essentia.ui.theme.NavIconActive
import com.essentia.ui.theme.NavIconInactive
import com.essentia.ui.theme.PaperWhite

enum class MainTab(val route: String, val label: String, val icon: ImageVector) {
    Today("today", "Today", Icons.Outlined.Today),
    Editions("editions", "Editions", Icons.Outlined.CalendarMonth),
    Archive("archive", "Archive", Icons.Outlined.Bookmark)
}

@Composable
fun BottomNav(
    current: MainTab,
    onSelect: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NavBackground)
    ) {
        // Top hairline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(com.essentia.ui.theme.Rule)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTab.values().forEach { tab ->
                NavItem(
                    tab = tab,
                    selected = current == tab,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val iconColor by animateColorAsState(
        targetValue = if (selected) NavIconActive else NavIconInactive,
        animationSpec = tween(280),
        label = "iconColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) NavIconActive else NavIconInactive,
        animationSpec = tween(280),
        label = "textColor"
    )
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(280),
        label = "indicatorAlpha"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Pill indicator
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(androidx.compose.ui.graphics.Color.Transparent)
            )
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        PaperWhite.copy(alpha = indicatorAlpha)
                    )
            )
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = tab.label.uppercase(),
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

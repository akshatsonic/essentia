package com.essentia.ui.editions

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.essentia.AppModule
import com.essentia.llm.mock.MockEditions
import com.essentia.ui.components.CategoryTag
import com.essentia.ui.components.EditorialRule
import com.essentia.ui.components.ReadingTimeLabel
import com.essentia.ui.components.SourceRow
import com.essentia.ui.theme.EssentiaTypography
import com.essentia.ui.theme.InkBlack
import com.essentia.ui.theme.InkMuted
import com.essentia.ui.theme.InkSoft
import com.essentia.ui.theme.PaperWhite

@Composable
fun PastEditionScreen(editionId: String, onItemClick: (String) -> Unit) {
    val items = remember(editionId) { MockEditions.allItems[editionId].orEmpty() }
    val edition = remember(editionId) { MockEditions.allEditions.firstOrNull { it.id == editionId } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperWhite)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppModule.nav { it.popBackStack() } }) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = InkBlack)
            }
            Spacer(Modifier.size(4.dp))
            Text(
                text = edition?.date.orEmpty(),
                style = EssentiaTypography.titleMedium,
                color = InkBlack
            )
        }
        EditorialRule()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(items = items, key = { it.id }) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemClick(item.id) }
                        )
                        .padding(vertical = 18.dp)
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
                EditorialRule()
            }
        }
    }
}

package com.essentia.ui.topics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.essentia.AppModule
import com.essentia.data.model.Topic
import com.essentia.ui.components.EditorialRule
import com.essentia.ui.theme.EssentiaTypography
import com.essentia.ui.theme.InkBlack
import com.essentia.ui.theme.InkMuted
import com.essentia.ui.theme.InkSoft
import com.essentia.ui.theme.PaperWhite

@Composable
fun TopicsScreen() {
    val topics = remember {
        mutableStateListOf(
            Topic("ai-agents", "AI Agents", "AI agents autonomous systems LLM tooling"),
            Topic("sde3", "SDE-3 System Design", "system design senior engineering interviews architecture"),
            Topic("world", "World News", "geopolitics global affairs"),
            Topic("tech", "Tech Industry", "tech industry earnings big tech")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperWhite)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppModule.nav { it.popBackStack() } }) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = InkBlack)
            }
            Spacer(Modifier.padding(4.dp))
            Text("Topics", style = EssentiaTypography.titleLarge, color = InkBlack)
        }
        EditorialRule()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(topics, key = { it.id }) { topic ->
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        text = topic.name,
                        style = EssentiaTypography.titleLarge.copy(fontSize = 20.sp),
                        color = InkBlack
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = topic.query,
                        style = EssentiaTypography.bodySmall,
                        color = InkMuted
                    )
                }
                EditorialRule()
            }
        }
    }
}

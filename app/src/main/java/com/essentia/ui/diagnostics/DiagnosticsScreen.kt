package com.essentia.ui.diagnostics

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.essentia.AppModule
import com.essentia.crash.CrashLogger
import com.essentia.ui.components.AccentButton
import com.essentia.ui.components.EditorialRule
import com.essentia.ui.components.SecondaryButton
import com.essentia.ui.theme.EssentiaTypography
import com.essentia.ui.theme.InkBlack
import com.essentia.ui.theme.InkMuted
import com.essentia.ui.theme.InkSoft
import com.essentia.ui.theme.PaperWhite
import com.essentia.ui.theme.Rule
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnosticsScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var refreshTick by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<File?>(null) }
    var contents by remember { mutableStateOf<String?>(null) }
    var contentsLoading by remember { mutableStateOf(false) }

    val logs = remember(refreshTick) { CrashLogger.logFiles() }

    // Load selected file's content when selection changes
    LaunchedEffect(selected) {
        if (selected != null) {
            contentsLoading = true
            contents = runCatching { selected!!.readText() }.getOrDefault("(could not read file)")
            contentsLoading = false
        } else {
            contents = null
        }
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
            Spacer(Modifier.size(4.dp))
            Text("Diagnostics", style = EssentiaTypography.titleLarge, color = InkBlack)
        }
        EditorialRule()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Header card
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.BugReport,
                    contentDescription = null,
                    tint = InkBlack,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.size(10.dp))
                Column {
                    Text(
                        text = "Crash Logs",
                        style = EssentiaTypography.titleLarge,
                        color = InkBlack
                    )
                    Text(
                        text = CrashLogger.directoryPath() ?: "Not initialized",
                        style = EssentiaTypography.bodySmall,
                        color = InkMuted
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "Every uncaught exception is written to a timestamped .txt file in the directory above. Files survive app restarts and are kept until you clear them or hit the cap (20).",
                style = EssentiaTypography.bodyMedium,
                color = InkSoft
            )

            Spacer(Modifier.height(20.dp))

            // Active log preview
            val active = selected
            if (active != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SecondaryButton(text = "Share Log", onClick = {
                            shareLog(ctx, active)
                        })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SecondaryButton(text = "Close", onClick = {
                            selected = null
                        })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 480.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(androidx.compose.ui.graphics.Color(0xFFF1EDE5))
                        .padding(12.dp)
                ) {
                    if (contentsLoading) {
                        Text(
                            text = "Loading…",
                            style = EssentiaTypography.bodySmall,
                            color = InkMuted
                        )
                    } else {
                        Text(
                            text = contents.orEmpty(),
                            style = EssentiaTypography.bodySmall.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            ),
                            color = InkBlack
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                EditorialRule()
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "ALL LOGS (${logs.size})",
                    style = EssentiaTypography.labelSmall.copy(letterSpacing = 1.5.sp, fontSize = 10.sp),
                    color = InkMuted
                )
                Spacer(Modifier.height(8.dp))
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        AccentButton(
                            text = "Cause Test Crash",
                            onClick = {
                                // Throw on the main thread so the default uncaught handler
                                // catches it. Useful for verifying the logger works.
                                throw RuntimeException("Test crash from Diagnostics at ${Date()}")
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SecondaryButton(
                            text = "Clear All",
                            onClick = {
                                CrashLogger.clearAll()
                                selected = null
                                refreshTick++
                            }
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                EditorialRule()
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "ALL LOGS (${logs.size})",
                    style = EssentiaTypography.labelSmall.copy(letterSpacing = 1.5.sp, fontSize = 10.sp),
                    color = InkMuted
                )
                Spacer(Modifier.height(8.dp))
            }

            if (logs.isEmpty()) {
                Text(
                    text = "No crash logs yet. Cause a test crash to verify the logger works.",
                    style = EssentiaTypography.bodyMedium,
                    color = InkMuted
                )
            } else {
                logs.forEach { file ->
                    LogRow(
                        file = file,
                        isSelected = selected?.absolutePath == file.absolutePath,
                        onSelect = {
                            selected = if (selected?.absolutePath == file.absolutePath) null else file
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LogRow(file: File, isSelected: Boolean, onSelect: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val sizeKb = file.length() / 1024
    val date = remember(file) {
        SimpleDateFormat("MMM d, HH:mm:ss", Locale.US).format(Date(file.lastModified()))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelect
            )
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = EssentiaTypography.bodyLarge.copy(fontSize = 15.sp),
                    color = if (isSelected) com.essentia.ui.theme.Accent else InkBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$date · ${sizeKb} KB",
                    style = EssentiaTypography.bodySmall,
                    color = InkMuted
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share",
                    tint = com.essentia.ui.theme.Accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
    EditorialRule(color = Rule.copy(alpha = 0.6f))
}

private fun shareLog(ctx: android.content.Context, file: File) {
    runCatching {
        val authority = ctx.packageName + ".fileprovider"
        val uri = FileProvider.getUriForFile(ctx, authority, file)
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Essentia crash log: ${file.name}")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(android.content.Intent.createChooser(intent, "Share crash log"))
    }.onFailure {
        android.widget.Toast.makeText(
            ctx,
            "Could not share: ${it.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}

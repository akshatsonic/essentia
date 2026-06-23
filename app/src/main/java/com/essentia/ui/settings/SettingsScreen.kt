package com.essentia.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.essentia.AppModule
import com.essentia.data.prefs.SecureKeys
import com.essentia.data.repository.AppSettings
import com.essentia.llm.DigestState
import com.essentia.ui.components.AccentButton
import com.essentia.ui.components.EditorialRule
import com.essentia.ui.components.SecondaryButton
import com.essentia.ui.theme.EssentiaTypography
import com.essentia.ui.theme.InkBlack
import com.essentia.ui.theme.InkMuted
import com.essentia.ui.theme.InkSoft
import com.essentia.ui.theme.PaperWhite
import com.essentia.ui.theme.Rule
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onOpenDiagnostics: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AppModule.settingsRepository }
    val settingsState = repo.settings.collectAsState(initial = null)

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
            Text("Settings", style = EssentiaTypography.titleLarge, color = InkBlack)
        }
        EditorialRule()

        val s = settingsState.value
        if (s == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading…", style = EssentiaTypography.bodyLarge, color = InkMuted)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LlmProviderSection(settings = s, onSaved = { /* no-op */ }, onError = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            })

            SourcesSection(settings = s)

            MaintenanceSection()

            DiagnosticsEntry(onClick = onOpenDiagnostics)

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Essentia v0.2.0",
                style = EssentiaTypography.labelSmall.copy(letterSpacing = 1.sp),
                color = InkMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LlmProviderSection(
    settings: AppSettings,
    onSaved: () -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val repo = remember { AppModule.settingsRepository }
    var apiKey by remember { mutableStateOf("") }
    var xToken by remember { mutableStateOf("") }
    var verifying by remember { mutableStateOf(false) }
    var lastVerified by remember { mutableStateOf(false) }
    LaunchedEffect(settings.openaiApiKey) {
        // Pre-fill with the existing masked placeholder; the real key is never read
        apiKey = settings.openaiApiKey?.let { "sk-•••••••••••••••" } ?: ""
        xToken = settings.xBearerToken?.let { "•••••••••••••••" } ?: ""
    }

    SectionLabel("LLM PROVIDER")
    Spacer(Modifier.height(12.dp))

    Text("Provider", style = EssentiaTypography.bodyLarge, color = InkBlack)
    Spacer(Modifier.height(4.dp))
    Text(
        "OpenAI-compatible (OpenRouter, OpenAI, Groq, Together, etc.)",
        style = EssentiaTypography.bodyMedium,
        color = InkSoft
    )
    EditorialRule()

    Text("Model", style = EssentiaTypography.bodyLarge, color = InkBlack, modifier = Modifier.padding(top = 12.dp))
    Spacer(Modifier.height(4.dp))
    Text(settings.model, style = EssentiaTypography.bodyMedium, color = InkSoft)
    EditorialRule()

    // API key
    Text("API key", style = EssentiaTypography.bodyLarge, color = InkBlack, modifier = Modifier.padding(top = 12.dp))
    Spacer(Modifier.height(8.dp))
    val isPlaceholder = apiKey.startsWith("sk-•") || apiKey.startsWith("Bearer")
    EditableSecretField(
        value = apiKey,
        isPlaceholder = isPlaceholder,
        onValueChange = { apiKey = it; lastVerified = false },
        placeholder = "sk-•••••••••••••••"
    )
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            SecondaryButton(
                text = if (lastVerified) "✓ Verified" else if (verifying) "Verifying…" else "Test Connection",
                onClick = {
                    if (isPlaceholder || apiKey.isBlank()) {
                        onError("Enter a new API key first")
                        return@SecondaryButton
                    }
                    verifying = true
                    scope.launch {
                        val result = runCatching {
                            com.essentia.llm.openai.OpenAiCompatibleProvider.testKey(
                                key = apiKey.trim(),
                                baseUrl = com.essentia.llm.openai.OpenAiCompatibleProvider.DEFAULT_BASE_URL,
                                model = com.essentia.llm.openai.OpenAiCompatibleProvider.DEFAULT_MODEL
                            )
                        }
                        verifying = false
                        result.fold(
                            onSuccess = {
                                lastVerified = true
                                repo.setProviderId("openai_compatible")
                                repo.setOpenaiCompatibleApiKey(apiKey.trim())
                                onSaved()
                            },
                            onFailure = { err -> onError(err.message ?: "Verification failed") }
                        )
                    }
                },
                enabled = !isPlaceholder && apiKey.isNotBlank() && !verifying && !lastVerified
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            SecondaryButton(
                text = "Save",
                onClick = {
                    if (!isPlaceholder && apiKey.isNotBlank()) {
                        scope.launch {
                            repo.setProviderId("openai_compatible")
                            repo.setOpenaiCompatibleApiKey(apiKey.trim())
                            lastVerified = true
                            onSaved()
                        }
                    }
                },
                enabled = !isPlaceholder && apiKey.isNotBlank()
            )
        }
    }
    if (lastVerified) {
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = com.essentia.ui.theme.AnalysisBlue,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "API key saved.",
                style = EssentiaTypography.bodySmall,
                color = com.essentia.ui.theme.AnalysisBlue
            )
        }
    }
    Spacer(Modifier.height(4.dp))
    Text(
        "Your key is encrypted locally and never leaves the device.",
        style = EssentiaTypography.bodySmall,
        color = InkMuted
    )
}

@Composable
private fun SourcesSection(settings: AppSettings) {
    val scope = rememberCoroutineScope()
    val repo = remember { AppModule.settingsRepository }
    val sources = settings.sourcesEnabled

    SectionLabel("DATA SOURCES")
    Spacer(Modifier.height(12.dp))
    ToggleRow(label = "Reddit", description = "Top posts from your subs", enabled = "reddit" in sources) { enabled ->
        scope.launch {
            repo.setSourcesEnabled(
                if (enabled) sources + "reddit" else sources - "reddit"
            )
        }
    }
    EditorialRule()
    ToggleRow(label = "Hacker News", description = "Tech-focused discussion", enabled = "hn" in sources) { enabled ->
        scope.launch {
            repo.setSourcesEnabled(
                if (enabled) sources + "hn" else sources - "hn"
            )
        }
    }
    EditorialRule()
    ToggleRow(label = "X.com", description = "Real-time takes — requires bearer token", enabled = "x" in sources) { enabled ->
        scope.launch {
            repo.setSourcesEnabled(
                if (enabled) sources + "x" else sources - "x"
            )
        }
    }
    EditorialRule()

    // X.com bearer token (only if X is enabled)
    if (settings.xEnabled) {
        Spacer(Modifier.height(8.dp))
        Text(
            "X.com bearer token",
            style = EssentiaTypography.bodyLarge,
            color = InkBlack
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "From the X developer portal (developer.x.com). Free tier allows 100 reads/month.",
            style = EssentiaTypography.bodySmall,
            color = InkMuted
        )
        Spacer(Modifier.height(8.dp))
        val placeholderX = settings.xBearerToken?.let { "•••••••••••••••" } ?: ""
        var tokenField by remember { mutableStateOf(placeholderX) }
        LaunchedEffect(settings.xBearerToken) {
            tokenField = placeholderX
        }
        val isXPlaceholder = tokenField.startsWith("•")
        EditableSecretField(
            value = tokenField,
            isPlaceholder = isXPlaceholder,
            onValueChange = { tokenField = it },
            placeholder = "AAAAAAAAAAAAAAAAAAAAA..."
        )
        Spacer(Modifier.height(8.dp))
        SecondaryButton(text = "Save Token", onClick = {
            if (!isXPlaceholder && tokenField.isNotBlank()) {
                scope.launch { repo.setXBearerToken(tokenField.trim()) }
            }
        }, enabled = !isXPlaceholder && tokenField.isNotBlank())
    }
}

@Composable
private fun MaintenanceSection() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val engine = remember { AppModule.digestEngine }
    val state by engine.state.collectAsState()
    var localError by remember { mutableStateOf<String?>(null) }

    SectionLabel("MAINTENANCE")
    Spacer(Modifier.height(12.dp))
    val isLoading = state is DigestState.Loading
    AccentButton(
        text = if (isLoading) "Generating…" else "Run Digest Now",
        onClick = {
            scope.launch {
                localError = null
                AppModule.nav { it.navigate("today") }
                val result = engine.run()
                result.onFailure { err ->
                    localError = err.message
                    android.widget.Toast.makeText(context, err.message ?: "Failed", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        },
        enabled = !isLoading
    )
    if (isLoading) {
        Spacer(Modifier.height(8.dp))
        val loadingState = state as? DigestState.Loading
        Text(
            text = "Building digest for \"${loadingState?.topic ?: "..."}\"",
            style = EssentiaTypography.bodySmall,
            color = com.essentia.ui.theme.AnalysisBlue
        )
    }
    localError?.let { err ->
        Spacer(Modifier.height(4.dp))
        Text(
            text = err,
            style = EssentiaTypography.bodySmall,
            color = com.essentia.ui.theme.BreakingRed
        )
    }
    Spacer(Modifier.height(8.dp))
    SecondaryButton(
        text = "Reset Checkpoint",
        onClick = { /* TODO: reset SourceCheckpoint in DataStore */ }
    )
}

@Composable
private fun DiagnosticsEntry(onClick: () -> Unit) {
    SectionLabel("DIAGNOSTICS")
    Spacer(Modifier.height(12.dp))
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.BugReport,
                contentDescription = null,
                tint = InkBlack,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Crash Logs", style = EssentiaTypography.bodyLarge, color = InkBlack)
                Text(
                    "View, share, or clear diagnostic reports",
                    style = EssentiaTypography.bodySmall,
                    color = InkMuted
                )
            }
        }
        Text("→", style = EssentiaTypography.titleLarge, color = InkMuted)
    }
    EditorialRule()
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = EssentiaTypography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 10.sp),
        color = InkMuted
    )
}

@Composable
private fun ToggleRow(
    label: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onToggle(!enabled) }
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = EssentiaTypography.bodyLarge, color = InkBlack)
            Text(description, style = EssentiaTypography.bodySmall, color = InkMuted)
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (enabled) InkBlack else Color(0xFFE0DACC)),
            contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PaperWhite)
            )
        }
    }
}

@Composable
private fun EditableSecretField(
    value: String,
    isPlaceholder: Boolean,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    var visible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            readOnly = isPlaceholder,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            textStyle = TextStyle(fontSize = 14.sp, color = if (isPlaceholder) InkMuted else InkBlack),
            placeholder = {
                Text(
                    text = placeholder,
                    style = TextStyle(fontSize = 14.sp, color = InkMuted)
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = InkBlack,
                unfocusedBorderColor = Color(0xFFE4E0D8),
                focusedContainerColor = Color(0xFFFAF8F4),
                unfocusedContainerColor = Color(0xFFF1EDE5),
                cursorColor = InkBlack,
                focusedTextColor = InkBlack,
                unfocusedTextColor = InkBlack,
                disabledTextColor = InkMuted,
                disabledBorderColor = Color(0xFFE4E0D8),
                disabledContainerColor = Color(0xFFEDE9E0)
            )
        )
        Text(
            text = if (visible) "HIDE" else "SHOW",
            style = EssentiaTypography.labelSmall.copy(letterSpacing = 1.5.sp, fontSize = 9.sp),
            color = InkMuted,
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { visible = !visible }
                )
                .padding(8.dp)
        )
    }
}

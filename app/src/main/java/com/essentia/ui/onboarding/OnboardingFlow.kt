package com.essentia.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.essentia.ui.components.AccentButton
import com.essentia.ui.components.SecondaryButton
import com.essentia.ui.theme.EssentiaTypography
import com.essentia.ui.theme.InkBlack
import com.essentia.ui.theme.InkMuted
import com.essentia.ui.theme.InkSoft
import com.essentia.ui.theme.PaperWhite
import com.essentia.ui.theme.Rule
import kotlinx.coroutines.launch

enum class OnboardingStep { Welcome, Llm, Topic, Sources }

@Composable
fun OnboardingFlow(onFinished: () -> Unit = {}) {
    var step by remember { mutableStateOf(OnboardingStep.Welcome) }
    val direction = remember { mutableStateOf(1) } // 1 = forward, -1 = back
    val scope = rememberCoroutineScope()
    val settingsRepo = remember { com.essentia.AppModule.settingsRepository }
    val secureStore = remember { com.essentia.AppModule.secureStore }
    val userPrefs = remember { com.essentia.AppModule.userPreferences }

    fun go(next: OnboardingStep) {
        direction.value = 1
        step = next
    }

    fun back(to: OnboardingStep) {
        direction.value = -1
        step = to
    }

    fun finishOnboarding() {
        scope.launch {
            userPrefs.setOnboarded(true)
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperWhite)
            .statusBarsPadding()
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val dir = direction.value
                if (dir >= 0) {
                    (slideInHorizontally(tween(420)) { it / 3 } + fadeIn(tween(420)))
                        .togetherWith(slideOutHorizontally(tween(280)) { -it / 3 } + fadeOut(tween(280)))
                } else {
                    (slideInHorizontally(tween(420)) { -it / 3 } + fadeIn(tween(420)))
                        .togetherWith(slideOutHorizontally(tween(280)) { it / 3 } + fadeOut(tween(280)))
                }
            },
            label = "onboarding-step"
        ) { current ->
            when (current) {
                OnboardingStep.Welcome -> WelcomeStep(onContinue = { go(OnboardingStep.Llm) })
                OnboardingStep.Llm -> LlmStep(
                    onBack = { back(OnboardingStep.Welcome) },
                    onContinue = { go(OnboardingStep.Topic) }
                )
                OnboardingStep.Topic -> TopicStep(
                    onBack = { back(OnboardingStep.Llm) },
                    onContinue = { go(OnboardingStep.Sources) }
                )
                OnboardingStep.Sources -> SourcesStep(
                    onBack = { back(OnboardingStep.Topic) },
                    onFinish = { finishOnboarding() }
                )
            }
        }
    }
}

// --- STEP 1: WELCOME ---

@Composable
private fun WelcomeStep(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        // Stacked cards illustration
        StackedCardsIllustration()

        Spacer(Modifier.height(48.dp))
        Text(
            text = "Essentia",
            style = EssentiaTypography.displayLarge,
            color = InkBlack
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 1.dp)
                .background(InkMuted)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Your daily digest, across\nthe sources you care about",
            style = EssentiaTypography.headlineMedium.copy(
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Normal
            ),
            color = InkBlack,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Finite reading for mindful minds",
            style = EssentiaTypography.bodyMedium.copy(
                fontStyle = FontStyle.Italic,
                color = InkMuted
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))
        AccentButton(
            text = "Get started",
            onClick = onContinue,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun StackedCardsIllustration() {
    Box(
        modifier = Modifier
            .size(width = 220.dp, height = 160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background card 2
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 24.dp, top = 16.dp)
                .size(width = 200.dp, height = 130.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFF1EDE5))
        )
        // Background card 1
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 8.dp)
                .size(width = 200.dp, height = 130.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFFFFFFF))
        )
        // Foreground card
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(width = 200.dp, height = 130.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(PaperWhite)
                .border(1.dp, Rule, RoundedCornerShape(2.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "ANALYSIS",
                    style = EssentiaTypography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF1F3A5F)
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(Color(0xFFE8E2D6))
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFFE0DACC))
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(6.dp)
                        .background(Color(0xFFE0DACC))
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(6.dp)
                    .background(Color(0xFFE0DACC))
                )
            }
        }
    }
}

// --- STEP 2: LLM ---

@Composable
private fun LlmStep(onBack: () -> Unit, onContinue: () -> Unit) {
    var selectedProvider by remember { mutableStateOf("OpenCode") }  // v1 only ships OpenCode
    var apiKey by remember { mutableStateOf("") }
    var verifying by remember { mutableStateOf(false) }
    var verified by remember { mutableStateOf(false) }
    var verifyError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val settingsRepo = remember { com.essentia.AppModule.settingsRepository }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        StepHeader(stepLabel = "ONBOARDING · STEP 02", headline = "Select Your Intelligence")
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Choose the model that will power your editorial experience.",
            style = EssentiaTypography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = InkMuted
        )
        Spacer(Modifier.height(24.dp))

        listOf(
            ProviderOption("OpenRouter", "Free tier available. Aggregates many models. Recommended for getting started.", true),
            ProviderOption("OpenAI", "GPT-4o, GPT-4o-mini, etc. Paid.", true),
            ProviderOption("Anthropic", "Claude 3.5, Claude 3.7, etc. Paid.", true),
            ProviderOption("Groq", "Fast inference. Free tier available.", true)
        ).forEach { option ->
            ProviderCard(
                option = option,
                selected = selectedProvider == option.name,
                onClick = { selectedProvider = option.name; verified = false; verifyError = null }
            )
            Spacer(Modifier.height(12.dp))
        }

        Column {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "PROVIDER API KEY",
                style = EssentiaTypography.labelSmall.copy(letterSpacing = 1.5.sp, fontSize = 10.sp),
                color = InkMuted
            )
            Spacer(Modifier.height(8.dp))
            ApiKeyField(
                value = apiKey,
                onValueChange = { apiKey = it; verified = false; verifyError = null },
                placeholder = "sk-•••••••••••••••••••••••••••••••"
            )
            Spacer(Modifier.height(12.dp))
            SecondaryButton(
                text = if (verified) "✓ Connection verified" else if (verifying) "Verifying…" else "Verify Connection",
                onClick = {
                    verifying = true
                    verifyError = null
                    scope.launch {
                        val result = runCatching {
                            com.essentia.llm.openai.OpenAiCompatibleProvider.testKey(
                                key = apiKey.trim(),
                                baseUrl = com.essentia.llm.openai.OpenAiCompatibleProvider.DEFAULT_BASE_URL,
                                model = com.essentia.llm.openai.OpenAiCompatibleProvider.DEFAULT_MODEL
                            )
                        }
                        verifying = false
                        result.onSuccess {
                            verified = true
                            settingsRepo.setProviderId("openai_compatible")
                            settingsRepo.setOpenaiCompatibleApiKey(apiKey.trim())
                        }
                        result.onFailure { err ->
                            verified = false
                            verifyError = err.message ?: "Verification failed"
                        }
                    }
                },
                enabled = apiKey.isNotBlank() && !verified && !verifying
            )
            verifyError?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
                    style = EssentiaTypography.bodySmall,
                    color = com.essentia.ui.theme.BreakingRed
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Your key is encrypted locally and never stored on our servers.",
                style = EssentiaTypography.bodySmall,
                color = InkMuted
            )
        }

        Spacer(Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                SecondaryButton(text = "Back", onClick = onBack)
            }
            Box(modifier = Modifier.weight(2f)) {
                AccentButton(
                    text = "Continue",
                    onClick = {
                        // Persist the key even if the user skipped verification.
                        if (apiKey.isNotBlank()) {
                            scope.launch {
                                settingsRepo.setProviderId("opencode")
                                settingsRepo.setOpenaiCompatibleApiKey(apiKey.trim())
                            }
                        }
                        onContinue()
                    },
                    enabled = true
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private data class ProviderOption(val name: String, val description: String, val external: Boolean)

@Composable
private fun ProviderCard(option: ProviderOption, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(PaperWhite)
            .border(
                width = 1.dp,
                color = if (selected) InkBlack else Rule,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.name,
                    style = EssentiaTypography.titleLarge.copy(fontSize = 22.sp),
                    color = InkBlack
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = option.description,
                    style = EssentiaTypography.bodyMedium,
                    color = InkSoft
                )
            }
            if (option.external) {
                Icon(
                    imageVector = Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = InkMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ApiKeyField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        placeholder = {
            Text(
                text = placeholder,
                style = TextStyle(fontSize = 14.sp, color = InkMuted)
            )
        },
        textStyle = TextStyle(fontSize = 14.sp, color = InkBlack),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp),
        shape = RoundedCornerShape(4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InkBlack,
            unfocusedBorderColor = Color(0xFFE4E0D8),
            focusedContainerColor = Color(0xFFFAF8F4),
            unfocusedContainerColor = Color(0xFFF1EDE5),
            cursorColor = InkBlack,
            focusedTextColor = InkBlack,
            unfocusedTextColor = InkBlack
        )
    )
}

// --- STEP 3: TOPIC ---

@Composable
private fun TopicStep(onBack: () -> Unit, onContinue: () -> Unit) {
    var topic by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val userPrefs = remember { com.essentia.AppModule.userPreferences }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        StepHeader(stepLabel = "ONBOARDING · STEP 03", headline = "What are you reading today?")
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = topic,
            onValueChange = { topic = it },
            singleLine = true,
            placeholder = {
                Text(
                    text = "e.g. AI agents",
                    style = EssentiaTypography.headlineSmall.copy(
                        fontSize = 20.sp,
                        color = InkMuted,
                        fontStyle = FontStyle.Italic
                    )
                )
            },
            textStyle = EssentiaTypography.headlineSmall.copy(
                fontSize = 20.sp,
                color = InkBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
            shape = RoundedCornerShape(4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = InkBlack,
                unfocusedBorderColor = Color(0xFFE4E0D8),
                focusedContainerColor = Color(0xFFFAF8F4),
                unfocusedContainerColor = Color(0xFFF1EDE5),
                cursorColor = InkBlack,
                focusedTextColor = InkBlack,
                unfocusedTextColor = InkBlack
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Add more later from the menu",
            style = EssentiaTypography.bodySmall.copy(fontStyle = FontStyle.Italic),
            color = InkMuted
        )
        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFF1EDE5))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "● ENGINE ACTIVE",
                    style = EssentiaTypography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF1F3A5F)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Passing query to LLM: ",
                    style = EssentiaTypography.bodyMedium,
                    color = InkSoft
                )
                Text(
                    text = "[ ${if (topic.isBlank()) "topic" else topic} ]",
                    style = EssentiaTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = InkBlack
                )
                Spacer(Modifier.height(12.dp))
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (i == 0) 1f else if (i == 1) 0.92f else 0.78f)
                            .height(8.dp)
                            .background(Color(0xFFE0DACC))
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                SecondaryButton(text = "Back", onClick = onBack)
            }
            Box(modifier = Modifier.weight(2f)) {
                AccentButton(
                    text = "Continue",
                    onClick = {
                        val cleaned = topic.trim()
                        if (cleaned.isNotEmpty()) {
                            scope.launch { userPrefs.addTopic(cleaned) }
                        }
                        onContinue()
                    },
                    enabled = true
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// --- STEP 4: SOURCES ---

@Composable
private fun SourcesStep(onBack: () -> Unit, onFinish: () -> Unit) {
    // Per the design doc: all three start in the same neutral "off" state.
    val sources = remember {
        mutableStateOf(
            listOf(
                SourceToggle("Reddit", "reddit", "Top posts from your subs", "COMMUNITY INTELLIGENCE", false),
                SourceToggle("Hacker News", "hn", "Tech-focused discussion", "SIGNAL PROCESSING", false),
                SourceToggle("X.com", "x", "Real-time takes - requires bearer token", "REAL-TIME VELOCITY", false)
            )
        )
    }
    val scope = rememberCoroutineScope()
    val userPrefs = remember { com.essentia.AppModule.userPreferences }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        StepHeader(stepLabel = "ONBOARDING · STEP 04", headline = "Pick Your Sources")
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Select the streams that will feed your personalised intelligence briefing. Each source is parsed with high-density accuracy.",
            style = EssentiaTypography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = InkMuted
        )
        Spacer(Modifier.height(24.dp))

        sources.value.forEachIndexed { idx, source ->
            SourceToggleCard(
                source = source,
                onToggle = {
                    val updated = sources.value.toMutableList()
                    updated[idx] = source.copy(enabled = !source.enabled)
                    sources.value = updated
                }
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.weight(1f))

        AccentButton(
            text = "Finalize Connections",
            onClick = {
                val enabled = sources.value.filter { it.enabled }.map { it.id }.toSet()
                scope.launch { userPrefs.setSourcesEnabled(enabled) }
                onFinish()
            }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "YOUR SELECTION WILL BE INDEXED INSTANTLY",
            style = EssentiaTypography.labelSmall.copy(
                fontSize = 9.sp,
                letterSpacing = 1.5.sp
            ),
            color = InkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
    }
}

private data class SourceToggle(
    val name: String,
    val id: String,
    val description: String,
    val tag: String,
    val enabled: Boolean
)

@Composable
private fun SourceToggleCard(source: SourceToggle, onToggle: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(PaperWhite)
            .border(
                width = if (source.enabled) 1.5.dp else 1.dp,
                color = if (source.enabled) Color(0xFFB8410F) else Rule,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = source.name,
                    style = EssentiaTypography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = InkBlack
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = source.description.uppercase(),
                    style = EssentiaTypography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    ),
                    color = InkSoft
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(width = 10.dp, height = 1.dp)
                            .background(InkMuted)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = source.tag,
                        style = EssentiaTypography.labelSmall.copy(
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        ),
                        color = InkMuted
                    )
                }
            }
            // Toggle
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 26.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (source.enabled) Color(0xFFB8410F) else Color(0xFFE0DACC)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 3.dp)
                        .size(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PaperWhite)
                )
            }
        }
    }
}

@Composable
private fun StepHeader(stepLabel: String, headline: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stepLabel,
            style = EssentiaTypography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 2.sp
            ),
            color = Color(0xFF1F3A5F)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = headline,
            style = EssentiaTypography.displaySmall.copy(
                fontSize = 32.sp,
                lineHeight = 38.sp
            ),
            color = InkBlack,
            textAlign = TextAlign.Center
        )
    }
}

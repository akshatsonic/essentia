package com.essentia.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = InkBlack,
    onPrimary = PaperWhite,
    primaryContainer = InkBlack,
    onPrimaryContainer = PaperWhite,

    secondary = Accent,
    onSecondary = PaperWhite,
    secondaryContainer = AccentSoft,
    onSecondaryContainer = Accent,

    tertiary = AnalysisBlue,
    onTertiary = PaperWhite,

    background = PaperWhite,
    onBackground = InkBlack,

    surface = Surface,
    onSurface = InkBlack,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = InkSoft,

    outline = Rule,
    outlineVariant = Rule,

    error = BreakingRed,
    onError = PaperWhite
)

@Composable
fun EssentiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColors // v1: paper-white light only

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // Defensive: view.context may be a ContextWrapper in some configurations.
            // Walk up the wrapper chain to find the Activity, then guard every window call.
            val activity = runCatching {
                var ctx = view.context
                while (ctx is android.content.ContextWrapper && ctx !is Activity) {
                    ctx = ctx.baseContext
                }
                ctx as? Activity
            }.getOrNull()
            if (activity != null) {
                runCatching {
                    val window = activity.window
                    window.statusBarColor = colorScheme.background.toArgb()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.navigationBarColor = colorScheme.background.toArgb()
                    }
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = true
                        isAppearanceLightNavigationBars = true
                    }
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EssentiaTypography,
        content = content
    )
}

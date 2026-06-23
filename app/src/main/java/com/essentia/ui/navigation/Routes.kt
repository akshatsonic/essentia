package com.essentia.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.essentia.ui.archive.ArchiveScreen
import com.essentia.ui.detail.DetailScreen
import com.essentia.ui.diagnostics.DiagnosticsScreen
import com.essentia.ui.editions.EditionsScreen
import com.essentia.ui.editions.PastEditionScreen
import com.essentia.ui.loading.LoadingScreen
import com.essentia.ui.onboarding.OnboardingFlow
import com.essentia.ui.settings.SettingsScreen
import com.essentia.ui.today.TodayScreen
import com.essentia.ui.topics.TopicsScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val LOADING = "loading"

    const val MAIN = "main"
    const val TODAY = "today"
    const val EDITIONS = "editions"
    const val ARCHIVE = "archive"

    const val DETAIL = "detail/{itemId}"
    const val PAST_EDITION = "edition/{editionId}"
    const val TOPICS = "topics"
    const val SETTINGS = "settings"
    const val DIAGNOSTICS = "diagnostics"

    fun detail(id: String) = "detail/$id"
    fun pastEdition(id: String) = "edition/$id"
}

private const val ENTER_MS = 380
private const val EXIT_MS = 300

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInFromRight(): EnterTransition =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ENTER_MS)) +
    fadeIn(tween(ENTER_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutToLeft(): ExitTransition =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(EXIT_MS)) +
    fadeOut(tween(EXIT_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInFromLeft(): EnterTransition =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ENTER_MS)) +
    fadeIn(tween(ENTER_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutToRight(): ExitTransition =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(EXIT_MS)) +
    fadeOut(tween(EXIT_MS))

fun NavGraphBuilder.mainGraph(
    onOpenItem: (String) -> Unit,
    onOpenEdition: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTopics: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    navigation(
        startDestination = Routes.TODAY,
        route = Routes.MAIN
    ) {
        composable(
            Routes.TODAY,
            enterTransition = { fadeIn(tween(ENTER_MS)) },
            exitTransition = { fadeOut(tween(EXIT_MS)) }
        ) {
            TodayScreen(
                onItemClick = onOpenItem,
                onSettingsClick = onOpenSettings
            )
        }
        composable(
            Routes.EDITIONS,
            enterTransition = { fadeIn(tween(ENTER_MS)) },
            exitTransition = { fadeOut(tween(EXIT_MS)) }
        ) {
            EditionsScreen(onEditionClick = onOpenEdition)
        }
        composable(
            Routes.ARCHIVE,
            enterTransition = { fadeIn(tween(ENTER_MS)) },
            exitTransition = { fadeOut(tween(EXIT_MS)) }
        ) {
            ArchiveScreen(onItemClick = onOpenItem)
        }
        composable(
            Routes.DETAIL,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ENTER_MS)) +
                fadeIn(tween(ENTER_MS))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(EXIT_MS)) +
                fadeOut(tween(EXIT_MS))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ENTER_MS)) +
                fadeIn(tween(ENTER_MS))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(EXIT_MS)) +
                fadeOut(tween(EXIT_MS))
            }
        ) { entry ->
            val id = entry.arguments?.getString("itemId") ?: return@composable
            DetailScreen(itemId = id)
        }
        composable(
            Routes.PAST_EDITION,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ENTER_MS)) +
                fadeIn(tween(ENTER_MS))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(EXIT_MS)) +
                fadeOut(tween(EXIT_MS))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ENTER_MS)) +
                fadeIn(tween(ENTER_MS))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(EXIT_MS)) +
                fadeOut(tween(EXIT_MS))
            }
        ) { entry ->
            val id = entry.arguments?.getString("editionId") ?: return@composable
            PastEditionScreen(editionId = id, onItemClick = onOpenItem)
        }
        composable(
            Routes.TOPICS,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ENTER_MS)) +
                fadeIn(tween(ENTER_MS))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(EXIT_MS)) +
                fadeOut(tween(EXIT_MS))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ENTER_MS)) +
                fadeIn(tween(ENTER_MS))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(EXIT_MS)) +
                fadeOut(tween(EXIT_MS))
            }
        ) {
            TopicsScreen()
        }
        composable(
            Routes.SETTINGS,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ENTER_MS)) +
                fadeIn(tween(ENTER_MS))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(EXIT_MS)) +
                fadeOut(tween(EXIT_MS))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ENTER_MS)) +
                fadeIn(tween(ENTER_MS))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(EXIT_MS)) +
                fadeOut(tween(EXIT_MS))
            }
        ) {
            SettingsScreen(onOpenDiagnostics = onOpenDiagnostics)
        }
        composable(
            Routes.DIAGNOSTICS,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ENTER_MS)) +
                fadeIn(tween(ENTER_MS))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(EXIT_MS)) +
                fadeOut(tween(EXIT_MS))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ENTER_MS)) +
                fadeIn(tween(ENTER_MS))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(EXIT_MS)) +
                fadeOut(tween(EXIT_MS))
            }
        ) {
            DiagnosticsScreen()
        }
    }
}

fun NavGraphBuilder.onboardingGraph(
    onFinished: () -> Unit = {}
) {
    composable(
        Routes.ONBOARDING,
        enterTransition = { fadeIn(tween(500)) },
        exitTransition = { fadeOut(tween(500)) }
    ) {
        OnboardingFlow(onFinished = onFinished)
    }
    composable(
        Routes.LOADING,
        enterTransition = { fadeIn(tween(500)) },
        exitTransition = { fadeOut(tween(500)) }
    ) {
        LoadingScreen()
    }
}

package com.rudra.defineeasy.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.work.HiltWorkerFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import com.rudra.defineeasy.core.analytics.AnalyticsService
import com.rudra.defineeasy.feature_dictionary.presentation.screens.CollectionWordsScreenRoute
import com.rudra.defineeasy.feature_dictionary.presentation.screens.CollectionsScreen
import com.rudra.defineeasy.feature_dictionary.presentation.screens.FavoritesScreen
import com.rudra.defineeasy.feature_dictionary.presentation.screens.ProgressScreen
import com.rudra.defineeasy.feature_dictionary.presentation.screens.QuizScreen
import com.rudra.defineeasy.feature_dictionary.presentation.screens.ReviewScreen
import com.rudra.defineeasy.feature_dictionary.presentation.screens.SearchScreen
import com.rudra.defineeasy.feature_dictionary.presentation.screens.WordDetailScreenRoute
import com.rudra.defineeasy.settings.SettingsScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DefineEasyNavGraph(
    navController: NavHostController,
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    val analytics = rememberAnalyticsService()
    NavHost(
        navController = navController,
        startDestination = DefineEasyDestination.Search.route,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it / 6 }) + fadeIn()
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it / 8 }) + fadeOut()
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it / 6 }) + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it / 8 }) + fadeOut()
        }
    ) {
        composable(route = DefineEasyDestination.Search.route) {
            LaunchedEffect(Unit) { analytics.trackScreen("Search") }
            SearchScreen(
                onWordSelected = { word ->
                    navController.navigate(DefineEasyDestination.WordDetail.createRoute(word))
                },
                contentPadding = contentPadding,
                onOpenSettings = {
                    navController.navigate(DefineEasyDestination.Settings.route)
                },
                onOpenReview = {
                    navController.navigate(DefineEasyDestination.Review.route)
                },
                onOpenQuiz = {
                    navController.navigate(DefineEasyDestination.Quiz.route)
                }
            )
        }
        composable(route = DefineEasyDestination.Favorites.route) {
            LaunchedEffect(Unit) { analytics.trackScreen("Favorites") }
            FavoritesScreen(
                onNavigateUp = { navController.navigateUp() },
                onWordSelected = { word ->
                    navController.navigate(DefineEasyDestination.WordDetail.createRoute(word))
                }
            )
        }
        composable(route = DefineEasyDestination.Review.route) {
            LaunchedEffect(Unit) { analytics.trackScreen("Review") }
            ReviewScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = DefineEasyDestination.Progress.route) {
            LaunchedEffect(Unit) { analytics.trackScreen("Progress") }
            ProgressScreen()
        }
        composable(route = DefineEasyDestination.Collections.route) {
            LaunchedEffect(Unit) { analytics.trackScreen("Collections") }
            CollectionsScreen(
                onCollectionSelected = { collectionId ->
                    navController.navigate(DefineEasyDestination.CollectionWords.createRoute(collectionId))
                }
            )
        }
        composable(route = DefineEasyDestination.Settings.route) {
            LaunchedEffect(Unit) {
                analytics.trackScreen("Settings")
                analytics.onSettingsOpened()
            }
            SettingsScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = DefineEasyDestination.Quiz.route) {
            LaunchedEffect(Unit) { analytics.trackScreen("Quiz") }
            QuizScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(
            route = DefineEasyDestination.CollectionWords.route,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.StringType }
            )
        ) {
            LaunchedEffect(Unit) { analytics.trackScreen("CollectionWords") }
            CollectionWordsScreenRoute(
                onNavigateUp = { navController.navigateUp() },
                onWordSelected = { word ->
                    navController.navigate(DefineEasyDestination.WordDetail.createRoute(word))
                }
            )
        }
        composable(
            route = DefineEasyDestination.WordDetail.route,
            arguments = listOf(
                navArgument("word") { type = NavType.StringType }
            )
        ) {
            LaunchedEffect(Unit) { analytics.trackScreen("WordDetail") }
            WordDetailScreenRoute(
                onNavigateUp = { navController.navigateUp() },
                onWordSelected = { word ->
                    navController.navigate(DefineEasyDestination.WordDetail.createRoute(word))
                }
            )
        }
    }
}

/**
 * Retrieves the [AnalyticsService] from the Hilt-injected Application instance.
 */
@Composable
private fun rememberAnalyticsService(): AnalyticsService {
    val context = LocalContext.current
    val app = context.applicationContext as com.rudra.defineeasy.DictionaryApp
    return app.analyticsService
}

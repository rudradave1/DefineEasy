package com.rudra.defineeasy.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import com.rudra.defineeasy.feature_dictionary.presentation.screens.CollectionWordsScreenRoute
import com.rudra.defineeasy.feature_dictionary.presentation.screens.CollectionsScreen
import com.rudra.defineeasy.feature_dictionary.presentation.screens.FavoritesScreen
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
                }
            )
        }
        composable(route = DefineEasyDestination.Favorites.route) {
            FavoritesScreen(
                onNavigateUp = { navController.navigateUp() },
                onWordSelected = { word ->
                    navController.navigate(DefineEasyDestination.WordDetail.createRoute(word))
                }
            )
        }
        composable(route = DefineEasyDestination.Review.route) {
            ReviewScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = DefineEasyDestination.Collections.route) {
            CollectionsScreen(
                onCollectionSelected = { collectionId ->
                    navController.navigate(DefineEasyDestination.CollectionWords.createRoute(collectionId))
                }
            )
        }
        composable(route = DefineEasyDestination.Settings.route) {
            SettingsScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(
            route = DefineEasyDestination.CollectionWords.route,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.StringType }
            )
        ) {
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
            WordDetailScreenRoute(
                onNavigateUp = { navController.navigateUp() },
                onWordSelected = { word ->
                    navController.navigate(DefineEasyDestination.WordDetail.createRoute(word))
                }
            )
        }
    }
}

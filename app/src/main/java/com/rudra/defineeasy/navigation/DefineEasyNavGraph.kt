package com.rudra.defineeasy.navigation

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

@Composable
fun DefineEasyNavGraph(
    navController: NavHostController,
    contentPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = DefineEasyDestination.Search.route
    ) {
        composable(route = DefineEasyDestination.Search.route) {
            SearchScreen(
                onWordSelected = { word ->
                    navController.navigate(DefineEasyDestination.WordDetail.createRoute(word))
                },
                contentPadding = contentPadding,
                onOpenSettings = {
                    navController.navigate(DefineEasyDestination.Settings.route)
                }
            )
        }
        composable(route = DefineEasyDestination.Favorites.route) {
            FavoritesScreen(
                onWordSelected = { word ->
                    navController.navigate(DefineEasyDestination.WordDetail.createRoute(word))
                }
            )
        }
        composable(route = DefineEasyDestination.Review.route) {
            ReviewScreen()
        }
        composable(route = DefineEasyDestination.Collections.route) {
            CollectionsScreen(
                onCollectionSelected = { collectionId ->
                    navController.navigate(DefineEasyDestination.CollectionWords.createRoute(collectionId))
                }
            )
        }
        composable(route = DefineEasyDestination.Settings.route) {
            SettingsScreen(onBackClick = navController::navigateUp)
        }
        composable(
            route = DefineEasyDestination.CollectionWords.route,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.StringType }
            )
        ) {
            CollectionWordsScreenRoute(
                onBackClick = navController::navigateUp,
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
                onBackClick = navController::navigateUp,
                onWordSelected = { word ->
                    navController.navigate(DefineEasyDestination.WordDetail.createRoute(word))
                }
            )
        }
    }
}

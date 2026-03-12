package com.rudra.defineeasy.navigation

import android.app.Activity
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import com.rudra.defineeasy.feature_dictionary.presentation.WordOfDayViewModel
import com.rudra.defineeasy.feature_dictionary.presentation.components.WordOfDayBottomSheet
import com.rudra.defineeasy.onboarding.OnboardingRoute
import com.rudra.defineeasy.onboarding.OnboardingViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefineEasyApp() {
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val onboardingState by onboardingViewModel.uiState.collectAsState()
    if (onboardingState.isLoading) {
        Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }
    if (!onboardingState.isCompleted) {
        OnboardingRoute(viewModel = onboardingViewModel)
        return
    }

    val context = LocalContext.current
    val navController = rememberNavController()
    val reviewBadgeViewModel: ReviewBadgeViewModel = hiltViewModel()
    val reviewDueCount by reviewBadgeViewModel.dueCount.collectAsState()
    val wordOfDayViewModel: WordOfDayViewModel = hiltViewModel()
    val wordOfDayState = wordOfDayViewModel.uiState.value
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination?.route != DefineEasyDestination.WordDetail.route

    LaunchedEffect(Unit) {
        val activity = context as? Activity ?: return@LaunchedEffect
        val requestedTab = activity.intent?.getStringExtra(com.rudra.defineeasy.MainActivity.EXTRA_OPEN_TAB)
        if (requestedTab == com.rudra.defineeasy.MainActivity.TAB_REVIEW) {
            navController.navigate(DefineEasyDestination.Review.route) {
                launchSingleTop = true
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
            }
            activity.intent?.removeExtra(com.rudra.defineeasy.MainActivity.EXTRA_OPEN_TAB)
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (destination.supportsBadge && reviewDueCount > 0) {
                                            Badge {
                                                Text(text = reviewDueCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = stringResource(destination.labelRes)
                                    )
                                }
                            },
                            label = {
                                Text(text = stringResource(destination.labelRes))
                            },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        DefineEasyNavGraph(
            navController = navController,
            contentPadding = paddingValues
        )
        if (wordOfDayState.isVisible && wordOfDayState.wordOfDay != null) {
            WordOfDayBottomSheet(
                wordOfDay = wordOfDayState.wordOfDay,
                isFavorited = wordOfDayState.isFavorited,
                onDismiss = wordOfDayViewModel::dismiss,
                onToggleFavorite = wordOfDayViewModel::toggleFavorite
            )
        }
    }
}

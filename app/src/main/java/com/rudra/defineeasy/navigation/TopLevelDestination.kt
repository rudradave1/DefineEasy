package com.rudra.defineeasy.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.rudra.defineeasy.R

enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int,
    val supportsBadge: Boolean = false
) {
    Search(
        route = "search",
        icon = Icons.Default.Search,
        labelRes = R.string.search_tab
    ),
    Favorites(
        route = "favorites",
        icon = Icons.Default.Favorite,
        labelRes = R.string.favorites_title
    ),
    Review(
        route = "review",
        icon = Icons.Default.School,
        labelRes = R.string.review_tab,
        supportsBadge = true
    ),
    Collections(
        route = "collections",
        icon = Icons.AutoMirrored.Filled.LibraryBooks,
        labelRes = R.string.collections_tab
    )
}

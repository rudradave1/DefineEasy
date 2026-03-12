package com.rudra.defineeasy.navigation

import android.net.Uri

sealed class DefineEasyDestination(val route: String) {
    data object Search : DefineEasyDestination("search")
    data object Favorites : DefineEasyDestination("favorites")
    data object Review : DefineEasyDestination("review")
    data object Collections : DefineEasyDestination("collections")
    data object Settings : DefineEasyDestination("settings")
    data object CollectionWords : DefineEasyDestination("collections/{collectionId}") {
        fun createRoute(collectionId: String): String {
            return "collections/${Uri.encode(collectionId)}"
        }
    }
    data object WordDetail : DefineEasyDestination("word_detail/{word}") {
        fun createRoute(word: String): String {
            return "word_detail/${Uri.encode(word)}"
        }
    }
}

package com.rudra.defineeasy.feature_dictionary.presentation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.ui.graphics.vector.ImageVector
import com.rudra.defineeasy.R
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionIds

data class CollectionUiMetadata(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val icon: ImageVector
)

fun collectionUiMetadata(collectionId: String): CollectionUiMetadata {
    return when (collectionId) {
        CollectionIds.UPSC -> CollectionUiMetadata(
            titleRes = R.string.collection_upsc_title,
            descriptionRes = R.string.collection_upsc_description,
            icon = Icons.Filled.School
        )
        CollectionIds.CAT -> CollectionUiMetadata(
            titleRes = R.string.collection_cat_title,
            descriptionRes = R.string.collection_cat_description,
            icon = Icons.Filled.Workspaces
        )
        CollectionIds.BUSINESS -> CollectionUiMetadata(
            titleRes = R.string.collection_business_title,
            descriptionRes = R.string.collection_business_description,
            icon = Icons.Filled.BusinessCenter
        )
        CollectionIds.CONFUSED -> CollectionUiMetadata(
            titleRes = R.string.collection_confused_title,
            descriptionRes = R.string.collection_confused_description,
            icon = Icons.AutoMirrored.Filled.CompareArrows
        )
        else -> CollectionUiMetadata(
            titleRes = R.string.collections_tab,
            descriptionRes = R.string.coming_soon,
            icon = Icons.Filled.Workspaces
        )
    }
}

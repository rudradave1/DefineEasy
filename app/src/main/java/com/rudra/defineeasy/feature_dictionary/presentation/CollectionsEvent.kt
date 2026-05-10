package com.rudra.defineeasy.feature_dictionary.presentation

sealed interface CollectionsEvent {
    data class CreateCollection(val name: String) : CollectionsEvent
    data class DeleteCollection(val id: Int) : CollectionsEvent
}

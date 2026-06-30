package com.rudra.defineeasy.feature_dictionary.domain.quiz

import com.rudra.defineeasy.feature_dictionary.domain.model.QuizQuestion
import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizGenerator @Inject constructor() {

    fun generateQuestions(
        dueWords: List<WordInfo>,
        recentSearches: List<WordInfo>,
        allWords: List<WordInfo>,
        questionCount: Int = 5
    ): List<QuizQuestion> {
        val candidateWords = mutableListOf<WordInfo>()
        candidateWords.addAll(dueWords.shuffled().take(2))
        candidateWords.addAll(recentSearches.shuffled().take(2))

        val remaining = allWords.filter { it.word !in candidateWords.map { w -> w.word } }
        candidateWords.addAll(remaining.shuffled().take(maxOf(0, questionCount - candidateWords.size)))

        val selectedWords = candidateWords.shuffled().take(questionCount)
        if (selectedWords.isEmpty()) return emptyList()

        val allDefinitions = allWords
            .flatMap { word ->
                word.meanings.flatMap { meaning ->
                    meaning.definitions.map { def ->
                        def.definition
                    }
                }
            }
            .filter { it.isNotBlank() }
            .distinct()

        return selectedWords.mapNotNull { wordInfo ->
            val correctDef = wordInfo.meanings
                .flatMap { it.definitions }
                .firstOrNull()?.definition
                ?: return@mapNotNull null

            val otherDefs = allDefinitions
                .filter { it != correctDef }
                .shuffled()
                .take(3)

            val options = (otherDefs + correctDef).shuffled()
            val correctIndex = options.indexOf(correctDef)

            QuizQuestion(
                word = wordInfo.word,
                correctDefinition = correctDef,
                options = options,
                correctIndex = correctIndex
            )
        }
    }
}

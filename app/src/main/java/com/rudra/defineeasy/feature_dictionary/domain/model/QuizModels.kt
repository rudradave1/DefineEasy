package com.rudra.defineeasy.feature_dictionary.domain.model

data class QuizQuestion(
    val word: String,
    val correctDefinition: String,
    val options: List<String>,
    val correctIndex: Int
)

data class QuizState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val score: Int = 0,
    val answeredIndex: Int? = null,
    val isCorrect: Boolean? = null,
    val isComplete: Boolean = false,
    val isLoading: Boolean = true
) {
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentIndex)

    val totalQuestions: Int
        get() = questions.size

    val progress: Float
        get() = if (totalQuestions > 0) currentIndex.toFloat() / totalQuestions.toFloat() else 0f
}

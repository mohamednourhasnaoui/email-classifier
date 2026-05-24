package com.example.emailclassifier.ml

import com.example.emailclassifier.data.ClassificationResult
import com.example.emailclassifier.data.LearningDataset
import kotlin.math.exp
import kotlin.math.ln

class EmailClassifier {

    private val categories = listOf("Spam", "Work", "Personal")
    private val trainingExamples = LearningDataset.examples

    private val stopWords = setOf(
        "the",
        "and",
        "you",
        "your",
        "are",
        "for",
        "this",
        "that",
        "with",
        "have",
        "has",
        "was",
        "were",
        "from",
        "will",
        "can",
        "our",
        "now",
        "get",
        "let",
        "please",
        "before",
        "after",
        "about",
        "into",
        "when",
        "where",
        "what",
        "how"
    )

    private val vocabulary: Set<String>
    private val wordCountsByCategory: Map<String, Map<String, Int>>
    private val totalWordsByCategory: Map<String, Int>
    private val exampleCountsByCategory: Map<String, Int>
    private val totalExamples: Int

    init {
        val vocabularyBuilder = mutableSetOf<String>()
        val wordCountMap = mutableMapOf<String, MutableMap<String, Int>>()
        val totalWordMap = mutableMapOf<String, Int>()
        val exampleCountMap = mutableMapOf<String, Int>()

        categories.forEach { category ->
            wordCountMap[category] = mutableMapOf()
            totalWordMap[category] = 0
            exampleCountMap[category] = 0
        }

        trainingExamples.forEach { example ->
            if (categories.contains(example.category)) {
                val words = tokenize(example.text)

                exampleCountMap[example.category] =
                    exampleCountMap.getOrDefault(example.category, 0) + 1

                words.forEach { word ->
                    vocabularyBuilder.add(word)

                    val categoryWords = wordCountMap[example.category] ?: mutableMapOf()
                    categoryWords[word] = categoryWords.getOrDefault(word, 0) + 1
                    wordCountMap[example.category] = categoryWords

                    totalWordMap[example.category] =
                        totalWordMap.getOrDefault(example.category, 0) + 1
                }
            }
        }

        vocabulary = vocabularyBuilder
        wordCountsByCategory = wordCountMap
        totalWordsByCategory = totalWordMap
        exampleCountsByCategory = exampleCountMap
        totalExamples = exampleCountMap.values.sum()
    }

    fun classify(emailText: String): ClassificationResult {
        val words = tokenize(emailText)

        if (words.isEmpty()) {
            return junkResult()
        }

        if (vocabulary.isEmpty()) {
            return junkResult()
        }

        if (totalExamples == 0) {
            return junkResult()
        }

        val knownWords = words.filter { word ->
            vocabulary.contains(word)
        }

        if (knownWords.isEmpty()) {
            return junkResult()
        }

        val logScores = categories.associateWith { category ->
            calculateLogScore(
                category = category,
                words = knownWords
            )
        }

        val probabilityScores = convertLogScoresToPercentages(logScores)

        val spamScore = probabilityScores["Spam"] ?: 0
        val workScore = probabilityScores["Work"] ?: 0
        val personalScore = probabilityScores["Personal"] ?: 0

        val finalCategory = determineFinalCategory(
            spamScore = spamScore,
            workScore = workScore,
            personalScore = personalScore
        )

        return ClassificationResult(
            category = finalCategory,
            spamScore = spamScore,
            workScore = workScore,
            personalScore = personalScore
        )
    }

    private fun calculateLogScore(
        category: String,
        words: List<String>
    ): Double {
        val categoryExampleCount = exampleCountsByCategory[category] ?: 0

        if (categoryExampleCount == 0) {
            return Double.NEGATIVE_INFINITY
        }

        val priorProbability = categoryExampleCount.toDouble() / totalExamples.toDouble()
        var score = ln(priorProbability)

        val categoryWordCounts = wordCountsByCategory[category] ?: emptyMap()
        val totalCategoryWords = totalWordsByCategory[category] ?: 0
        val vocabularySize = vocabulary.size

        words.forEach { word ->
            val wordCount = categoryWordCounts[word] ?: 0

            val wordProbability =
                (wordCount + 1).toDouble() / (totalCategoryWords + vocabularySize).toDouble()

            score += ln(wordProbability)
        }

        return score
    }

    private fun convertLogScoresToPercentages(
        logScores: Map<String, Double>
    ): Map<String, Int> {
        val validScores = logScores.filterValues { value ->
            value.isFinite()
        }

        if (validScores.isEmpty()) {
            return mapOf(
                "Spam" to 0,
                "Work" to 0,
                "Personal" to 0
            )
        }

        val maxLogScore = validScores.values.maxOrNull() ?: 0.0

        val expScores = categories.associateWith { category ->
            val value = logScores[category] ?: Double.NEGATIVE_INFINITY

            if (value.isFinite()) {
                exp(value - maxLogScore)
            } else {
                0.0
            }
        }

        val total = expScores.values.sum()

        if (total == 0.0) {
            return mapOf(
                "Spam" to 0,
                "Work" to 0,
                "Personal" to 0
            )
        }

        return expScores.mapValues { entry ->
            ((entry.value / total) * 100).toInt()
        }
    }

    private fun determineFinalCategory(
        spamScore: Int,
        workScore: Int,
        personalScore: Int
    ): String {
        if (spamScore == 0 && workScore == 0 && personalScore == 0) {
            return "Junk Email"
        }

        if (spamScore == workScore && workScore == personalScore) {
            return "Junk Email"
        }

        val scores = mapOf(
            "Spam" to spamScore,
            "Work" to workScore,
            "Personal" to personalScore
        )

        val highestScore = scores.values.maxOrNull() ?: 0

        val winners = scores.filter { entry ->
            entry.value == highestScore
        }

        if (winners.size > 1) {
            return "Junk Email"
        }

        if (highestScore < 45) {
            return "Junk Email"
        }

        return winners.keys.first()
    }

    private fun junkResult(): ClassificationResult {
        return ClassificationResult(
            category = "Junk Email",
            spamScore = 0,
            workScore = 0,
            personalScore = 0
        )
    }

    private fun tokenize(text: String): List<String> {
        return text
            .lowercase()
            .replace(Regex("[^a-zA-Z ]"), " ")
            .split(Regex("\\s+"))
            .map { word ->
                word.trim()
            }
            .filter { word ->
                word.length > 2 && !stopWords.contains(word)
            }
    }
}
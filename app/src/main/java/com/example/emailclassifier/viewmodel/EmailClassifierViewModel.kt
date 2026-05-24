package com.example.emailclassifier.viewmodel

import androidx.lifecycle.ViewModel
import com.example.emailclassifier.data.ClassificationResult
import com.example.emailclassifier.data.LearningDataset
import com.example.emailclassifier.data.TrainingExample
import com.example.emailclassifier.ml.EmailClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EmailClassifierViewModel : ViewModel() {

    private val classifier = EmailClassifier()

    private val _classificationResult = MutableStateFlow<ClassificationResult?>(null)
    val classificationResult: StateFlow<ClassificationResult?> = _classificationResult

    private val _customText = MutableStateFlow("")
    val customText: StateFlow<String> = _customText

    private val _selectedDatasetCategory = MutableStateFlow<String?>(null)
    val selectedDatasetCategory: StateFlow<String?> = _selectedDatasetCategory

    private val _predictionHistory = MutableStateFlow<List<ClassificationResult>>(emptyList())
    val predictionHistory: StateFlow<List<ClassificationResult>> = _predictionHistory

    fun updateCustomText(text: String) {
        _customText.value = text
    }

    fun classifyCustomText() {
        val text = _customText.value.trim()

        if (text.isNotEmpty()) {
            val result = classifier.classify(text)
            _classificationResult.value = result
            _predictionHistory.value = listOf(result) + _predictionHistory.value
        }
    }

    fun clearInput() {
        _customText.value = ""
        _classificationResult.value = null
    }

    fun clearHistory() {
        _predictionHistory.value = emptyList()
    }

    fun loadSampleText(type: String) {
        _classificationResult.value = null

        _customText.value = when (type) {
            "Spam" -> "Congratulations, you won a free prize. Click here now to claim your reward and bonus."
            "Work" -> "Please send the project report before the meeting with the client tomorrow."
            "Personal" -> "Hey friend, are you coming to dinner with the family this weekend?"
            else -> "Blue window car table sky random object walking paper."
        }
    }

    fun openDatasetCategory(category: String) {
        _selectedDatasetCategory.value = category
    }

    fun closeDatasetCategory() {
        _selectedDatasetCategory.value = null
    }

    fun getExamplesForCategory(category: String): List<TrainingExample> {
        return LearningDataset.examples.filter { example ->
            example.category == category
        }
    }
}
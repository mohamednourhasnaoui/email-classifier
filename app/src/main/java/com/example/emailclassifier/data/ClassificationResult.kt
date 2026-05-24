package com.example.emailclassifier.data

data class ClassificationResult(
    val category: String,
    val spamScore: Int,
    val workScore: Int,
    val personalScore: Int
)
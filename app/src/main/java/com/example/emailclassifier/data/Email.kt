package com.example.emailclassifier.data

data class Email(
    val id: Int,
    val sender: String,
    val subject: String,
    val body: String
)
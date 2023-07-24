package com.example.jogoquizz

data class Question(
    val id: Int,
    val statement: String,
    val options: List<String>,
    val correctOptionIndex: Int
)

data class AnswerRequest(
    val answer: String
)


data class AnswerResponse(
    val result: Boolean
)


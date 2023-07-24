package com.example.jogoquizz.api

import com.example.jogoquizz.AnswerRequest
import com.example.jogoquizz.AnswerResponse
import com.example.jogoquizz.Question
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("/question")
    fun getQuestion(): Call<Question>

    @POST("/answer")
    fun submitAnswer(
        @Query("questionId") questionId: String,
        @Body answer: AnswerRequest
    ): Call<AnswerResponse>
}


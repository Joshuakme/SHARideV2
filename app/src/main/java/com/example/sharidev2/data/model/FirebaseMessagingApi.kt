package com.example.sharidev2.data.model

import com.example.sharidev2.data.dto.SendMessageDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FirebaseMessagingApi {
    @POST("/send")
    suspend fun sendMessage(
        @Body body: SendMessageDto
    )

    @POST("/broadcast")
    suspend fun broadcast(
        @Body body: SendMessageDto
    )
}

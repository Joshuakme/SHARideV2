package com.example.sharide.data.model

import com.example.sharide.data.dto.SendMessageDto
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

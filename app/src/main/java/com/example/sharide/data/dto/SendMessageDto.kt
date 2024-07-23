package com.example.sharide.data.dto

class SendMessageDto(
    val to: String?,
    val notification: NotificationBody
)

data class NotificationBody(
    val title: String,
    val body: String
)
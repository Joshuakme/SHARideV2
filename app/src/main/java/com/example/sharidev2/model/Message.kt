package com.example.sharidev2.model

import java.time.LocalDateTime

data class Message(
    val messageId: String,
    val sender: User,
    val content: String,
    val dateTime: LocalDateTime
)

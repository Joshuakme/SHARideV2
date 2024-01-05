package com.example.sharidev2.model

import java.time.LocalDateTime

data class Message(
    val messageID: String,
    val senderID: String,
    val text: String,
    val timestamp: Long,
    val attachmentURL: String?, // Nullable for text messages
    val readBy: List<String>,
    val messageType: MessageType
)

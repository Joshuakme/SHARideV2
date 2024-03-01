package com.example.sharidev2.data.model

import com.google.firebase.Timestamp

data class Message(
    val messageId: String,
    val senderId: String,
    val text: String,
    val timestamp: Timestamp? = null,
    val attachmentURL: String? = null, // Nullable for text messages
    val readBy: List<String>,
    val messageType: MessageType
)

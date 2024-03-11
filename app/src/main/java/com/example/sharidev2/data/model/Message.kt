package com.example.sharidev2.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val messageId: String,
    val senderId: String,
    val text: String,
    val timestamp: Timestamp? = null,
    val attachmentURL: String? = null, // Nullable for text messages
    val readBy: List<String>,
    val messageType: MessageType
): Parcelable

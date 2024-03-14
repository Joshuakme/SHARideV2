package com.example.sharidev2.data.model

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Message(
    val messageId: String? = UUID.randomUUID().toString(),
    var senderId: String? = null,
    var senderName: String? = null,
    val text: String,
    val timestamp: Timestamp? = Timestamp.now(),
    val attachmentURL: String? = null, // Nullable for text messages
    val readBy: List<String> = emptyList(),
    val messageType: MessageType = MessageType.Text,
    var photoUrl: Uri? = null
): Parcelable

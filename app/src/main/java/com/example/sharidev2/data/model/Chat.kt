package com.example.sharidev2.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chat (
    val chatId: String ?= null,
    val members: List<String>? = null,
    val lastMessage: String? = null,
    val timestamp: Timestamp? = null,
    var messages: MutableList<Message>? = mutableListOf(),
    val rideId: String? = null,
    var chatStatus: ChatStatus = ChatStatus.ACTIVE
) : Parcelable
package com.example.sharidev2.data.model

import com.google.firebase.Timestamp

data class Chat (
    val chatId: String ?= null,
    val members: List<String>? = null,
    val lastMessage: String? = null,
    val timestamp: Timestamp? = null,
    val typingUsers: List<String>? = null,
    val messages: MutableList<Message>? = mutableListOf()
) {

}
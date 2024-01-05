package com.example.sharidev2.model

data class Chat (
    val chatID: String ?= null,
    val members: List<String>,
    val lastMessage: String?,
    val timestamp: Long,
    val typingUsers: List<String>,
    val messages: MutableList<Message>
) {

}
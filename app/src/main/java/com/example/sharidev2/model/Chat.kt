package com.example.sharidev2.model

data class Chat (
    val chatID: String,
    val messages: MutableList<Message>
) {
    val participants: List<String?>
        get() = messages.flatMap { listOf(it.sender.uid) }.distinct()
}
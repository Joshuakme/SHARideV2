package com.example.sharide.data.model

enum class ChatStatus {
    ACTIVE, CLOSED;


    fun isChatActive(): Boolean {
        return this == ACTIVE
    }
}

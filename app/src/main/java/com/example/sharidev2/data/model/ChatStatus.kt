package com.example.sharidev2.data.model

enum class ChatStatus {
    ACTIVE, CLOSED;


    fun isChatActive(): Boolean {
        return this == ACTIVE
    }
}

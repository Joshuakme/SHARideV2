package com.example.sharidev2.utility

import com.example.sharidev2.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserClient {
    private var currentUser: User? = null

    suspend fun setCurrentUser(userUid: String) {
        withContext(Dispatchers.IO) {
            currentUser = FirebaseClient.getUserFromUid(userUid)
        }
    }

    fun currentUser(): User? {
        return currentUser
    }
}
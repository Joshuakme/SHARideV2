package com.example.sharidev2.service

import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.viewmodel.SharedCurrentUserViewModel
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await

class FcmNotificationService: FirebaseMessagingService() {
    val firestore = FirebaseClient.firestore

    override fun onNewToken(token: String) {
        super.onNewToken(token)


        // Update server
        sendTokenToServer(token)
    }
    private fun sendTokenToServer(token: String?) {
        // If you're running your own server, call API to send token and today's date for the user


//        if(currentUserViewModel.userUid.value != null) {
//            firestore.collection("user")
//                .document(currentUserViewModel.userUid.value!!)
//                .update("fcmToken", token)
//        }
    }
}
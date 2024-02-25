package com.example.sharidev2.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseInitializer {
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
}
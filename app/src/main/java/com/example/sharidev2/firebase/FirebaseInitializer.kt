package com.example.sharidev2.firebase

import com.example.sharidev2.utility.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseInitializer {
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val firebaseUtils: FirebaseUtils by lazy {
        FirebaseUtils(firestore, firebaseAuth)
    }
}
package com.example.sharidev2.data.model

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.IgnoreExtraProperties
import java.time.LocalDateTime

data class User(
    val uid: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: Uri? = null,
    val rideOption: RideOption? = null,
    val rating: Float ?= null,
    val savedAddresses: MutableList<SearchLocation> ?= null,
    val gender: Gender ?= null,
    val joinedDate: Timestamp? = null
) {
    companion object {
        fun fromFirebaseUser(firebaseUser: FirebaseUser): User {
            firebaseUser.apply {
                val uid = this.uid
                val displayName = this.displayName
                val email = this.email
                val phoneNumber = this.phoneNumber
                val photoUrl = this.photoUrl

                return User(uid, displayName, email, phoneNumber, photoUrl)
            }
        }
    }
}

package com.example.sharidev2.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.IgnoreExtraProperties
import java.time.LocalDateTime

@IgnoreExtraProperties
data class User @RequiresApi(Build.VERSION_CODES.O) constructor(
    val uid: String? = null,
    val username: String? = null,
    val email: String? = null,
    val specialAttribute: String? = null,
    val profilePictureUrl: String? = null,
    val rating: Float ?= null,
    val savedAddresses: MutableList<Address> ?= null,
    val gender: Gender ?= null,
    val joinedDate: LocalDateTime = LocalDateTime.now()
)

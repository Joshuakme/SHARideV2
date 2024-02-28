package com.example.sharidev2.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    val contactId: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val userUid: String? = null
) : Parcelable

package com.example.sharidev2.data.model

import java.util.Objects

data class FirebaseResponse<T>(
     val status: Int,
     val data: T? = null
) {

}
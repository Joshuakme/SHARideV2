package com.example.sharide.data.model

data class FirebaseResponse<T>(
     val status: Int,
     val data: T? = null
) {

}
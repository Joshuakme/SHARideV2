package com.example.sharide.data.model

import retrofit2.http.GET
import retrofit2.http.Query

interface DistanceMatrixApi {

    @GET("distancematrix/json")
    suspend fun getDistanceAndDuration(
        @Query("units") units: String,
        @Query("origins") origins: String,
        @Query("destinations") destinations: String,
        @Query("key") apiKey: String
    ): DistanceMatrixResponse

}


package com.example.sharide.data.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface PopularLocationApi {
    @GET("place/search/json")
    fun findPlaces(
        @Query("location") location: String?,
        @Query("radius") radius: Int,
        @Query("types") placeType: String?,
        @Query("sensor") sensor: Boolean,
        @Query("key") apiKey: String?
    ): Call<PopularLocationResponse?>?
}
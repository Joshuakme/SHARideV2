package com.example.sharidev2.utility

import android.content.Context
import android.util.Log
import com.example.sharidev2.R
import com.example.sharidev2.data.model.DistanceMatrixApi
import com.example.sharidev2.data.model.DistanceMatrixResponse
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.SearchLocation
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt

class FareUtils {
    private fun calculateFare(distanceInKm: Double, durationInMinute: Int): Int {
        return (Constants.RIDE_BASE_PRICE +
                (distanceInKm * Constants.RIDE_FARE_PER_KM) +
                (durationInMinute * Constants.RIDE_FARE_PER_MINUTE)
                ).roundToInt()
    }

    suspend fun calculatePassengerFare(context: Context, ride: Ride, passenger: Passenger) {
        val dmApi = DistanceMatrixAPiClient.create()



        // let ride origin = A
        // destination = B
        // stop1 = C

        // TODO: Calculate distance and duration of AC (origin-stop1)
        val origin = ride.origin.geolocation
        val destination = ride.destination.geolocation

        if(origin != null && destination != null) {
            try {
                val response = dmApi.getDistanceAndDuration(
                    units = "metric",
                    origins = "${origin.latitude},${origin.longitude}",
                    destinations = "${destination.latitude},${destination.longitude}",
                    apiKey = context.getString(R.string.google_api_key)
                )

                val fare = handleDistanceResponse(response)
            } catch (e: Exception) {
                // Handle exceptions, such as network errors
                println("Error occurred while fetching distance and duration: ${e.message}")
            }
        }




        // TODO: Calculate distance and duration of CD,DE,EF,etc.


        // TODO: Calculate distance and duration of FB(lastStop-destination, assume F is the last stop)
    }

    suspend fun calculatePassengerFare(context: Context, origin: SearchLocation, destination: SearchLocation, responseListener: OnDistanceResponseListener) {
        val dmApi = DistanceMatrixAPiClient.create()



        // let ride origin = A
        // destination = B
        // stop1 = C

        // TODO: Calculate distance and duration of AC (origin-stop1)
        try {
            val response = dmApi.getDistanceAndDuration(
                units = "metric",
                origins = origin.name,
                destinations = destination.name,
                apiKey = context.getString(R.string.google_api_key)
            )

            val fare = handleDistanceResponse(response)

            responseListener.onPriceCalculated(fare)
        } catch (e: Exception) {
            // Handle exceptions, such as network errors
            println("Error occurred while fetching distance and duration: ${e.message}")
        }




        // TODO: Calculate distance and duration of CD,DE,EF,etc.


        // TODO: Calculate distance and duration of FB(lastStop-destination, assume F is the last stop)
    }

    object DistanceMatrixAPiClient {
        private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

        fun create(): DistanceMatrixApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(DistanceMatrixApi::class.java)
        }
    }


    private fun handleDistanceResponse(response: DistanceMatrixResponse): Int {
        if (response.status == "OK") {
            val firstRow= response.rows.firstOrNull()
            if ((firstRow != null) && firstRow.elements.isNotEmpty()) {
                val firstElement = firstRow.elements.first()
                val distanceText = firstElement.distance.text
                val distanceValue = firstElement.distance.value
                val durationText = firstElement.duration.text
                val durationValue = firstElement.duration.value

                // Now you can use distanceText, distanceValue, durationText, and durationValue as needed
                Log.e("FareUtils: handleDistanceResponse", "Distance: $distanceText ($distanceValue meters)")
                Log.e("FareUtils: handleDistanceResponse", "Duration: $durationText ($durationValue seconds)")

                val distanceInKm = (distanceValue/1000.0 * 10.0).roundToInt() / 10.0
                val durationInMinute = (durationValue.toDouble() / 60.0).roundToInt()

                return calculateFare(distanceInKm, durationInMinute)
            } else {
                Log.e("FareUtils: handleDistanceResponse", "No elements found in the response.")
                return 0
            }
        } else {
            Log.e("FareUtils: handleDistanceResponse", "Response status: " + response.status)
            return 0
        }
    }



    interface OnDistanceResponseListener {
        fun onPriceCalculated(fare: Int)
    }
}
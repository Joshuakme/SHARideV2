package com.example.sharidev2.utility

import android.content.Context
import android.util.Log
import com.example.sharidev2.R
import com.example.sharidev2.data.model.DistanceMatrixApi
import com.example.sharidev2.data.model.DistanceMatrixResponse
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Ride
import com.google.android.gms.maps.model.LatLng
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class FareUtils {
    companion object {
        private val discountMap = mapOf(
            1 to Constants.RIDE_PASSENGER_DISCOUNT_PAX_ONE,
            2 to Constants.RIDE_PASSENGER_DISCOUNT_PAX_TWO,
            3 to Constants.RIDE_PASSENGER_DISCOUNT_PAX_THREE,
        )


        private fun calculateFare(distanceInKm: Double, durationInMinute: Int): Double {
            return (Constants.RIDE_BASE_PRICE +
                    (distanceInKm * Constants.RIDE_FARE_PER_KM) +
                    (durationInMinute * Constants.RIDE_FARE_PER_MINUTE)
                    )
        }

        suspend fun calculatePassengerFare(
            context: Context,
            ride: Ride,
            newPassenger: Passenger,
            pickupList: List<Passenger>,
            responseListener: OnDistanceResponseListener
        ) {
            // 1. Calculate price from previous location (origin / last passenger) to current passenger location (Detour price)
            // 2. Calculate price from current passenger location to destination (toDestination price)
            // 3. Sum the price
            // 4. Adjust the price based on number of passenger in the ride

            val origin = ride.origin
            val destination = ride.destination

            val passengerIndex = pickupList.indexOf(newPassenger)
            val discount =
                discountMap.getOrElse(pickupList.size) { Constants.RIDE_PASSENGER_DISCOUNT_PAX_FOUR_AND_MORE }


            try {
                if (newPassenger.origin!!.placeId == origin.placeId) {
                    // same origin, then calculate from origin-destination
                    val fare = calculateFareFromOriginToDestination(
                        context,
                        origin.name,
                        destination.name,
                        discount
                    )

                    responseListener.onPriceCalculated(fare)
                } else {
                    // Calculate distance and duration of AC (stopN-1-stopN, stopN-dest)
                    if (passengerIndex in 1..pickupList.lastIndex) {
                        val fare = calculateDetourFare(
                            context,
                            pickupList[passengerIndex - 1].origin!!.name,
                            pickupList[passengerIndex].origin!!.name,
                            destination.name,
                            discount
                        )

                        responseListener.onPriceCalculated(fare)

                    } else if (passengerIndex == 0) {
                        // Calculate distance and duration of AC (origin-stop1, stop1-dest)
                        val fare = calculateDetourFare(
                            context,
                            origin.name,
                            pickupList[passengerIndex].origin!!.name,
                            destination.name,
                            discount
                        )

                        responseListener.onPriceCalculated(fare)
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions, such as network errors
                println("Error occurred while fetching distance and duration: ${e.message}")
            }
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

        private fun handleDistanceResponse(response: DistanceMatrixResponse): Double {
            if (response.status == "OK") {
                val firstRow = response.rows.firstOrNull()
                if ((firstRow != null) && firstRow.elements.isNotEmpty()) {
                    val firstElement = firstRow.elements.first()
                    val distanceText = firstElement.distance.text
                    val distanceValue = firstElement.distance.value
                    val durationText = firstElement.duration.text
                    val durationValue = firstElement.duration.value

                    // Now you can use distanceText, distanceValue, durationText, and durationValue as needed
                    Log.e(
                        "FareUtils: handleDistanceResponse",
                        "Distance: $distanceText ($distanceValue meters)"
                    )
                    Log.e(
                        "FareUtils: handleDistanceResponse",
                        "Duration: $durationText ($durationValue seconds)"
                    )

                    val distanceInKm = (distanceValue / 1000.0 * 10.0).roundToInt() / 10.0
                    val durationInMinute = (durationValue.toDouble() / 60.0).roundToInt()

                    return calculateFare(distanceInKm, durationInMinute)
                } else {
                    Log.e("FareUtils: handleDistanceResponse", "No elements found in the response.")
                    return 0.0
                }
            } else {
                Log.e("FareUtils: handleDistanceResponse", "Response status: " + response.status)
                return 0.0
            }
        }

        fun calculateDistance(origin: LatLng, destination: LatLng): Double {
            val radius = 6371   // Earth radius in kilometers

            val lat1 = Math.toRadians(origin.latitude)
            val lon1 = Math.toRadians(origin.longitude)
            val lat2 = Math.toRadians(destination.latitude)
            val lon2 = Math.toRadians(destination.longitude)
            val dLat = lat2 - lat1
            val dLon = lon2 - lon1
            val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return radius * c
        }


        private suspend fun calculateDetourFare(
            context: Context,
            origin: String,
            stop: String,
            destination: String,
            discount: Double
        ): Double {
            val detourResponse = makeDistanceDurationRequest(context, origin, stop)
            val toDestinationResponse = makeDistanceDurationRequest(context, stop, destination)

            val detourFare = handleDistanceResponse(detourResponse)
            val toDestinationFare = handleDistanceResponse(toDestinationResponse) * discount

            return detourFare + toDestinationFare
        }

        private suspend fun calculateFareFromOriginToDestination(
            context: Context,
            origin: String,
            destination: String,
            discount: Double
        ): Double {
            val response = makeDistanceDurationRequest(context, origin, destination)
            return handleDistanceResponse(response) * discount
        }

        fun getSortedPassengerList(
            origin: LatLng,
            passengerList: MutableList<Passenger>
        ): List<Passenger> {
            var minDistance = Double.MAX_VALUE
            var nearestPassenger = Passenger()
            val clonedPassengerList = passengerList.toMutableList()
            val sortedList = mutableListOf<Passenger>()

            while (clonedPassengerList.isNotEmpty()) {
                for (passenger in clonedPassengerList) {
                    if (passenger.location != null) {
                        val distance = if (sortedList.isEmpty()) {
                            calculateDistance(origin, passenger.location)
                        } else {
                            calculateDistance(sortedList.last().location!!, passenger.location)
                        }

                        if (distance < minDistance) {
                            minDistance = distance
                            nearestPassenger = passenger
                        }
                    }
                }

                clonedPassengerList.remove(nearestPassenger)
                sortedList.add(nearestPassenger)
            }

            return sortedList
        }

        private suspend fun makeDistanceDurationRequest(
            context: Context,
            origin: String,
            destination: String
        ): DistanceMatrixResponse {
            val dmApi = DistanceMatrixAPiClient.create()

            return dmApi.getDistanceAndDuration(
                units = "metric",
                origins = origin,
                destinations = destination,
                apiKey = context.getString(R.string.google_map_key)
            )
        }

        interface OnDistanceResponseListener {
            fun onPriceCalculated(fare: Double)
        }
    }
}
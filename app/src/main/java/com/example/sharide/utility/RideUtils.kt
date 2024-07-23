package com.example.sharide.utility

import android.util.Log
import com.example.sharide.data.model.Passenger
import com.example.sharide.data.model.Ride
import com.example.sharide.data.model.SearchLocation
import com.example.sharide.data.model.SearchRide
import com.google.firebase.Timestamp
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class RideUtils {
    val ORIGIN_DETOUR_DISTANCE_IN_KM = 5.0
    val DESTINATION_DETOUR_DISTANCE_IN_KM = 2.0


    // TODO: modify the algorithm to calculate similarity score
    private fun calculateSimilarityScore(searchRide: SearchRide, ride: Ride): Double {
        // Assign weights to different criteria
        val exactLocationWeight = 5.0
        val partialLocationWeight = 3.0
        val datetimeWeight = 5.0
        val driverGenderWeight = 0.9
        val vehicleTypeWeight = 0.7
        val petFriendlyWeight = 0.7

        // Calculate individual scores for each criterion
        val locationScore =
            if (searchRide.origin.placeId == ride.origin.placeId && ride.destination.placeId == searchRide.destination.placeId) {
                exactLocationWeight
            } else if (isWithinRoute(
                    ride.origin.geolocation!!,
                    ride.destination.geolocation!!,
                    searchRide.origin.geolocation!!,
                    searchRide.destination.geolocation!!
                )
            ) {
                partialLocationWeight
            } else 0.0

//        val searchRideDate = searchRide.datetime.toDate()

        val searchRideDate = searchRide.datetime.toLocalDateTime().toLocalDate()
        val rideDate = ride.datetime.toLocalDateTime().toLocalDate()
        val dateScore = if (searchRideDate == rideDate) {
            datetimeWeight
        } else {
            return 0.0 // If the dates are not the same, the ride is not considered
        }

        val driverGenderScore =
            if (searchRide.rideOption.driverGender == ride.driver.user?.gender) driverGenderWeight else 0.0
        val vehicleTypeScore =
            if (searchRide.rideOption.vehicleType == ride.driver.vehicle?.type) vehicleTypeWeight else 0.0
        val petFriendlyScore =
            if (searchRide.rideOption.petFriendly == ride.driver.user?.rideOption?.petFriendly) petFriendlyWeight else 0.0

        // You can adjust weights based on the importance of each criterion
        val totalScore =
            locationScore + dateScore + driverGenderScore + vehicleTypeScore + petFriendlyScore
        val totalWeight =
            exactLocationWeight + partialLocationWeight + datetimeWeight + driverGenderWeight + vehicleTypeWeight + petFriendlyWeight
        val similarityScore = totalScore / totalWeight

        return similarityScore
    }


    //
    fun matchRidePassenger(ride: Ride, searchRide: SearchRide, passenger: Passenger): Passenger? {
        // Matching Factors
        val maxDetourDistanceInMeter = 5000.0

        if (ride.origin.geolocation == null ||
            ride.destination.geolocation == null ||
            searchRide.origin.geolocation == null ||
            searchRide.destination.geolocation == null
        ) {
            return null
        }

        // TODO: Check datetime && rideOption

        // 1. Check for perfect origin and destination match
        if ((ride.origin.placeId == searchRide.origin.placeId) &&
            (ride.destination.placeId == searchRide.destination.placeId)
        ) {
            Log.e("RideUtils: Match Ride Passenger", "Perfect Ride Location Match")
            return directlyMatchRidePassenger(ride, passenger)
        } else {
            // 2. Check for partial match with pick-up detour
            if (isWithinRoute(
                    ride.origin.geolocation!!,
                    ride.destination.geolocation!!,
                    searchRide.origin.geolocation!!,
                    searchRide.destination.geolocation!!
                )
            ) {
                Log.e(
                    "RideUtils: Match Ride Passenger",
                    "Partial Ride Location Match With Pickup detour"
                )
                return partiallyMatchRidePassenger(ride, passenger)
            }

        }

        return null
    }

    fun filterRideByPassenger(rideList: List<Ride>, searchRide: SearchRide): List<Ride> {
        if (rideList.isNotEmpty() || searchRide.origin.geolocation == null || searchRide.destination.geolocation == null) {
            return emptyList()
        }

        // Calculate the similarity score for each ride and sort by the highest score
        val scoredRides = rideList.map { ride ->
            val score = calculateSimilarityScore(searchRide, ride)
            ride to score
        }.sortedByDescending { it.second }

        // Return the top ten rides based on similarity scores
        return scoredRides.take(10).map { it.first }
    }

    private fun directlyMatchRidePassenger(ride: Ride, passenger: Passenger): Passenger {

        val distanceInKm =
            calculateDistanceInKm(ride.origin.geolocation!!, ride.destination.geolocation!!)
        //val fare = ride.baseFare + distance * ride.pricePerKm
        val fare = distanceInKm




        passenger.ridePrice = fare

        // 3. Send confirmation notifications to both driver and passenger
        // ... (implementation depends on your notification system)

        return passenger
    }

    private fun partiallyMatchRidePassenger(ride: Ride, passenger: Passenger): Passenger {
        // 1. Calculate additional fare due to detour
        //val detourFare = detourDistance * ride.pricePerKm
        val baseFare = 0.0
        val detourFare = 0.0
        val pricePerKm = 1.0

        // 2. Update fare and display it to passenger for confirmation
        val totalFare = baseFare + calculateDistanceInKm(
            ride.origin.geolocation!!,
            ride.destination.geolocation!!
        ) * pricePerKm + detourFare


        passenger.ridePrice = totalFare

        return passenger
    }


    private fun isTimeCompatible(
        searchTime: Timestamp,
        rideTime: Timestamp,
        timeWindowInHours: Int = 1
    ): Boolean {
        val differenceInHours =
            abs(TimeUnit.HOURS.convert(searchTime.seconds - rideTime.seconds, TimeUnit.SECONDS))
        return differenceInHours <= timeWindowInHours
    }


    // Fare Calculation (Passenger from the various Ride Driver)
    fun calculateEstimatedPrice(ride: SearchRide, waypointList: List<SearchLocation>): Double {
        // Fare Price Factors
        var distance: Double
        var fuelConsumptionRatePerKm: Double
        var tollFares: Double
        var numberOfPassengers: Int

        // Implement your pricing logic based on criteria and attributes of the search and actual ride
        // For example, you might calculate the price based on distance, time, or other factors
        // Return the estimated price

        return 0.0 // Replace with your actual calculation
    }


    private fun calculateDistanceInKm(origin: LatLng, destination: LatLng): Double {
        // Radius of the Earth in kilometers
        val R = 6371.0

        val lat1 = origin.latitude
        val lng1 = origin.longitude
        val lat2 = destination.latitude
        val lng2 = destination.longitude


        // Convert latitude and longitude from degrees to radians
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lng1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lng2)

        // Calculate the change in coordinates
        val dlng = lon2Rad - lon1Rad
        val dlat = lat2Rad - lat1Rad

        // Apply Haversine formula
        val a = sin(dlat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dlng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // Calculate the distance
        val distance = R * c

        return distance
    }


    // Function to calculate the bearing between two LatLng points
    private fun calculateBearing(origin: LatLng, destination: LatLng): Double {
        val lat1 = Math.toRadians(origin.latitude)
        val lon1 = Math.toRadians(origin.longitude)
        val lat2 = Math.toRadians(destination.latitude)
        val lon2 = Math.toRadians(destination.longitude)

        val dLon = lon2 - lon1

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        var bearing = atan2(y, x)
        bearing = Math.toDegrees(bearing)
        bearing = (bearing + 360) % 360

        return bearing
    }

    // Function to check if the requested origin is within the route


    private fun isWithinRoute(
        rideOrigin: LatLng,
        rideDestination: LatLng,
        passengerOrigin: LatLng,
        passengerDestination: LatLng
    ): Boolean {
        val bearingToDestination = calculateBearing(rideOrigin, rideDestination)
        val bearingToPassengerOrigin = calculateBearing(rideOrigin, passengerOrigin)
        var differenceInBearing = abs(bearingToDestination - bearingToPassengerOrigin)

        // Adjust difference in bearing if it exceeds 180 degrees
        if (differenceInBearing > 180) {
            differenceInBearing = 360 - differenceInBearing
        }

        val originDistanceDifferenceInKm = calculateDistanceInKm(rideOrigin, passengerOrigin)
        val destinationDistanceDifferenceInKm =
            calculateDistanceInKm(rideDestination, passengerDestination)


        // Check if the difference in bearing falls within the desired range
        val threshold = 75 // 75-degree range on either side

        // If nearby within 0.5 KM range
        return if (originDistanceDifferenceInKm < 0.5 &&
            destinationDistanceDifferenceInKm < DESTINATION_DETOUR_DISTANCE_IN_KM
        ) {
            true
        } else if (originDistanceDifferenceInKm < ORIGIN_DETOUR_DISTANCE_IN_KM &&
            destinationDistanceDifferenceInKm < 0.5
        ) {
            true
        } else {
            differenceInBearing <= threshold &&
                    originDistanceDifferenceInKm < ORIGIN_DETOUR_DISTANCE_IN_KM &&
                    destinationDistanceDifferenceInKm < DESTINATION_DETOUR_DISTANCE_IN_KM
        }
    }

    // Convert Firebase Timestamp to LocalDateTime
    private fun Timestamp.toLocalDateTime(): LocalDateTime {
        return this.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    // Calculate the difference in minutes between two LocalDateTime objects
    private fun calculateTimeDifferenceInMinutes(dateTime1: LocalDateTime, dateTime2: LocalDateTime): Long {
        return ChronoUnit.MINUTES.between(dateTime1, dateTime2).absoluteValue
    }
}
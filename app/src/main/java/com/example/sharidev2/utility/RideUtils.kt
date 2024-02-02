package com.example.sharidev2.utility

import com.example.sharidev2.data.model.MatchedRide
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.SearchRide
import com.example.sharidev2.data.model.User

class RideUtils {

    fun findMatchingRides(user: User, searchRide: SearchRide, rides: List<Ride>): List<MatchedRide> {
        val matchedRides = mutableListOf<MatchedRide>()

        for (ride in rides) {
            val similarityScore = calculateSimilarityScore(user, searchRide, ride)
            matchedRides.add(MatchedRide(ride, similarityScore))
        }

        // Sort rides by descending similarity score
        matchedRides.sortByDescending { it.similarityScore }

        return matchedRides
    }

    // TODO: modify the algorithm to calculate similarity score
    private fun calculateSimilarityScore(user: User, searchRide: SearchRide, ride: Ride): Double {
        // Implement your logic to calculate the similarity score based on criteria
        // You can assign weights to different criteria and calculate an overall score
        // For simplicity, let's assume a linear combination of criteria for this example
        val originScore = if (searchRide.origin == ride.origin) 1.0 else 0.0
        val destinationScore = if (searchRide.destination == ride.destination) 1.0 else 0.0
        val dateScore = if (searchRide.date == ride.date) 1.0 else 0.0
        val timeScore = if (searchRide.time == ride.time) 1.0 else 0.0

        // You can adjust weights based on the importance of each criterion
        val totalWeight = 4.0
        val similarityScore = (originScore + destinationScore + dateScore + timeScore) / totalWeight

        return similarityScore
    }

    // Fare Calculation (Passenger from the various Ride Driver)
    fun calculateEstimatedPrice(searchRide: SearchRide, actualRideList: List<Ride>): List<Double> {
        // Implement your pricing logic based on criteria and attributes of the search and actual ride
        // For example, you might calculate the price based on distance, time, or other factors
        // Return the estimated price

        return listOf(42.0) // Replace with your actual calculation
    }
}
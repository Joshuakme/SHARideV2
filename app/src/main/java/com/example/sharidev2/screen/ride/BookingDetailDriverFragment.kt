package com.example.sharidev2.screen.ride

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.adapter.BookingTimeLineAdapter
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.databinding.FragmentBookingDetailDriverBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.FirebaseClient


class BookingDetailDriverFragment : Fragment() {
    private lateinit var binding: FragmentBookingDetailDriverBinding

    private val currentUser = FirebaseClient.firebaseAuth.currentUser
    private lateinit var ride: Ride

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_booking_detail_driver, container, false)


        // DATA
        try {
            ride = arguments?.get("ride") as Ride
        } catch (e: Exception) {
            Log.e("Booking Detail Fragment", e.message.toString())
        }

        Log.e("", "RideId: ${ride.id}")

        // ELEMENT VARIABLES




        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)


        setupTextData()

        setupOnClickListeners()

        return binding.root
    }


    private fun setupTextData() {
        val bookingIdText = binding.textBookingDetailBookingId
        val driverNameText = binding.textBookingDetailDriverName
        val driverPhoneNumberText = binding.textBookingDetailDriverPhoneNumber
        val ridePriceText = binding.textBokingDetailRidePrice
        val rideDistanceHourMinText = binding.textBookingDetailDistanceHourMin
        val rideTimelineRecyclerView = binding.recyclerViewBookingDetailTimeline
        val ratingText = binding.textBookingDetailRating


        // Booking
        bookingIdText.text = ride.id

        // Driver
        driverNameText.text = ride.driver.user?.displayName ?: ""
        driverPhoneNumberText.text = CommonUtils.formatHiddenPhoneNumber(ride.driver.user?.phoneNumber ?: "")


        // Ride Price
        var totalPrice = 0.0
        ride.passengers.forEach { (_, passenger) ->
            if(passenger.ridePrice != null) {
                totalPrice += passenger.ridePrice!!
            }
        }

        ridePriceText.text = getString(R.string.price, totalPrice)


        // Map


        // Ride Details
        if(ride.startTime != null && ride.completeTime != null) {
            val rideDurationInSecond = CommonUtils().calculateTimestampDurationInSeconds(ride.startTime!!, ride.completeTime!!)

            val rideHours = (rideDurationInSecond / 3600).toInt()
            val rideMins = ((rideDurationInSecond % 3600) / 60).toInt()

            rideDistanceHourMinText.text = getString(
                R.string.booking_detail_fragment_distance_hour_min,
                ride.destination.distanceMetersFromOrigin / 1000,
                rideHours, rideMins
            )
        } else {
            rideDistanceHourMinText.text = getString(
                R.string.booking_detail_fragment_distance_hour_min,
                ride.destination.distanceMetersFromOrigin / 1000,
                0, 0
            )
        }



        val locationList = mutableListOf(ride.origin.name, ride.destination.name)

        val adapter = BookingTimeLineAdapter(locationList)
        rideTimelineRecyclerView.adapter = adapter
        rideTimelineRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)




        // Rating
        var rating = 0.0F
        ride.reviews.forEach {(id, review) ->
            if(review.rating != null) {
                rating += review.rating
            }
        }

        ratingText.text = rating.toString()
    }

    private fun setupOnClickListeners() {
        val bookingDetailViewRequests = binding.textBookingDetailViewRequests
        val startRideBtn = binding.cardBookingDetailCtaStartBtn


        // NAVIGATION LISTENERS
        // Booking Detail Fragment -> View Booking Request Fragment
        bookingDetailViewRequests.setOnClickListener {
            findNavController().navigate(R.id.action_bookingDetailFragment_to_viewBookingRequestsFragment)
        }

        // Booking Detail Fragment -> Active Ride Fragment
        startRideBtn.setOnClickListener {
            val action = BookingDetailDriverFragmentDirections.actionBookingDetailFragmentToActiveRideFragment(ride)
            findNavController().navigate(action)
        }
    }



}
package com.example.sharidev2.screen.ride

import android.os.Bundle
import android.os.Parcel
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
import com.example.sharidev2.data.model.Ride.Companion.write
import com.example.sharidev2.databinding.FragmentBookingDetailPassengerBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.FirebaseClient


class BookingDetailPassengerFragment : Fragment() {
    private lateinit var binding: FragmentBookingDetailPassengerBinding

    private val currentUser = FirebaseClient.firebaseAuth.currentUser
    private lateinit var ride: Ride

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_booking_detail_passenger, container, false)

        Log.e("Booking Detail Passenger Fragment", "On Created")

        // DATA
        try {
            ride = arguments?.get("ride") as Ride
        } catch (e: Exception) {
            Log.e("Booking Detail Fragment", e.message.toString())
        }


        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnViewBookingDetailPassengerNavBack


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)


        setupTextData()

        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }


        return binding.root
    }


    private fun setupTextData() {
        val bookingDateTimeText = binding.textViewBookingDetailPassengerFragmentTitleDate
        val bookingIdText = binding.textBookingDetailPassengerBookingId
        val driverNameText = binding.textBookingDetailPassengerDriverName
        val driverPhoneNumberText = binding.textBookingDetailPassengerDriverPhoneNumber
        val ridePriceText = binding.textBokingDetailPassengerRidePrice
        val rideDistanceHourMinText = binding.textBookingDetailPassengerDistanceHourMin
        val rideTimelineRecyclerView = binding.recyclerViewBookingPassengerDetailTimeline
        val ratingText = binding.textBookingDetailRating


        // Booking
        bookingDateTimeText.text = CommonUtils.formatDateTime(ride.datetime)
        bookingIdText.text = ride.id

        // Driver
        driverNameText.text = ride.driver.user?.displayName ?: ""
        driverPhoneNumberText.text = CommonUtils.formatHiddenPhoneNumber(ride.driver.user?.phoneNumber ?: "")


        // Ride Price
        val priceValue = ride.passengers[currentUser?.uid]?.ridePrice ?: 0.0

        ridePriceText.text = getString(R.string.price, priceValue)


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




        val locationList = mutableListOf(ride.origin.name,ride.origin.name, ride.destination.name)

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable("ride", ride)
    }
}
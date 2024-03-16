package com.example.sharidev2.screen.ride

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.sharidev2.utility.GoogleMapUtils
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions


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
        val bookingDateTimeText = binding.textBookingDetailDriverFragmentTitleDate
        val bookingIdText = binding.textBookingDetailBookingId
        val driverNameText = binding.textBookingDetailDriverName
        val driverPhoneNumberText = binding.textBookingDetailDriverPhoneNumber
        val ridePriceText = binding.textBokingDetailRidePrice
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_booking_detail_driver_container) as SupportMapFragment
        val rideDistanceHourMinText = binding.textBookingDetailDistanceHourMin
        val rideTimelineRecyclerView = binding.recyclerViewBookingDetailTimeline
        val ratingText = binding.textBookingDetailRating


        // Booking
        bookingDateTimeText.text = CommonUtils.formatDateTime(ride.datetime)
        bookingIdText.text = ride.id

        // Driver
        driverNameText.text = ride.driver.user?.displayName ?: ""
        driverPhoneNumberText.text = CommonUtils.formatHiddenPhoneNumber(ride.driver.user?.phoneNumber ?: "")


        // Ride Price
        var totalPrice = 0.0
        ride.passengers.forEach { passenger ->
            if(passenger.ridePrice != null) {
                totalPrice += passenger.ridePrice!!
            }
        }

        ridePriceText.text = getString(R.string.price, totalPrice)


        // Map
        mapFragment.getMapAsync { googleMap ->
            googleMap.uiSettings.let {
                it.isMapToolbarEnabled = false
                it.isMyLocationButtonEnabled = false
                it.isZoomControlsEnabled = false
                it.isTiltGesturesEnabled = false
                it.isCompassEnabled = false
                it.isScrollGesturesEnabled = false
                it.isScrollGesturesEnabledDuringRotateOrZoom = false
                it.isIndoorLevelPickerEnabled = false
                it.isRotateGesturesEnabled = false
                it.isZoomGesturesEnabled = false
            }
            // Disable marker onclick event
            googleMap.setOnMarkerClickListener {
                true
            }

            Toast.makeText(requireContext(), ride.completedRoute?.isEmpty().toString(), Toast.LENGTH_SHORT).show()

            if(ride.completedRoute != null && ride.completedRoute!!.isNotEmpty()) {
                val polyline = googleMap.addPolyline(PolylineOptions().addAll(ride.completedRoute!!).clickable(false))
                polyline.color = CommonUtils().getThemeColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceInverse)
            }

            val originColor = CommonUtils().getThemeColor(requireContext(), com.google.android.material.R.attr.colorPrimaryInverse)
            val destinationColor = CommonUtils().getThemeColor(requireContext(), com.google.android.material.R.attr.colorErrorContainer)
            val originIcon = CommonUtils().getLocationBitmapFromVector(requireContext(), originColor)
            val destinationIcon = CommonUtils().getLocationBitmapFromVector(requireContext(), destinationColor)

            GoogleMapUtils().addMarker(googleMap, ride.origin.geolocation!!, originIcon)
            GoogleMapUtils().addMarker(googleMap, ride.destination.geolocation!!, destinationIcon)
            GoogleMapUtils().updateMapZoomAndCamera(requireContext(), googleMap, ride.origin.geolocation, ride.destination.geolocation)
        }


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
        ride.reviews.forEach {review ->
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
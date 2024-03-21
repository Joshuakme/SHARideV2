package com.example.sharidev2.screen.ride

import android.os.Bundle
import android.os.Parcel
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.adapter.BookingTimeLineAdapter
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.Ride.Companion.write
import com.example.sharidev2.data.repository.RideRepository
import com.example.sharidev2.databinding.FragmentBookingDetailPassengerBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.GoogleMapUtils
import com.example.sharidev2.viewmodel.BookingDetailViewModel
import com.example.sharidev2.viewmodel.SharedSearchRideViewModel
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.launch


class BookingDetailPassengerFragment : Fragment() {
    private lateinit var binding: FragmentBookingDetailPassengerBinding

    private val bookingDetailViewModel: BookingDetailViewModel by viewModels()

    private val currentUser = FirebaseClient.firebaseAuth.currentUser
    private lateinit var ride: Ride

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_booking_detail_passenger, container, false)


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
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_booking_detail_passenger_container) as SupportMapFragment
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
        val ridePrice: Double = ride.passengers
            .filter { it.userUid == currentUser?.uid }
            .sumOf { it.ridePrice ?: 0.0 }

        ridePriceText.text = getString(R.string.price, ridePrice)


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


            lifecycleScope.launch {
                val route = if(ride.completedRoute != null && ride.completedRoute!!.isNotEmpty()) {
                    ride.completedRoute
                } else {
                    bookingDetailViewModel.getRoute(ride.origin.name, ride.destination.name)
                }

                if(route != null) {
                    val polyline = googleMap.addPolyline(PolylineOptions().addAll(route).clickable(false))
                    polyline.color = CommonUtils().getThemeColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceInverse)
                }
            }




            val originColor = CommonUtils().getMapOriginMarkerColor(requireContext())
            val destinationColor = CommonUtils().getMapDestMarkerColor(requireContext())
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable("ride", ride)
    }
}
package com.example.sharidev2.screen.ride

import android.net.Uri
import android.os.Bundle
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
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharidev2.GlideApp
import com.example.sharidev2.R
import com.example.sharidev2.adapter.RideDetailPassengerImageAdapter
import com.example.sharidev2.adapter.BookingTimeLineAdapter
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.databinding.FragmentRideDetailBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.viewmodel.CurrentLocationViewModel
import com.example.sharidev2.viewmodel.RideDetailViewModel
import com.example.sharidev2.viewmodel.RideViewModel
import com.example.sharidev2.viewmodel.SharedSearchRideViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RideDetailFragment : Fragment() {
    private lateinit var binding: FragmentRideDetailBinding
    private val rideDetailViewModel: RideDetailViewModel by viewModels()
    private val rideViewModel: RideViewModel by viewModels()
    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModels()
    private val searchRideViewModel: SharedSearchRideViewModel by activityViewModels()
    private val currentUser = FirebaseClient.firebaseAuth.currentUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ride_detail, container, false)


        // DATA
        val rideId = arguments?.getString("rideId")
        Log.e("", "RideId: $rideId")

        lifecycleScope.launch(Dispatchers.Main) {
            if(rideId != null) {
                val reqRide = getRide(rideId)

                if(reqRide != null) {
                    rideDetailViewModel.setRide(reqRide)
                }
            }
        }
        var estimatedPrice: Double = 0.0


        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnRideDetailNavBack
        val requestBtn = binding.btnRideDetailRequestRide
        val navController = Navigation.findNavController(requireActivity(), R.id.fragment_container_main)
        val driverImg = binding.imgRideDetailDriver
        val driverNameText = binding.textRideDetailDriverName
        val driverPhoneNumberText = binding.textRideDetailPhoneNumber
        val driverRatingText = binding.textRideDetailDriverRating
        val driverRatingReviewText = binding.textRideDetailDriverRatingReview
        val passengersSeatsBookedText = binding.textRideDetailPassengersSeatsBooked
        val passengersImageRecyclerView = binding.recyclerRideDetailPassengersImage
        val rideDetailsTimelineRecyclerView = binding.recyclerRideDetailTimeline
        val rideDetailRideDateText = binding.textRideDetailRideDate
        val rideDetailStartingTimeText = binding.textRideDetailStartingTime
        val rideDetailVehicleText = binding.textRideDetailVehicle
        val estimatedPriceText = binding.textRideDetailEstimatedPrice


        rideDetailViewModel.ride.observe(viewLifecycleOwner) {ride ->
            if(ride != null) {
                // Driver
                if(ride.driver.user != null) {
                    if(ride.driver.user!!.photoUri != null || ride.driver.user!!.photoUri.toString() != "") {
                        val photoUri = ride.driver.user!!.photoUri

                        Log.e("RideDetailFragment", "PhotoUri: " + photoUri.toString())
                        if(CommonUtils().isUrl(photoUri.toString())) {
                            GlideApp.with(requireContext())
                                .load(photoUri.toString())
                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                                .into(driverImg)
                        }
                    } else {
                        val colorOutline = CommonUtils().getThemeColor(requireContext(), com.google.android.material.R.attr.colorOutline)
                        driverImg.setColorFilter(colorOutline)
                    }

                    driverNameText.text = ride.driver.user?.displayName
                    driverPhoneNumberText.text = CommonUtils.formatHiddenPhoneNumber(ride.driver.user?.phoneNumber ?: "")
                    driverRatingText.text = getString(R.string.ride_detail_fragment_driver_rating, ride.driver.user?.rating?.toDouble() ?: 0.0)
                    driverRatingReviewText.text = getString(R.string.ride_detail_fragment_driver_rating_review, 0)
                }

                // Passengers
                if(ride.driver.vehicle != null) {
                    passengersSeatsBookedText.text = getString(R.string.ride_detail_fragment_passengers_seat_booked, 0, ride.driver.vehicle.capacity-1)
                    val defaultUserImage = binding.imgRideDetailPassenger1
                    defaultUserImage.tag = "baseline_account_circle_24"

                    if(ride.passengers.isNotEmpty() && ride.passengers != null) {
                        passengersSeatsBookedText.text = getString(
                            R.string.ride_detail_fragment_passengers_seat_booked,
                            ride.passengers.size,
                            ride.driver.vehicle.capacity-1
                        )

                        // Passengers Image
                        val imageList = mutableListOf<Uri>()
                        // Check if there are passengers
                        if (ride.passengers.isEmpty()) {
                            // Populate with default user images
                            val remainingCapacity = ride.driver.vehicle.capacity - 1
                            repeat(remainingCapacity) {
                                imageList.add(CommonUtils().getUriFromVectorDrawable(defaultUserImage))
                            }
                        } else {
                            // Populate with passengers' photos
                            ride.passengers.forEach { passenger ->
                                val photoUri = passenger.user?.photoUri ?: CommonUtils().getUriFromVectorDrawable(defaultUserImage)
                                imageList.add(photoUri)
                            }
                        }



                        val adapter = RideDetailPassengerImageAdapter(requireContext(), imageList)
                        passengersImageRecyclerView.adapter = adapter
                        passengersImageRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
                    } else {
                        passengersSeatsBookedText.text = getString(
                            R.string.ride_detail_fragment_passengers_seat_booked,
                            0,
                            ride.driver.vehicle.capacity-1
                        )

                        val imageList = mutableListOf<Uri>()
                        for(i in 1..<ride.driver.vehicle.capacity) {
                            imageList.add(CommonUtils().getUriFromVectorDrawable(defaultUserImage))
                        }

                        val adapter = RideDetailPassengerImageAdapter(requireContext(), imageList)
                        passengersImageRecyclerView.adapter = adapter
                        passengersImageRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
                    }
                }

                // Ride Details
                val rideList = mutableListOf(ride.origin.name, ride.destination.name)

                val rideAdapter = BookingTimeLineAdapter(rideList)
                rideDetailsTimelineRecyclerView.adapter = rideAdapter
                rideDetailsTimelineRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)


                rideDetailRideDateText.text =  ride.datetime.let { CommonUtils.formatDate(it) + if(CommonUtils().isToday(it)) "(Today)" else ""}
                rideDetailStartingTimeText.text =  CommonUtils.formatTime(ride.datetime)

                if(ride.driver.vehicle != null) {
                    val vehicle = ride.driver.vehicle
                    rideDetailVehicleText.text = "${vehicle.model} (${vehicle.color})"
                }


                // Price Estimation
                estimatedPrice = 0.0      // TODO: Calculate price
                estimatedPriceText.text = getString(R.string.ride_detail_fragment_passengers_estimated_price, estimatedPrice)

                loadingData(false)
            } else {
                loadingData(true)
            }
        }


        requestBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {

                if((rideId != null) && (currentUser != null)) {
                    val passenger = Passenger(
                        userUid = currentUser.uid,
                        location = currentLocationViewModel.currentLocation.value,
                        origin = searchRideViewModel.origin.value,
                        destination = searchRideViewModel.destination.value,
                        ridePrice = estimatedPrice,
                        requestedDateTime = searchRideViewModel.rideDateTime.value
                    )

                    val responseStatus = rideViewModel.addPassengerToRide(passenger, rideId)

                    when(responseStatus) {
                        Constants.FIREBASE_REQUEST_SUCCESS -> {
                            Toast.makeText(requireContext(), "Ride requested successfully!", Toast.LENGTH_SHORT).show()

                            navController.navigate(R.id.action_rideDetailFragment_to_bookingFragment)
                        }

                        Constants.FIREBASE_REQUEST_EXCEPTION -> {
                            Toast.makeText(requireContext(), "Ride requested failed!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }


        return binding.root
    }


    private fun loadingData(loading: Boolean) {
        val loadingProgressBar = binding.progressBarRideDetailLoading
        val loadingBackgroundModal = binding.clRideDetailLoadingModalBackground

        if(loading) {
            loadingProgressBar.visibility = View.VISIBLE
            loadingBackgroundModal.visibility = View.VISIBLE
        } else {
            loadingProgressBar.visibility = View.GONE
            loadingBackgroundModal.visibility = View.GONE
        }
    }

    private suspend fun getRide(rideId: String): Ride? {
        return withContext(Dispatchers.IO) {
            try {
                FirebaseClient.getRideFromRideId(rideId)
            } catch (e: Exception) {
                Log.e("Ride Detail Fragment: getRide()", e.message.toString())
                null
            }
        }
    }
}
package com.example.sharidev2.screen.ride

import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.adapter.ActiveRidePassengerImageAdapter
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.databinding.FragmentActiveRideBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.FirebaseClient.convertFirebaseImageToBitmap
import com.example.sharidev2.utility.GoogleMapUtils
import com.example.sharidev2.viewmodel.ActiveRideViewModel
import com.example.sharidev2.viewmodel.CurrentLocationViewModel
import com.google.android.gms.maps.SupportMapFragment


class ActiveRideFragment : Fragment() {
    private lateinit var binding: FragmentActiveRideBinding
    private val activeRideViewModel: ActiveRideViewModel by viewModels()
    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModels()

    private lateinit var googleMapFragment: SupportMapFragment

    private val passengerList = mutableListOf<Passenger>()


    private val googleMapUtils = GoogleMapUtils()
    private val mHandler: Handler = Handler()
    private lateinit var mRunnable: Runnable
    private val LOCATION_UPDATE_INTERVAL = 3000 as Long

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_active_ride, container, false)


        // Args

        try {
            val activeRide = arguments?.get("ride") as Ride

            if(activeRide != null) {
                activeRideViewModel.setActiveRide(activeRide)
            }
        } catch (e: Exception) {
            Log.e("Booking Detail Fragment", e.message.toString())
        }


        // ELEMENT VARIABLES
        googleMapFragment = childFragmentManager.findFragmentById(R.id.map_active_ride_container) as SupportMapFragment





        setupMap()
        setupData()

        return binding.root
    }


    private fun startUserLocationsRunnable() {
        Log.d(
            TAG,
            "startUserLocationsRunnable: starting runnable for retrieving updated locations."
        )
        mHandler.postDelayed(Runnable {
            getUserLocation()
            mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL)
        }.also { mRunnable = it }, LOCATION_UPDATE_INTERVAL)
    }

    private fun stopLocationUpdates() {
        mHandler.removeCallbacks(mRunnable)
    }

    private fun setupMap() {

        currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) { currentLocation ->
            if (currentLocation != null) {
                val myLocationBtn = binding.cardActiveRideMyLocationContainer

                googleMapFragment.getMapAsync { googleMap ->
                    googleMapUtils.setupMapListeners(googleMap, currentLocation, myLocationBtn,
                        object : GoogleMapUtils.MyLocationButtonCallback {
                            override fun showMyLocationButton(show: Boolean) {
                                showMyLocationBtn(show)
                            }
                        }
                    )
                }
            }
        }

    }

    private fun setupData() {
        val driverPhotoImg = binding.imgActiveRideDriverPhoto
        val rideVehicleModelColor = binding.textActiveRideVehicleModelColor
        val rideVehiclePlateNumber = binding.textActiveRideVehiclePlateNumber
        val passengersRecyclerView = binding.recyclerViewActiveRidePassengers


        activeRideViewModel.activeRide.observe(viewLifecycleOwner) {activeRide ->
            if(activeRide != null) {
                // Driver
                if(activeRide.driver.user?.photoUrl != null) {
                    driverPhotoImg.setImageURI(activeRide.driver.user?.photoUrl)
                }

                // Ride
                if(activeRide.driver.vehicle != null) {
                    rideVehicleModelColor.text = "${activeRide.driver.vehicle.model} (${activeRide.driver.vehicle.color})"
                    rideVehiclePlateNumber.text = activeRide.driver.vehicle.plateNumber
                }

                // Passengers
                activeRide.passengers.forEach { (id, passenger) ->
                    passengerList.add(passenger)
                }
                val adapter = ActiveRidePassengerImageAdapter(requireContext(), passengerList,
                    object: ActiveRidePassengerImageAdapter.OnPassengerImageClickListener {
                        override fun OnPassengerImageClick(passenger: Passenger) {
                            if(passenger != null) {
                                googleMapFragment.getMapAsync {googleMap ->
                                    googleMapUtils.moveMapCamera(googleMap, passenger.location!!)
                                }
                            }
                        }
                    })
                passengersRecyclerView.adapter = adapter
                passengersRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            }
        }


    }

    private fun getUserLocation() {
        googleMapFragment.getMapAsync { googleMap ->
            activeRideViewModel.activeRideUserLocationList.observe(viewLifecycleOwner) { locationList ->
                if (locationList != null) {
                    for (location in locationList) {
                        if (location.location != null) {
                            googleMap.clear()


                            val currentUserColor = CommonUtils().getThemeColor(
                                requireContext(),
                                com.google.android.material.R.attr.colorPrimary
                            )
                            val otherUserColor = CommonUtils().getThemeColor(
                                requireContext(),
                                com.google.android.material.R.attr.colorSecondary
                            )

                            val firebaseStorage = FirebaseClient.firebaseStorage
                            val storageRef =
                                firebaseStorage.reference.child(location.user?.photoUrl.toString())


                            convertFirebaseImageToBitmap(storageRef,
                                onSuccess = { bitmap ->
                                    val userIcon =
                                        if (location.user?.uid == FirebaseClient.firebaseAuth.uid) {
                                            CommonUtils().createMarkerWithCircularImage(
                                                requireContext(),
                                                bitmap,
                                                currentUserColor
                                            )
                                        } else {
                                            CommonUtils().createMarkerWithCircularImage(
                                                requireContext(),
                                                bitmap,
                                                otherUserColor
                                            )
                                        }

//                                    val userIcon = if(activeRideViewModel.activeRideCurrentUserRole.value != "driver") {
//
//                                    } else {
//                                        CommonUtils().createMarkerWithCircularImage(requireContext(), bitmap, otherUserColor)
//                                    }

                                    googleMapUtils.addMarker(
                                        googleMap,
                                        location.location,
                                        userIcon
                                    )
                                },
                                onFailure = { exception ->
                                    // Error occurred, handle it
                                }
                            )

                        }
                    }
                }
            }
        }
    }


    private fun showMyLocationBtn(show: Boolean) {
        val myLocationBtn = binding.cardActiveRideMyLocationContainer

        myLocationBtn.visibility = if (show) View.VISIBLE else View.GONE
    }


    override fun onResume() {
        super.onResume()

        startUserLocationsRunnable()
    }
}
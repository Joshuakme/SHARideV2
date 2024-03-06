package com.example.sharidev2.screen.ride

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Polyline


class ActiveRideFragment() :
    Fragment(),
    GoogleMap.OnPolylineClickListener {
    private lateinit var binding: FragmentActiveRideBinding
    private val activeRideViewModel: ActiveRideViewModel by viewModels()
    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModels()

    private lateinit var googleMapFragment: SupportMapFragment

    private val currentUser = FirebaseClient.firebaseAuth.currentUser
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

        googleMapFragment.getMapAsync { googleMap ->
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
            } else {
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = false
                googleMap.uiSettings.isMapToolbarEnabled = false
            }

            currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) { currentLocation ->
                if (currentLocation != null) {
                    val myLocationBtn = binding.cardActiveRideMyLocationContainer

                    googleMapUtils.setupMapListeners(googleMap, currentLocation, myLocationBtn,
                        object : GoogleMapUtils.MyLocationButtonCallback {
                            override fun showMyLocationButton(show: Boolean) {
                                showMyLocationBtn(show)
                            }
                        }
                    )
                }
            }

            // Add User Markers into map
            activeRideViewModel.activeRide.observe(viewLifecycleOwner) {activeRide ->
                if(activeRide != null) {
                    // Origin
                    val typedValue = TypedValue()
                    context?.theme?.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
                    val colorPrimary = typedValue.data

                    GoogleMapUtils().addMarker(
                        googleMap,
                        activeRide.origin.geolocation!!,
                        CommonUtils().getLocationBitmapFromVector(requireContext(), colorPrimary)
                    )

                    // Destination
                    context?.theme?.resolveAttribute(com.google.android.material.R.attr.colorError, typedValue, true)
                    val colorError = typedValue.data

                    GoogleMapUtils().addMarker(
                        googleMap,
                        activeRide.destination!!.geolocation!!,
                        CommonUtils().getLocationBitmapFromVector(requireContext(), colorError)
                    )


                    if(currentUser != null) {
                        // Driver
                        if( activeRide.driver.location != null) {
                            // Resolve the attribute to get the color value programmatically
                            context?.theme?.resolveAttribute(com.google.android.material.R.attr.colorPrimaryDark, typedValue, true)
                            val colorPrimaryDark = typedValue.data

                            val icon = CommonUtils().getLocationBitmapFromVector(requireContext(), colorPrimaryDark)
                            GoogleMapUtils().addMarker(googleMap, activeRide.driver.location, icon)
                        }


                        // Passengers
                        activeRide.passengers.forEach() {(s, passenger) ->
                            if(passenger?.location != null) {
                                val icon = CommonUtils().getLocationBitmapFromVector(requireContext(), Color.BLUE)

                                // Add a marker to the map
                                if(passenger.userUid == currentUser.uid) {

                                }

                                GoogleMapUtils().addMarker(googleMap, passenger.location, icon)
                                GoogleMapUtils().moveMapCamera(googleMap, passenger.location)

                                Glide.with(requireContext())
                                    .asBitmap()
                                    .load(passenger.user?.photoUrl) // Replace profilePictureUrl with the actual URL
                                    .into(object : CustomTarget<Bitmap>() {
                                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                            // Set the loaded bitmap as the marker image
//                                        val markerView: View = LayoutInflater.from(context).inflate(R.layout.marker_custom_layout, null)
//                                        markerView.findViewById<ImageView>(R.id.image_marker).setImageBitmap(resource)
//
//                                        // Convert the marker view to a BitmapDescriptor
//                                        val icon = BitmapDescriptorFactory.fromBitmap(CommonUtils().createDrawableFromView(requireContext(), markerView))

//                                        GoogleMapUtils().addMarker(googleMap, passenger.location, icon)
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {
                                            // Handle resource clearing if needed
                                        }
                                    })
                            }
                        }
                    }

                    GoogleMapUtils().calculateDirections(
                        requireContext(),
                        googleMap,
                        activeRide.origin.geolocation!!,
                        activeRide.destination.geolocation!!
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

    override fun onPolylineClick(p0: Polyline) {
        TODO("Not yet implemented")
    }
}
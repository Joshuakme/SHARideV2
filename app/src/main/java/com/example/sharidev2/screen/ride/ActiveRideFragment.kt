package com.example.sharidev2.screen.ride

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.sharidev2.R
import com.example.sharidev2.adapter.ActiveRidePassengerImageAdapter
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.PolylineData
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.databinding.FragmentActiveRideBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.FirebaseClient.convertFirebaseImageToBitmap
import com.example.sharidev2.utility.GoogleMapUtils
import com.example.sharidev2.viewmodel.ActiveRideViewModel
import com.example.sharidev2.viewmodel.CurrentLocationViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.internal.PolylineEncoding
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ActiveRideFragment : Fragment() {
    private lateinit var binding: FragmentActiveRideBinding
    private val activeRideViewModel: ActiveRideViewModel by viewModels()
    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModels()

    private lateinit var googleMapFragment: SupportMapFragment

    private val currentUser = FirebaseClient.firebaseAuth.currentUser
    private val passengerList = mutableListOf<Passenger>()
    private val polylineList = mutableListOf<PolylineData>()


    private val googleMapUtils = GoogleMapUtils()
    private val mHandler: Handler = Handler()
    private lateinit var mRunnable: Runnable
    private val LOCATION_UPDATE_INTERVAL = 8000 as Long

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_active_ride, container, false)


        // Args
        try {
            val activeRide = arguments?.get("ride") as Ride

            activeRideViewModel.setActiveRide(activeRide)
        } catch (e: Exception) {
            Log.e("Booking Detail Fragment", e.message.toString())
        }


        // ELEMENT VARIABLES
        googleMapFragment =
            childFragmentManager.findFragmentById(R.id.map_active_ride_container) as SupportMapFragment



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
            activeRideViewModel.activeRide.observe(viewLifecycleOwner) { activeRide ->
                if (activeRide != null) {
                    // Origin
                    val typedValue = TypedValue()
                    context?.theme?.resolveAttribute(
                        com.google.android.material.R.attr.colorPrimary,
                        typedValue,
                        true
                    )
                    val colorPrimary = typedValue.data

                    GoogleMapUtils().addMarker(
                        googleMap,
                        activeRide.origin.geolocation!!,
                        CommonUtils().getLocationBitmapFromVector(requireContext(), colorPrimary)
                    )

                    // Destination
                    context?.theme?.resolveAttribute(
                        com.google.android.material.R.attr.colorError,
                        typedValue,
                        true
                    )
                    val colorError = typedValue.data

                    GoogleMapUtils().addMarker(
                        googleMap,
                        activeRide.destination.geolocation!!,
                        CommonUtils().getLocationBitmapFromVector(requireContext(), colorError)
                    )


                    if (currentUser != null) {
                        // Driver
                        if (activeRide.driver.location != null) {
                            // Resolve the attribute to get the color value programmatically
                            context?.theme?.resolveAttribute(
                                com.google.android.material.R.attr.colorPrimaryDark,
                                typedValue,
                                true
                            )
                            val colorPrimaryDark = typedValue.data

                            val icon = CommonUtils().getLocationBitmapFromVector(
                                requireContext(),
                                colorPrimaryDark
                            )
                            GoogleMapUtils().addMarker(googleMap, activeRide.driver.location, icon)
                        }


                        // Passengers
                        activeRide.passengers.forEach() { passenger ->
                            if (passenger?.location != null) {

                                // Add a marker to the map
                                if (passenger.userUid == currentUser.uid) {

                                }

                                GoogleMapUtils().moveMapCamera(googleMap, passenger.location)

                                Glide.with(requireContext())
                                    .asBitmap()
                                    .load(passenger.user?.photoUri) // Replace profilePictureUrl with the actual URL
                                    .transform(RoundedCornersTransformation(8, 2))
                                    .into(object : CustomTarget<Bitmap>() {
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: Transition<in Bitmap>?
                                        ) {

                                            GoogleMapUtils().addOverlayToMap(
                                                googleMap,
                                                resource,
                                                passenger.location,
                                                30f,
                                                30f
                                            )
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
                        activeRide.origin.geolocation!!,
                        activeRide.destination.geolocation!!,
                        true
                    ) { result ->
                        if (result != null) {
                            Handler(Looper.getMainLooper()).post {
                                if (polylineList.isNotEmpty()) {
                                    for (polylineData in polylineList) {
                                        polylineData.polyline.remove()
                                    }
                                    polylineList.clear()
                                }
                                val routePathsList = mutableListOf<MutableList<LatLng>>()

                                for (route in result.routes) {
                                    val decodedPath =
                                        PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                                    val newDecodedPath = mutableListOf<LatLng>()

                                    for (latLng in decodedPath) {
                                        newDecodedPath.add(LatLng(latLng.lat, latLng.lng))
                                        Log.e(
                                            "GoogleMapUtils: Polyline",
                                            "Lat: ${latLng.lat}, Lng: ${latLng.lng}"
                                        )
                                    }


                                    val polyline: Polyline = googleMap.addPolyline(
                                        PolylineOptions().addAll(newDecodedPath).clickable(true)
                                    )


                                    polylineList.add(PolylineData(polyline, route.legs[0]))
                                    routePathsList.add(newDecodedPath)
                                }


                                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                    activeRideViewModel.addRoutePathList(routePathsList)
                                }
                            }
                        }
                    }
                }
            }

            googleMap.setOnPolylineClickListener {
                val colorSecondary = CommonUtils().getThemeColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorSecondary
                )

                for (polylineData in polylineList) {
                    if (it.id == polylineData.polyline.id) {
                        polylineData.polyline.color = colorSecondary
                        polylineData.polyline.zIndex = 1f
                    } else {
                        polylineData.polyline.color = Color.DKGRAY
                        polylineData.polyline.zIndex = 0f
                    }
                }
            }
        }
    }

    private fun setupData() {
        val driverPhotoImg = binding.imgActiveRideDriverPhoto
        val driverName = binding.textActiveRideDriverName
        val driverContact = binding.textActiveRideDriverPhone
        val rideVehicleModelColor = binding.textActiveRideVehicleModelColor
        val rideVehiclePlateNumber = binding.textActiveRideVehiclePlateNumber
        val passengersRecyclerView = binding.recyclerViewActiveRidePassengers
        val pickUpDetailsName = binding.textActiveRidePickUpName
        val pickUpDetailsDetailedAddress = binding.textActiveRidePickUpDetailedAddress
        val destinationDetailsName = binding.textActiveRideDestinationName
        val destinationDetailsDetailedAddress = binding.textActiveRideDestinationDetailedAddress


        activeRideViewModel.activeRide.observe(viewLifecycleOwner) { activeRide ->
            if (activeRide != null) {
                // Driver
                if (activeRide.driver.user?.photoUri != null) {
                    val photoUri = activeRide.driver.user?.photoUri

                    if (CommonUtils().isUrl(photoUri.toString())) {
                        Glide.with(requireContext())
                            .load(photoUri.toString())
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                            .into(driverPhotoImg)
                    }
                }
                if (activeRide.driver.user != null) {
                    driverName.text = activeRide.driver.user!!.displayName
                    driverContact.text = activeRide.driver.user!!.phoneNumber
                }


                // Ride Vehicle
                if (activeRide.driver.vehicle != null) {
                    rideVehicleModelColor.text =
                        "${activeRide.driver.vehicle.model} (${activeRide.driver.vehicle.color})"
                    rideVehiclePlateNumber.text = activeRide.driver.vehicle.plateNumber
                }

                // Passengers

                val adapter =
                    ActiveRidePassengerImageAdapter(requireContext(), activeRide.passengers,
                        object : ActiveRidePassengerImageAdapter.OnPassengerImageClickListener {
                            override fun OnPassengerImageClick(passenger: Passenger) {
                                googleMapFragment.getMapAsync { googleMap ->
                                    googleMapUtils.moveMapCamera(googleMap, passenger.location!!)
                                }
                            }
                        })
                passengersRecyclerView.adapter = adapter
                passengersRecyclerView.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)


                // Pick Up Details
                pickUpDetailsName.text = activeRide.origin.name
                pickUpDetailsDetailedAddress.text = activeRide.origin.detailAddress


                // Destination Details
                destinationDetailsName.text = activeRide.destination.name
                destinationDetailsDetailedAddress.text = activeRide.destination.detailAddress


                setupListener(activeRide)
            }
        }


    }


    private fun setupListener(activeRide: Ride) {
        val rideInfoScrolLView = binding.svActiveRideRideInfo
        val expandMapBtn = binding.imgBtnActiveRideExpandRideDetail
        val shareRideBtn = binding.btnActiveRideShareRide
        val sosCallBtn = binding.btnActiveRideSosCall
        val callDriverBtn = binding.btnActiveRideCallDriver
        val messageDriverBtn = binding.btnActiveRideMessageDriver

        // Drawer Open Status
        var drawerOpen = false

        expandMapBtn.setOnClickListener {
            // TODO: Expand the ride info segment


            val params = rideInfoScrolLView.layoutParams
            params.height =
                if (drawerOpen) resources.getDimensionPixelSize(R.dimen.ss_height_350dp) else ViewGroup.LayoutParams.MATCH_PARENT

            rideInfoScrolLView.layoutParams = params

            expandMapBtn.rotation = if (drawerOpen) 0f else 180f

            drawerOpen = !drawerOpen
        }

        shareRideBtn.setOnClickListener {
            // TODO: Share link to other app
        }

        sosCallBtn.setOnClickListener {
            // TODO: Handle SOS accordingly
        }

        callDriverBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL)

            if (activeRide.driver.user?.phoneNumber != null) {
                intent.data = Uri.parse("tel:${activeRide.driver.user!!.phoneNumber}")
            }
        }

        messageDriverBtn.setOnClickListener {
            // TODO: Navigate to message chat fragment
        }
    }

    private fun getUserLocation() {
        googleMapFragment.getMapAsync { googleMap ->
            activeRideViewModel.activeRideUserLocationList.observe(viewLifecycleOwner) { locationList ->
                if (locationList != null) {
                    for (location in locationList) {
                        if (location.location != null) {
                            googleMap.clear()

                            if (location.user?.photoUri != null) {
                                Glide.with(requireContext())
                                    .asBitmap()
                                    .load(location.user!!.photoUri.toString()) // Replace profilePictureUrl with the actual URL
                                    .transform(RoundedCornersTransformation(8, 2))
                                    .into(object : CustomTarget<Bitmap>() {
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: Transition<in Bitmap>?
                                        ) {
                                            GoogleMapUtils().addOverlayToMap(
                                                googleMap,
                                                resource,
                                                location.location,
                                                30f,
                                                30f
                                            )
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {
                                            // Handle resource clearing if needed
                                        }
                                    })
                            }

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

        //startUserLocationsRunnable()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.PERMISSIONS_REQUEST_CALL) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Do nothing
            } else {
                // Permission denied, handle accordingly
            }
        }
    }
}
package com.example.sharidev2.screen.ride

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.databinding.FragmentSearchSelectOriginBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.GoogleMapUtils
import com.example.sharidev2.viewmodel.CurrentLocationViewModel
import com.example.sharidev2.viewmodel.SharedSearchRideViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.card.MaterialCardView
import com.google.maps.internal.PolylineEncoding

class SearchSelectOriginFragment : Fragment() {
    private lateinit var binding: FragmentSearchSelectOriginBinding
    private val searchRideViewModel: SharedSearchRideViewModel by activityViewModels()
    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModels()
    private lateinit var myLocationBtn: MaterialCardView

    private lateinit var context: Context
    private lateinit var placesClient: PlacesClient
    private lateinit var mapFragment: SupportMapFragment



    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_select_origin, container, false)


        // VARIABLES INIT
        context = if(getContext() != null) {
            requireContext()
        } else {
            requireActivity().applicationContext
        }

        placesClient = Places.createClient(context)

        // ELEMENT VARIABLES
        myLocationBtn = binding.cardSearchSelectOriginMyLocationContainer
        mapFragment =
            childFragmentManager.findFragmentById(R.id.map_search_origin_container) as SupportMapFragment
        val originNameText = binding.textSearchSelectOriginLocationName
        val originDistanceAddress = binding.textSearchSelectOriginLocationDistanceAddress


        // LAYOUT SETTINGS
        val activity = activity as MainActivity
        activity.setStatusBarColor(CommonUtils().getThemeColor(context, android.R.attr.colorBackground))
        activity.setBottomNavVisible(false)

        setupMap()
        if(searchRideViewModel.origin.value == null) {
            performOriginCurrentPlaceRequest()      // Get current location
        }

        // GOOGLE MAP
        searchRideViewModel.origin.observe(viewLifecycleOwner) { searchLocation ->
            if(searchLocation != null) {
                originNameText.text = searchLocation.name
                originDistanceAddress.text = searchLocation.detailAddress
                updateMap()
            }
        }


        // NAVIGATION EVENT LISTENERS
        setupNavigationListener()

        return binding.root
    }



    // CUSTOMIZE METHODS
    @SuppressLint("MissingPermission")
    private fun setupMap() {
        // Setup Google Map
        mapFragment.getMapAsync {googleMap ->
            // Map Settings
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            googleMap.uiSettings.isMapToolbarEnabled = false


            currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) { currentLocation ->
                updateMap(originLocation = currentLocation)

                GoogleMapUtils().setupMapListeners(googleMap, currentLocation, myLocationBtn,
                    object: GoogleMapUtils.MyLocationButtonCallback {
                        override fun showMyLocationButton(show: Boolean) {
                            showMyLocationBtn(show)
                        }
                    })
            }
        }
    }

    private fun setupNavigationListener() {
        val backBtn = binding.cardSearchSelectOriginBackContainer
        val originDetailCard = binding.cardSearchSelectOriginOriginContainer
        val chooseOriginBtn = binding.btnSearchSelectOriginCta

        // Search Select Origin Fragment -> Search Fragment
        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_searchSelectOriginFragment_to_searchRideFragment)
        }

        // Search Select Origin Fragment -> Search Fragment
        originDetailCard.setOnClickListener {
            findNavController().navigate(R.id.action_searchSelectOriginFragment_to_searchRideFragment)
            // TODO: Focus origin edit text field
        }

        // Search Select Origin Fragment -> Ride Detail Config Fragment
        chooseOriginBtn.setOnClickListener {
            findNavController().navigate(R.id.action_searchSelectOriginFragment_to_rideDetailConfigurationFragment)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,
        onLocationResult: (LatLng) -> Unit,
        onLocationError: () -> Unit
    ) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        val latLng = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                        onLocationResult.invoke(latLng)
                    } else {
                        // Handle the case where lastKnownLocation is null
                        onLocationError.invoke()
                    }
                } else {
                    // Handle the case where the task is not successful
                    onLocationError.invoke()
                    Log.d(ContentValues.TAG, "Current location is null. Using defaults.")
                }
            }
        } catch (e: SecurityException) {
            // Handle the case where a SecurityException occurs
            onLocationError.invoke()
            Log.e("Exception: %s", e.message, e)
        }
    }


    private fun updateMap(originLocation: LatLng? = null) {
        val origin = searchRideViewModel.origin.value?.geolocation
        val destination = searchRideViewModel.destination.value?.geolocation
        val googleMapUtils = GoogleMapUtils()

        // Colors

        mapFragment.getMapAsync { googleMap ->
            val originColor = CommonUtils().getMapOriginMarkerColor(context)
            val destinationColor = CommonUtils().getMapDestMarkerColor(context)
            val originLocationIcon = CommonUtils().getLocationBitmapFromVector(context, originColor)
            val destinationLocationIcon = CommonUtils().getLocationBitmapFromVector(context, destinationColor)

            googleMap.setOnMapLoadedCallback {
                googleMap.clear()   // Clear previous markers

                // Add origin marker
                originLocation?.let {
                    googleMapUtils.addMarker(googleMap, it, originLocationIcon)
                } ?: origin?.let {
                    googleMapUtils.addMarker(googleMap, it, originLocationIcon)
                }

                // Add destination marker
                destination?.let {
                    googleMapUtils.addMarker(googleMap, it, destinationLocationIcon)
                }

                googleMapUtils.updateMapZoomAndCamera(context, googleMap, origin, destination)


                // Draw Route
                if(origin != null && destination != null) {
                    if (searchRideViewModel.rideRoute.value.isNullOrEmpty()) {
                        googleMapUtils.calculateDirections(
                            context,
                            origin,
                            destination,
                            false,
                        ) { result ->
                            if (result != null) {
                                Handler(Looper.getMainLooper()).post {
                                    for (route in result.routes) {
                                        val decodedPath =
                                            PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                                        val newDecodedPath = mutableListOf<LatLng>()

                                        for (latLng in decodedPath) {
                                            newDecodedPath.add(LatLng(latLng.lat, latLng.lng))
                                        }

                                        val polyline: Polyline = googleMap.addPolyline(
                                            PolylineOptions().addAll(newDecodedPath).clickable(true)
                                        )
                                        polyline.color = CommonUtils().getThemeColor(
                                            context,
                                            com.google.android.material.R.attr.colorOnSurfaceInverse
                                        )

                                        searchRideViewModel.setRideRoute(newDecodedPath)
                                    }
                                }
                            }
                        }
                    }

                    searchRideViewModel.rideRoute.observe(viewLifecycleOwner) { rideRoute ->
                        if (rideRoute != null) {
                            val polyline: Polyline =
                                googleMap.addPolyline(PolylineOptions().addAll(rideRoute))
                            polyline.color = CommonUtils().getThemeColor(
                                context,
                                com.google.android.material.R.attr.colorOnSurfaceInverse
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showMyLocationBtn(show: Boolean) {
        myLocationBtn.visibility = if (show) View.VISIBLE else View.GONE
    }


    // LOCATION RELATED METHODS
    private fun performOriginCurrentPlaceRequest() {
        // Use fields to define the data types to return.
        val placeFields: List<Place.Field> = listOf(
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.ID)

        // Use the builder to create a FindCurrentPlaceRequest.
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {

            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result
                    val highestLikelihoodPlace = response?.placeLikelihoods?.maxByOrNull { it.likelihood }

                    highestLikelihoodPlace?.let {placeLikelihood ->
                        val originPlace = SearchLocation(
                            placeId = placeLikelihood.place.id ?: "",
                            name = placeLikelihood.place.name ?: "Name Not Found",
                            detailAddress = placeLikelihood.place.address ?: "Address Not Found",
                            geolocation = placeLikelihood.place.latLng
                        )

                        searchRideViewModel.setOrigin(originPlace)
                    }
                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.e(ContentValues.TAG, "Place not found: ${exception.statusCode}")
                    }
                }
            }
        } else {
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
            //getLocationPermission()
        }
    }
}
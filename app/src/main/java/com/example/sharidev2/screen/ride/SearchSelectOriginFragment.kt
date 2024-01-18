package com.example.sharidev2.screen.ride

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.VectorDrawable
import android.location.Location
import android.os.Bundle
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
import com.example.sharidev2.databinding.FragmentSearchSelectOriginBinding
import com.example.sharidev2.viewmodel.SearchRideViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.card.MaterialCardView

class SearchSelectOriginFragment : Fragment() {
    private lateinit var binding: FragmentSearchSelectOriginBinding
    private val searchRideViewModel: SearchRideViewModel by activityViewModels()
    private lateinit var myLocationBtn: MaterialCardView

    private val locationCamera = Location("Camera")
    private val locationUser = Location("User")
    private val ZOOM_INDEX = 17.8f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_search_select_origin,
            container,
            false
        )

        // ELEMENT VARIABLES
        val backBtn = binding.cardSearchSelectOriginBackContainer
        myLocationBtn = binding.cardSearchSelectOriginMyLocationContainer
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_search_origin_container) as SupportMapFragment
        val originNameText = binding.textSearchSelectOriginLocationName
        val originDistanceAddress = binding.textSearchSelectOriginLocationDistanceAddress
        val chooseOriginBtn = binding.btnSearchSelectOriginCta



        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)


        //Toast.makeText(requireContext(), searchRideViewModel.origin.value?.name.toString(), Toast.LENGTH_SHORT).show()
        // GOOGLE MAP
        searchRideViewModel.origin.observe(viewLifecycleOwner) { searchLocation ->

            mapFragment.getMapAsync { googleMap ->
                // Handle the GoogleMap instance
                // You can use the googleMap object to add markers, set camera position, etc.
                val fusedLocationProviderClient = FusedLocationProviderClient(requireContext())

                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = false

                getDeviceLocation(fusedLocationProviderClient,
                    onLocationResult = { currentLocation ->
                        searchLocation.geolocation?.let { location ->
                            val originMarker = MarkerOptions().position(location)
                                .icon(getOriginMarkerBitmap(requireContext()))

                            googleMap.addMarker(originMarker)
                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLocation,
                                    ZOOM_INDEX
                                )
                            )
                        }

                    },
                    onLocationError = {
                        // Handle the case where there's an error getting the device location
                        // Toast.makeText(requireContext(), "Error getting device location", Toast.LENGTH_SHORT).show()
                    }
                )

                setupMapListeners(googleMap, fusedLocationProviderClient)
            }

            originNameText.text = searchLocation.name
            originDistanceAddress.text = searchLocation.detailAddress
        }


        // NAVIGATION EVENT LISTENERS
        // Search Select Origin Fragment -> Search Fragment
        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_searchSelectOriginFragment_to_searchFragment)
        }

        // Search Select Origin Fragment -> Ride Detail Config Fragment
        chooseOriginBtn.setOnClickListener {
            findNavController().navigate(R.id.action_searchSelectOriginFragment_to_rideDetailConfigurationFragment)
        }

        return binding.root
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

    private fun setupMapListeners(
        googleMap: GoogleMap,
        fusedLocationProviderClient: FusedLocationProviderClient
    ) {
        googleMap.setOnCameraMoveListener {
            handleCameraMove(googleMap, fusedLocationProviderClient)
            true
        }

        myLocationBtn.setOnClickListener {
            handleMyLocationButtonClick(googleMap, fusedLocationProviderClient)
            true
        }
    }

    private fun handleCameraMove(
        googleMap: GoogleMap,
        fusedLocationProviderClient: FusedLocationProviderClient
    ) {
        getDeviceLocation(
            fusedLocationProviderClient,
            onLocationResult = { currentLocation ->
                val currentCameraPosition = googleMap.cameraPosition.target

                if (!isMapOnCurrentLocation(currentCameraPosition, currentLocation)) {
                    showMyLocationButton(true)
                } else {
                    showMyLocationButton(false)
                }
            },
            onLocationError = {
                // Toast.makeText(requireContext(), "Error getting device location", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun handleMyLocationButtonClick(
        googleMap: GoogleMap,
        fusedLocationProviderClient: FusedLocationProviderClient
    ) {
        getDeviceLocation(
            fusedLocationProviderClient,
            onLocationResult = { currentLocation ->
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLocation,
                        ZOOM_INDEX
                    )
                )
            },
            onLocationError = {
                // Toast.makeText(requireContext(), "Error getting device location", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun isMapOnCurrentLocation(
        currentCameraPosition: LatLng,
        currentLocation: LatLng
    ): Boolean {
        locationCamera.latitude = currentCameraPosition.latitude
        locationCamera.longitude = currentCameraPosition.longitude

        locationUser.latitude = currentLocation.latitude
        locationUser.longitude = currentLocation.longitude

        val distance = locationCamera.distanceTo(locationUser)

        return distance < 0.03f
    }

    private fun showMyLocationButton(show: Boolean) {
        myLocationBtn.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun getOriginMarkerBitmap(context: Context): BitmapDescriptor {
        val SCALE_FACTOR = 2.0f

        // Create a VectorDrawable from the default marker resource
        val vectorDrawable =
            ContextCompat.getDrawable(context, R.drawable.location) as VectorDrawable

        val colorPrimary = Color.parseColor("#246489")
        vectorDrawable.setColorFilter(colorPrimary, PorterDuff.Mode.SRC_IN)

        val bitmapWidth = (vectorDrawable.intrinsicWidth * SCALE_FACTOR).toInt()
        val bitmapHeight = (vectorDrawable.intrinsicHeight * SCALE_FACTOR).toInt()

        // Convert the VectorDrawable to a BitmapDescriptor
        val bitmap = Bitmap.createBitmap(
            bitmapWidth,
            bitmapHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
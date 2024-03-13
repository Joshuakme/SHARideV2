package com.example.sharidev2.screen.ride

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.adapter.SearchRideAdapter
import com.example.sharidev2.data.model.PolylineData
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.databinding.FragmentDriverCreateRideBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.GoogleMapUtils
import com.example.sharidev2.utility.NetworkUtils
import com.example.sharidev2.viewmodel.CurrentLocationViewModel
import com.example.sharidev2.viewmodel.SharedCreateRideViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.internal.PolylineEncoding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DriverCreateRideFragment : Fragment() {
    private lateinit var binding: FragmentDriverCreateRideBinding
    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModels()
    private val createRideViewModel: SharedCreateRideViewModel by activityViewModels()

    private lateinit var placesClient: PlacesClient
    private lateinit var searchResultRecyclerView: RecyclerView
    private lateinit var searchResultAdapter: SearchRideAdapter
    private lateinit var mapFragment: SupportMapFragment

    // Flags
    private var isOriginFocused = false
    private var isDestinationFocused = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_create_ride, container, false)


        // VARIABLES INIT
        placesClient = Places.createClient(requireContext())


        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnDriverCreateRideNavBack
        val originLocationEditText = binding.editTextCreateRideOriginLocation
        val destinationLocationEditText = binding.editTextCreateRideDestinationLocation
        mapFragment = childFragmentManager.findFragmentById(R.id.map_driver_create_ride_container) as SupportMapFragment
        val nextBtn = binding.btnDriverCreateRideCtaNext


        searchResultRecyclerView = binding.recyclerViewDriverCreateRide


        // LAYOUT SETTINGS
        setupMap()
        if(createRideViewModel.origin.value?.placeId == null) {
            performOriginCurrentPlaceRequest()      // Get current location
        }

        createRideViewModel.origin.observe(viewLifecycleOwner) { searchLocation ->
            originLocationEditText.setText(searchLocation.name)
            destinationLocationEditText.requestFocus()
            updateMap()
        }

        createRideViewModel.destination.observe(viewLifecycleOwner) { destinationSearchLocation ->
            destinationLocationEditText.setText(destinationSearchLocation.name)
            destinationLocationEditText.clearFocus()
            nextBtn.requestFocus()
            updateMap()
        }

        // EVENT LISTENERS
        setupOriginEditText()
        setupDestinationEditText()


        // NAVIGATION EVENT LISTENERS
        // Driver Add Ride Fragment -> Search Fragment
        backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        // Driver Add Ride Fragment -> Driver Ride Config Fragment
        nextBtn.setOnClickListener {
            findNavController().navigate(R.id.action_driverCreateRideFragment_to_driverRideConfigFragment)
            createRideViewModel.saveRoutePath()
        }


        return binding.root
    }



    // CUSTOMIZE METHODS

    private fun setupOriginEditText() {
        val originLocationEditText = binding.editTextCreateRideOriginLocation
        val originEditTextCancelButton = binding.imgBtnDriverCreateRideOriginCancel

        originLocationEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // Unused
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                if(!charSequence.isNullOrBlank()) {
                    showSearchResultCard(true)
                    originEditTextCancelButton.visibility = View.VISIBLE
                } else {
                    showSearchResultCard(false)
                    originEditTextCancelButton.visibility = View.INVISIBLE
                }


                // Trigger search on text change
                charSequence?.toString()?.let { query ->

                    var currentLocation: LatLng? = null

                    currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) {
                        currentLocation = it

                        NetworkUtils(requireContext()).showNetworkStatus()

                        performLocationAutocompleteRequest(query, currentLocation)
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {
                // Unused
            }

        })

        // FOCUS CHANGE LISTENER
        originLocationEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            focusOriginEditText(hasFocus)
        }

        originEditTextCancelButton.setOnClickListener {
            originLocationEditText.text.clear()
        }
    }

    private fun setupDestinationEditText() {
        val destinationLocationEditText = binding.editTextCreateRideDestinationLocation
        val destinationEditTextCancelButton = binding.imgBtnDriverCreateRideDestinationCancel

        destinationLocationEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // Unused
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                if(!charSequence.isNullOrBlank()) {
                    showSearchResultCard(true)
                    destinationEditTextCancelButton.visibility = View.VISIBLE
                } else {
                    showSearchResultCard(false)
                    destinationEditTextCancelButton.visibility = View.INVISIBLE
                }


                // Trigger search on text change
                charSequence?.toString()?.let { query ->

                    var currentLocation: LatLng? = null

                    currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) {
                        currentLocation = it

                        NetworkUtils(requireContext()).showNetworkStatus()

                        performLocationAutocompleteRequest(query, currentLocation)
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {
                // Unused
            }

        })

        // FOCUS CHANGE LISTENER
        destinationLocationEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            focusDestinationEditText(hasFocus)
        }

        destinationEditTextCancelButton.setOnClickListener {
            destinationLocationEditText.text.clear()
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        val myLocationBtn = binding.cardDriverCreateRideMyLocationContainer

        // Setup Google Map
        mapFragment.getMapAsync {googleMap ->
            // Map Settings
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            googleMap.uiSettings.isMapToolbarEnabled = false



            // Draw marker
            currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) { origin ->
                updateMap(originLocation = origin)
                GoogleMapUtils().setupMapListeners(googleMap, origin, myLocationBtn,
                    object: GoogleMapUtils.MyLocationButtonCallback {
                        override fun showMyLocationButton(show: Boolean) {
                            showMyLocationBtn(show)
                        }
                    })
            }
        }
    }

    // BEHAVIOURAL METHODS
    private fun showSearchResultCard(show: Boolean) {
        val searchResultCard = binding.cardDriverCreateRideSearchResultRecyclerView

        searchResultCard.visibility = if(show) View.VISIBLE else View.GONE
    }

    private fun focusOriginEditText(focus: Boolean) {
        val pickUpLocationEditTextCard = binding.cardCreateRidePickUpLocation
        val destinationLocationEditTextCard = binding.cardCreateRideDestinationLocation
        val originLocationEditText = binding.editTextCreateRideOriginLocation
        val originEditTextCancelButton = binding.imgBtnDriverCreateRideOriginCancel
        val destinationEditTextCancelButton = binding.imgBtnDriverCreateRideDestinationCancel


        val typedValue = TypedValue()
        // Resolve the attribute to get the color value programmatically
        context?.theme?.resolveAttribute(com.google.android.material.R.attr.colorOnBackground, typedValue, true)
        val colorOnBackground = typedValue.data
        context?.theme?.resolveAttribute(com.google.android.material.R.attr.colorOutlineVariant, typedValue, true)
        val colorOutlineVariant = typedValue.data


        if (focus) {
            isOriginFocused = true
            isDestinationFocused = false

            pickUpLocationEditTextCard.strokeColor = colorOnBackground
            destinationLocationEditTextCard.strokeColor = colorOutlineVariant

            if(originLocationEditText.text.isNullOrEmpty()) {
                originEditTextCancelButton.visibility = View.INVISIBLE
                showSearchResultCard(false)
            } else {
                originEditTextCancelButton.visibility = View.VISIBLE
            }
            destinationEditTextCancelButton.visibility = View.INVISIBLE

            updateSearchResultRecyclerViewPosition()
        } else {
            isOriginFocused = false
            showSearchResultCard(false)

            pickUpLocationEditTextCard.strokeColor = colorOutlineVariant
            destinationLocationEditTextCard.strokeColor = colorOutlineVariant

            originEditTextCancelButton.visibility = View.INVISIBLE
        }
    }

    private fun focusDestinationEditText(focus: Boolean) {
        val pickUpLocationEditTextCard = binding.cardCreateRidePickUpLocation
        val destinationLocationEditTextCard = binding.cardCreateRideDestinationLocation
        val destinationLocationEditText = binding.editTextCreateRideDestinationLocation
        val originEditTextCancelButton = binding.imgBtnDriverCreateRideOriginCancel
        val destinationEditTextCancelButton = binding.imgBtnDriverCreateRideDestinationCancel

        val typedValue = TypedValue()
        // Resolve the attribute to get the color value programmatically
        context?.theme?.resolveAttribute(com.google.android.material.R.attr.colorOnBackground, typedValue, true)
        val colorOnBackground = typedValue.data
        context?.theme?.resolveAttribute(com.google.android.material.R.attr.colorOutlineVariant, typedValue, true)
        val colorOutlineVariant = typedValue.data


        if (focus) {
            isOriginFocused = false
            isDestinationFocused = true

            pickUpLocationEditTextCard.strokeColor = colorOutlineVariant
            destinationLocationEditTextCard.strokeColor = colorOnBackground

            if(destinationLocationEditText.text.isNullOrEmpty() || destinationLocationEditText.text.isNullOrBlank()) {
                destinationEditTextCancelButton.visibility = View.INVISIBLE
                showSearchResultCard(false)
            } else {
                destinationEditTextCancelButton.visibility = View.VISIBLE
            }
            originEditTextCancelButton.visibility = View.INVISIBLE

            updateSearchResultRecyclerViewPosition()
        } else {
            isDestinationFocused = false
            showSearchResultCard(false)

            pickUpLocationEditTextCard.strokeColor = colorOutlineVariant
            destinationLocationEditTextCard.strokeColor = colorOutlineVariant
            destinationEditTextCancelButton.visibility = View.INVISIBLE

        }
    }

    private fun updateSearchResultRecyclerViewPosition() {
        val recyclerViewCard = binding.cardDriverCreateRideSearchResultRecyclerView
        val constraintLayout = binding.constraintDriverCreateRideLocation
        val constraintSet = ConstraintSet()

        constraintSet.clone(constraintLayout)
        if(isOriginFocused)
            constraintSet.connect(recyclerViewCard.id, ConstraintSet.TOP, R.id.card_create_ride_pick_up_location, ConstraintSet.BOTTOM)
        else
            constraintSet.connect(recyclerViewCard.id, ConstraintSet.TOP, R.id.card_create_ride_destination_location, ConstraintSet.BOTTOM)

        constraintSet.applyTo(constraintLayout)
    }

    // GOOGLE MAP RELATED METHODS



    private fun updateMap(originLocation: LatLng? = null) {
        val origin = createRideViewModel.origin.value?.geolocation
        val destination = createRideViewModel.destination.value?.geolocation
        val googleMapUtils = GoogleMapUtils()

        // Colors
        val originColor = CommonUtils().getThemeColor(requireContext(), com.google.android.material.R.attr.colorPrimary)
        val destinationColor = CommonUtils().getThemeColor(requireContext(), com.google.android.material.R.attr.colorError)

        mapFragment.getMapAsync { googleMap ->
            val originLocationIcon = CommonUtils().getLocationBitmapFromVector(requireContext(), originColor)
            val destinationLocationIcon = CommonUtils().getLocationBitmapFromVector(requireContext(), destinationColor)

            googleMap.setOnMapLoadedCallback {
                googleMap.clear()   // Clear previous markers
                // Disable marker onclick event
                googleMap.setOnMarkerClickListener {
                    true
                }

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

                googleMapUtils.updateMapZoomAndCamera(requireContext(), googleMap, origin, destination)


                // Draw Route
                if(origin != null && destination != null) {
                    createRideViewModel.rideRoute.observe(viewLifecycleOwner) {rideRoute ->
                        if(rideRoute != null) {
                            val polyline: Polyline = googleMap.addPolyline(PolylineOptions().addAll(rideRoute))
                            polyline.color = CommonUtils().getThemeColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceInverse)
                        } else {
                            googleMapUtils.calculateDirections(
                                requireContext(),
                                origin,
                                destination,
                                false,
                            ) {result ->
                                if(result != null) {
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
                                            polyline.color = CommonUtils().getThemeColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceInverse)

                                            createRideViewModel.setRoutePath(newDecodedPath)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showMyLocationBtn(show: Boolean) {
        val myLocationBtn = binding.cardDriverCreateRideMyLocationContainer

        myLocationBtn.visibility = if (show) View.VISIBLE else View.GONE
    }



    // LOCATION RELATED METHODS
    @SuppressLint("MissingPermission")
    private fun performLocationAutocompleteRequest(query: String, currentLocation: LatLng?) {
        // Perform autocomplete predictions
        val autocompleteRequest = AutocompleteSessionToken.newInstance()

        val request = if (currentLocation != null) {
            buildLocationAutocompleteRequestWithLocation(autocompleteRequest, query, currentLocation)
        } else {
            buildLocationAutocompleteRequestWithLocation(autocompleteRequest, query)
        }

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                if(isAdded) {   // check if the fragment is attached to the parent
                    val locationList = getPredictionList(response.autocompletePredictions)

                    if(locationList.isNotEmpty()) {
                        val locationLoadingProgressBar = binding.progressBarDriverCreateRideLocation
                        val recyclerView = binding.recyclerViewDriverCreateRide
                        locationLoadingProgressBar.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE

                        searchResultAdapter =
                            SearchRideAdapter(requireContext(), locationList) { selectedLocation ->
                                // Determine if the user is focusing on origin or destination
                                if (isOriginFocused) {
                                    createRideViewModel.setOrigin(selectedLocation)
                                } else if (isDestinationFocused) {
                                    createRideViewModel.setDestination(selectedLocation)
                                } else {
                                    // Focus lost, do nothing..
                                }

                                CommonUtils().closeKeyboard(requireView(), requireContext())
                                showSearchResultCard(false)
                            }
                        searchResultRecyclerView.layoutManager = LinearLayoutManager(context)
                        searchResultRecyclerView.adapter = searchResultAdapter
                    }
                }
            }
            .addOnFailureListener {
                Log.e(tag, it.message.toString())
            }
    }

    private fun buildLocationAutocompleteRequestWithLocation(
        autocompleteRequest: AutocompleteSessionToken,
        query: String,
        currentLocation: LatLng? = null
    ): FindAutocompletePredictionsRequest {
        val bounds = RectangularBounds.newInstance(
            LatLng(1.093836, 100.934026),  // Southwest corner of Malaysia
            LatLng(7.358882, 119.308789)  // Northeast corner of Malaysia
        )

        return FindAutocompletePredictionsRequest.builder()
            .setSessionToken(autocompleteRequest)
            .setQuery(query)
            .setCountries("MY")
            .setOrigin(currentLocation)
            .setLocationRestriction(bounds)
            .build()
    }

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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
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

                        createRideViewModel.setOrigin(originPlace)
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

    private fun getPredictionList(predictions: List<AutocompletePrediction>): List<SearchLocation> {
        return predictions.map { prediction ->
            SearchLocation(
                placeId = prediction.placeId,
                name = prediction.getPrimaryText(null).toString(),
                distanceMetersFromOrigin = prediction.distanceMeters ?: 0,
                detailAddress = prediction.getFullText(null).toString(),
            ).apply {
                getLatLngFromPlaceId(prediction.placeId) { latLng ->
                    geolocation = latLng
                    // Notify the adapter that data has changed
                    searchResultAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun getLatLngFromPlaceId(placeId: String, callback: (LatLng?) -> Unit) {
        val placeFields = listOf(Place.Field.LAT_LNG)
        val fetchPlaceRequest = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(fetchPlaceRequest)
            .addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng
                callback.invoke(latLng)
            }
            .addOnFailureListener { exception ->
                // Handle failure to fetch place details
                Toast.makeText(requireContext(), exception.toString(), Toast.LENGTH_SHORT).show()
                Log.e("EXCEPTION BABIIIIII", exception.toString())
                callback.invoke(null)
            }
    }

}
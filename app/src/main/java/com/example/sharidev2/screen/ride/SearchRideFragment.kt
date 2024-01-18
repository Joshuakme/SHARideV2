package com.example.sharidev2.screen.ride

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.adapter.SearchRideAdapter
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.databinding.FragmentSearchBinding
import com.example.sharidev2.utility.NetworkUtils
import com.example.sharidev2.viewmodel.CurrentLocationViewModel
import com.example.sharidev2.viewmodel.SearchRideViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlin.math.cos

class SearchRideFragment : Fragment() {
    // Global Variables Init
    private val REQUEST_LOCATION_PERMISSION = 123 // You can use any unique integer value
    private lateinit var binding: FragmentSearchBinding
    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModels()
    private val searchRideViewModel: SearchRideViewModel by activityViewModels()
    private lateinit var placesClient: PlacesClient
    private lateinit var searchResultAdapter: SearchRideAdapter
    private lateinit var searchResultRecyclerView: RecyclerView

    // Flags
    private var isOriginFocused = false
    private var isDestinationFocused = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)


        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnSearchBack
        val pickUpLocationEditText = binding.editTextOfferRidePickUpLocation
        val pickUpLocationEditTextCard = binding.cardOfferRidePickUpLocation
        val destinationLocationEditText = binding.editTextOfferRideDestinationLocation
        val destinationLocationEditTextCard = binding.cardOfferRideDestinationLocation
        searchResultRecyclerView = binding.recyclerSearchPlaceResult
        placesClient = Places.createClient(requireContext())

        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)



        // BEHAVIOUR EVENT LISTENERS
        // Get the color value programmatically
        // Create a TypedValue object to hold the resolved attribute value
        val typedValue = TypedValue()

        // Resolve the attribute to get the color value programmatically
        context?.theme?.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainer, typedValue, true)
        val colorSurfaceContainer = typedValue.data

        pickUpLocationEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            isOriginFocused = hasFocus

            if (hasFocus) {
                // Change background color when focused
                pickUpLocationEditTextCard.setCardBackgroundColor(colorSurfaceContainer)
                destinationLocationEditTextCard.setCardBackgroundColor(Color.TRANSPARENT)
            } else {
                // Change background color when not focused
                pickUpLocationEditTextCard.setCardBackgroundColor(Color.TRANSPARENT)
            }
        }

        destinationLocationEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            isDestinationFocused = hasFocus

            if (hasFocus) {
                // Change background color when focused
                destinationLocationEditTextCard.setCardBackgroundColor(colorSurfaceContainer)
                pickUpLocationEditTextCard.setCardBackgroundColor(Color.TRANSPARENT)
            } else {
                // Change background color when not focused
                destinationLocationEditTextCard.setCardBackgroundColor(Color.TRANSPARENT)
            }
        }


        // DATA FETCHING
        destinationLocationEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // Unused
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // Trigger search on text change
                charSequence?.toString()?.let { query ->

                    var currentLocation: LatLng? = null

                    currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) {
                        currentLocation = it

                        NetworkUtils(requireContext()).showNetworkStatus()

                        performDestinationAutocompleteRequest(query, currentLocation)
                    }
                }
            }

            override fun afterTextChanged(editable: Editable?) {
                // Unused
            }
        })



        // NAVIGATION EVENT LISTENERS
        // Search Fragment -> Home Fragment
        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_homeFragment)
        }

        return binding.root
    }


    private fun performOriginAutocompleteRequest(query: String? = null, currentLocation: LatLng?) {
        // Perform autocomplete predictions
        val autocompleteToken = AutocompleteSessionToken.newInstance()

        if(currentLocation == null) return

        // Build a request for autocomplete predictions based on the current location
        if(query != null) {
            val request = buildOriginCurrentPlaceRequest(autocompleteToken, query, currentLocation)

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    val predictions: List<AutocompletePrediction> = response.autocompletePredictions

                    Toast.makeText(
                        requireContext(),
                        predictions.isEmpty().toString(),
                        Toast.LENGTH_SHORT
                    ).show()

                    if (predictions.isNotEmpty()) {
                        val nearestPlace = predictions[0] // Assuming the first prediction is the nearest
                        val placeId = nearestPlace.placeId

                        Toast.makeText(
                            requireContext(),
                            nearestPlace.getPrimaryText(null),
                            Toast.LENGTH_SHORT
                        ).show()

                        // Now, you can use placeId to fetch the details of the place, including latitude and longitude
                        getLatLngFromPlaceId(placeId) { latLng ->
                            // Now you have the latitude and longitude of the nearest place
                            // Update your ViewModel here
                            searchRideViewModel.setOrigin(
                                SearchLocation(
                                    placeId = nearestPlace.placeId,
                                    name = nearestPlace.getPrimaryText(null).toString(),
                                    distanceMetersFromOrigin = nearestPlace.distanceMeters ?: 0,
                                    detailAddress = nearestPlace.getFullText(null).toString(),
                                ).apply {
                                geolocation = latLng
                            }
                            )
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    Toast.makeText(
                        requireContext(),
                        exception.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
//            val request = buildOriginCurrentPlaceRequest()

//            findNearestPlace(request, currentLocation)

            val request = buildOriginAutocompleteRequestWithLocation(autocompleteToken, currentLocation)

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    val predictions: List<AutocompletePrediction> = response.autocompletePredictions

                    Toast.makeText(
                        requireContext(),
                        predictions.isEmpty().toString(),
                        Toast.LENGTH_SHORT
                    ).show()

                    if (predictions.isNotEmpty()) {
                        val nearestPlace = predictions[0] // Assuming the first prediction is the nearest
                        val placeId = nearestPlace.placeId

                        Toast.makeText(
                            requireContext(),
                            nearestPlace.getPrimaryText(null),
                            Toast.LENGTH_SHORT
                        ).show()

                        // Now, you can use placeId to fetch the details of the place, including latitude and longitude
                        getLatLngFromPlaceId(placeId) { latLng ->
                            // Now you have the latitude and longitude of the nearest place
                            // Update your ViewModel here
                            searchRideViewModel.setOrigin(
                                SearchLocation(
                                    placeId = nearestPlace.placeId,
                                    name = nearestPlace.getPrimaryText(null).toString(),
                                    distanceMetersFromOrigin = nearestPlace.distanceMeters ?: 0,
                                    detailAddress = nearestPlace.getFullText(null).toString(),
                                ).apply {
                                geolocation = latLng
                            }
                            )
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    Toast.makeText(
                        requireContext(),
                        exception.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

    }


    private fun findNearestPlace(
        request: FindCurrentPlaceRequest,
        currentLocation: LatLng
    ) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            placesClient.findCurrentPlace(request)
                .addOnSuccessListener { response ->
                    val placeLikelihoods = response.placeLikelihoods

                    if (placeLikelihoods.isNotEmpty()) {
                        // Assuming the first place is the nearest
                        val nearestPlace = placeLikelihoods[0].place
                        val placeId = nearestPlace.id

                        val distanceMeters = calculateDistance(
                            currentLocation.latitude,
                            currentLocation.longitude,
                            nearestPlace.latLng?.latitude ?: 0.0,
                            nearestPlace.latLng?.longitude ?: 0.0
                        ).toInt()

                        // Now, you can use placeId to fetch the details of the place, including latitude and longitude
                        if (placeId != null) {
                            getLatLngFromPlaceId(placeId) { latLng ->
                                // Now you have the latitude and longitude of the nearest place
                                // Create a SearchLocation instance
                                val nearestSearchLocation = SearchLocation(
                                    placeId = placeId,
                                    name = nearestPlace.name.toString(),
                                    distanceMetersFromOrigin = distanceMeters,
                                    detailAddress = nearestPlace.address.toString()
                                ).apply {
                                    geolocation = latLng
                                }

                                // Use the nearestSearchLocation as needed (e.g., update ViewModel)
                                searchRideViewModel.setOrigin(nearestSearchLocation)
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "No places found!!!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } else {
                        // Handle the case where no places are found
                        Toast.makeText(requireContext(), "No places found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    Toast.makeText(
                        requireContext(),
                        exception.message.toString(),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
        }
    }

    private fun buildOriginCurrentPlaceRequest(
        autocompleteRequest: AutocompleteSessionToken,
        query: String,
        currentLocation: LatLng,
        radiusMeters: Int = 1000
    ): FindAutocompletePredictionsRequest {
        val bounds = RectangularBounds.newInstance(
            LatLng(
                currentLocation.latitude - radiusMeters / 111000.0,
                currentLocation.longitude - radiusMeters / (111000.0 * cos(
                    Math.toRadians(currentLocation.latitude)
                ))
            ),
            LatLng(
                currentLocation.latitude + radiusMeters / 111000.0,
                currentLocation.longitude + radiusMeters / (111000.0 * cos(
                    Math.toRadians(currentLocation.latitude)
                ))
            )
        )

        return FindAutocompletePredictionsRequest.builder()
                .setSessionToken(autocompleteRequest)
                .setQuery(query)
                .setCountries("MY")
                .setOrigin(currentLocation)
                .setLocationRestriction(bounds)
                .build()
    }

    private fun buildOriginAutocompleteRequestWithLocation(
        autocompleteRequest: AutocompleteSessionToken,
        currentLocation: LatLng
    ): FindAutocompletePredictionsRequest {
        val bounds = RectangularBounds.newInstance(
            LatLng(1.093836, 100.934026),  // Southwest corner of Malaysia
            LatLng(7.358882, 119.308789)  // Northeast corner of Malaysia
        )

        return FindAutocompletePredictionsRequest.builder()
            .setSessionToken(autocompleteRequest)
            .setQuery("PV9")
            .setCountries("MY")
            .setOrigin(currentLocation)
            .setLocationRestriction(bounds)
            .build()
    }

    private fun buildOriginCurrentPlaceRequest(): FindCurrentPlaceRequest {

        return FindCurrentPlaceRequest.newInstance(listOf(Place.Field.LAT_LNG))
    }


    private fun performDestinationAutocompleteRequest(query: String, currentLocation: LatLng?) {

        // Perform autocomplete predictions
        val autocompleteRequest = AutocompleteSessionToken.newInstance()

        val request = if (currentLocation != null) {
            buildDestinationAutocompleteRequestWithLocation(autocompleteRequest, query, currentLocation)
        } else {
            buildDestinationAutocompleteRequestWithoutLocation(autocompleteRequest, query)
        }

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                // Handle the response and update your UI with the predictions
                val predictions: List<AutocompletePrediction> = response.autocompletePredictions

                val locationList = predictions.map { prediction ->
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

                // Pass list of data to adapter
                searchResultAdapter =
                    SearchRideAdapter(requireContext(), locationList) { selectedLocation ->
                        // Determine if the user is focusing on origin or destination
                        if (isOriginFocused) {
                            searchRideViewModel.setOrigin(selectedLocation)
                        } else if (isDestinationFocused) {
                            searchRideViewModel.setDestination(selectedLocation)
                        } else {
                            Toast.makeText(requireContext(), "focus lost", Toast.LENGTH_SHORT)
                                .show()
                        }

                        performOriginAutocompleteRequest(currentLocation = currentLocation)

                        findNavController().navigate(R.id.action_searchFragment_to_searchSelectOriginFragment)
                    }
                searchResultRecyclerView.layoutManager = LinearLayoutManager(context)
                searchResultRecyclerView.adapter = searchResultAdapter
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Toast.makeText(requireContext(), exception.toString(), Toast.LENGTH_SHORT).show()
                Log.e("EXCEPTION BABIIIIII", exception.toString())
            }
    }

    private fun buildDestinationAutocompleteRequestWithLocation(
        autocompleteRequest: AutocompleteSessionToken,
        query: String,
        currentLocation: LatLng
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

    private fun buildDestinationAutocompleteRequestWithoutLocation(
        autocompleteRequest: AutocompleteSessionToken,
        query: String
    ): FindAutocompletePredictionsRequest {
        val bounds = RectangularBounds.newInstance(
            LatLng(1.093836, 100.934026),  // Southwest corner of Malaysia
            LatLng(7.358882, 119.308789)  // Northeast corner of Malaysia
        )

        return FindAutocompletePredictionsRequest.builder()
            .setSessionToken(autocompleteRequest)
            .setQuery(query)
            .setCountries("MY")
            .setLocationRestriction(bounds)
            .build()
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

    // Function to calculate distance in meters between two locations
    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val result = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return result[0]
    }
}
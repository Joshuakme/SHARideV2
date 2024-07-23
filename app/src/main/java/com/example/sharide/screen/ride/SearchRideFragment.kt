package com.example.sharide.screen.ride

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharide.MainActivity
import com.example.sharide.R
import com.example.sharide.adapter.SearchRideAdapter
import com.example.sharide.data.model.SearchLocation
import com.example.sharide.databinding.FragmentSearchRideBinding
import com.example.sharide.utility.CommonUtils
import com.example.sharide.utility.NetworkUtils
import com.example.sharide.viewmodel.CurrentLocationViewModel
import com.example.sharide.viewmodel.SharedSearchRideViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.Timestamp

class SearchRideFragment : Fragment() {
    // Global Variables Init
    private lateinit var binding: FragmentSearchRideBinding
    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModels()
    private val searchRideViewModel: SharedSearchRideViewModel by activityViewModels()
    private lateinit var placesClient: PlacesClient
    private lateinit var searchResultAdapter: SearchRideAdapter
    private lateinit var searchResultRecyclerView: RecyclerView


    private lateinit var context: Context

    // Flags
    private var isOriginFocused = false
    private var isDestinationFocused = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_ride, container, false)


        // Get Context
        context = if(getContext() != null) {
            requireContext()
        } else {
            requireActivity().applicationContext
        }


        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnSearchBack
        val nowBtn = binding.cardSearchNow
        val scheduleBtn = binding.cardSearchSchedule
        val pickUpLocationEditText = binding.editTextOfferRidePickUpLocation
        val pickUpLocationCancelButton = binding.imgBtnDriverSearchRideOriginCancel
        val destinationLocationEditText = binding.editTextOfferRideDestinationLocation
        val destinationLocationCancelButton = binding.imgBtnDriverSearchRideDestinationCancel
        val createRideButton = binding.cardSearchCreateRideContainer
        searchResultRecyclerView = binding.recyclerSearchPlaceResult
        placesClient = Places.createClient(context)

        // LAYOUT SETTINGS
        val activity = activity as MainActivity
        activity.setStatusBarColor(CommonUtils().getThemeColor(context, android.R.attr.colorBackground))
        activity.setBottomNavVisible(false)


        searchRideViewModel.origin.observe(viewLifecycleOwner) {origin ->
            pickUpLocationEditText.setText(origin?.name)
        }

        searchRideViewModel.destination.observe(viewLifecycleOwner) {destination ->
            destinationLocationEditText.setText(destination?.name)
        }


        // BEHAVIOUR EVENT LISTENERS
        nowBtn.setOnClickListener {
            selectSearchNow(true)
        }

        scheduleBtn.setOnClickListener {
            selectSearchNow(false)
            showRideTimingDialog()
        }

        pickUpLocationEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            focusOriginEditText(hasFocus)

            if(pickUpLocationEditText.text.toString() == getString(R.string.search_fragment_origin_default)) {
                pickUpLocationEditText.text.clear()
            }
        }

        destinationLocationEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            focusDestinationEditText(hasFocus)
        }

        pickUpLocationCancelButton.setOnClickListener {
            pickUpLocationEditText.text.clear()
            searchRideViewModel.clearOrigin()
        }

        destinationLocationCancelButton.setOnClickListener {
            destinationLocationEditText.text.clear()
            searchRideViewModel.clearDestination()
        }


        // DATA FETCHING
        pickUpLocationEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Unused
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if(!charSequence.isNullOrBlank()) {
                    searchResultRecyclerView.visibility = View.VISIBLE
                    pickUpLocationCancelButton.visibility = View.VISIBLE
                } else {
                    searchResultRecyclerView.visibility = View.GONE
                    pickUpLocationCancelButton.visibility = View.GONE
                }

                charSequence?.toString()?.let {query ->
                    currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) {currentLocation ->

                        NetworkUtils(context).showNetworkStatus()

                        performOriginAutocompleteRequest(query, currentLocation)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Unused
            }

        })

        destinationLocationEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // Unused
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                if(!charSequence.isNullOrBlank()) {
                    searchResultRecyclerView.visibility = View.VISIBLE
                    destinationLocationCancelButton.visibility = View.VISIBLE
                } else {
                    searchResultRecyclerView.visibility = View.GONE
                    destinationLocationCancelButton.visibility = View.GONE
                }

                // Trigger search on text change
                charSequence?.toString()?.let { query ->

                    currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) {currentLocation ->

                        NetworkUtils(context).showNetworkStatus()

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
            findNavController().navigate(R.id.action_searchRideFragment_to_homeFragment)
        }

        // Search Fragment -> Driver Ride Fragment
        createRideButton.setOnClickListener {
            findNavController().navigate(R.id.action_searchRideFragment_to_driverCreateRideFragment)
        }

        return binding.root
    }


    // CUSTOM METHODS
    private fun selectSearchNow(selectNow: Boolean) {
        val nowBtn = binding.cardSearchNow
        val nowBtnText = binding.textSearchNow
        val scheduleBtn = binding.cardSearchSchedule
        val scheduleBtnText = binding.textSearchSchedule


        val colorPrimary = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorPrimary)
        val colorOnPrimary = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorOnPrimary)
        val colorOutline = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorOutline)

        val poppinsMediumTypeface = ResourcesCompat.getFont(context, R.font.poppins_medium)
        val poppinsTypeface = resources.getFont(R.font.poppins)


        if(selectNow) {
            nowBtn.setCardBackgroundColor(colorPrimary)
            nowBtnText.setTextColor(colorOnPrimary)
            nowBtnText.typeface = poppinsMediumTypeface

            scheduleBtn.setCardBackgroundColor(Color.TRANSPARENT)
            scheduleBtnText.setTextColor(colorOutline)
            scheduleBtnText.typeface = poppinsTypeface
        } else {
            nowBtn.setCardBackgroundColor(Color.TRANSPARENT)
            nowBtnText.setTextColor(colorOutline)
            nowBtnText.typeface = poppinsTypeface

            scheduleBtn.setCardBackgroundColor(colorPrimary)
            scheduleBtnText.setTextColor(colorOnPrimary)
            scheduleBtnText.typeface = poppinsMediumTypeface
        }

    }

    private fun focusOriginEditText(focus: Boolean) {
        val pickUpLocationEditText = binding.editTextOfferRidePickUpLocation
        val pickUpLocationCancelButton = binding.imgBtnDriverSearchRideOriginCancel
        val pickUpLocationEditTextCard = binding.cardOfferRidePickUpLocation
        val destinationLocationEditText = binding.editTextOfferRideDestinationLocation
        val destinationLocationEditTextCard = binding.cardOfferRideDestinationLocation


        // Resolve the attribute to get the color value programmatically
        val colorSurfaceContainer = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorSurfaceContainer)

        if(focus) {
            pickUpLocationEditTextCard.setCardBackgroundColor(colorSurfaceContainer)
            destinationLocationEditTextCard.setCardBackgroundColor(Color.TRANSPARENT)
            pickUpLocationEditText.requestFocus()
            destinationLocationEditText.clearFocus()

            isOriginFocused = true
            isDestinationFocused = false

            if(pickUpLocationEditText.text.isNullOrEmpty()) {
                searchResultRecyclerView.visibility = View.GONE
            } else {
                pickUpLocationCancelButton.visibility = View.VISIBLE
            }
        } else {
            pickUpLocationEditText.clearFocus()
            pickUpLocationEditTextCard.setCardBackgroundColor(Color.TRANSPARENT)
            pickUpLocationCancelButton.visibility = View.GONE

            isOriginFocused = false
        }

    }

    private fun focusDestinationEditText(focus: Boolean) {
        val pickUpLocationEditText = binding.editTextOfferRidePickUpLocation
        val pickUpLocationCancelButton = binding.imgBtnDriverSearchRideOriginCancel
        val pickUpLocationEditTextCard = binding.cardOfferRidePickUpLocation
        val destinationLocationEditText = binding.editTextOfferRideDestinationLocation
        val destinationLocationCancelButton = binding.imgBtnDriverSearchRideDestinationCancel
        val destinationLocationEditTextCard = binding.cardOfferRideDestinationLocation

        val colorSurfaceContainer = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorSurfaceContainer)

        if (focus) {
            // Change background color when focused
            destinationLocationEditTextCard.setCardBackgroundColor(colorSurfaceContainer)
            pickUpLocationEditTextCard.setCardBackgroundColor(Color.TRANSPARENT)
            destinationLocationEditText.requestFocus()
            pickUpLocationEditText.clearFocus()

            isOriginFocused = false
            isDestinationFocused = true

            if(destinationLocationEditText.text.isNullOrBlank()) {
                searchResultRecyclerView.visibility = View.GONE
            } else {
                searchResultRecyclerView.visibility = View.VISIBLE
                destinationLocationCancelButton.visibility = View.VISIBLE
            }

            if(pickUpLocationEditText.text.isNullOrEmpty()) {
                pickUpLocationEditText.setText(getString(R.string.search_fragment_origin_default))
                pickUpLocationCancelButton.visibility = View.GONE
            }
        } else {
            // Change background color when not focused
            destinationLocationEditTextCard.setCardBackgroundColor(Color.TRANSPARENT)
            destinationLocationEditText.clearFocus()
            destinationLocationCancelButton.visibility = View.GONE

            isDestinationFocused = false
        }
    }


    private fun showRideTimingDialog() {
        val dialogFragment = TimingBottomDialogFragment(object: TimingBottomDialogFragment.DialogClickListener {
            override fun onCancelClick() {
                selectSearchNow(true)
            }

            override fun onConfirmClick(datetime: Timestamp) {
                searchRideViewModel.setRideDateTime(datetime)
            }

        })
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
        dialogFragment.isCancelable = false
    }

    private fun performOriginAutocompleteRequest(query: String?, currentLocation: LatLng?) {
        // Perform autocomplete predictions
        val autocompleteToken = AutocompleteSessionToken.newInstance()

        if (currentLocation == null) return

        if (query != null) {
            val request = buildOriginAutocompleteRequestWithLocation(autocompleteToken, query, currentLocation)

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
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
                        SearchRideAdapter(context, locationList) { selectedLocation ->
                            // Determine if the user is focusing on origin or destination
                            if (isOriginFocused) {
                                searchRideViewModel.setOrigin(selectedLocation)
                            } else if (isDestinationFocused) {
                                searchRideViewModel.setDestination(selectedLocation)
                            }

                            focusDestinationEditText(true)
                        }
                    searchResultRecyclerView.layoutManager = LinearLayoutManager(context)
                    searchResultRecyclerView.adapter = searchResultAdapter
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    Toast.makeText(context, exception.message.toString(), Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun buildOriginAutocompleteRequestWithLocation(
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
                    SearchRideAdapter(context, locationList) { selectedLocation ->
                        // Determine if the user is focusing on origin or destination
                        if (isOriginFocused) {
                            searchRideViewModel.setOrigin(selectedLocation)
                        } else if (isDestinationFocused) {
                            searchRideViewModel.setDestination(selectedLocation)
                        } else {
                        }

                        //performOriginAutocompleteRequest(currentLocation = currentLocation)

                        findNavController().navigate(R.id.action_searchRideFragment_to_searchSelectOriginFragment)
                    }
                searchResultRecyclerView.layoutManager = LinearLayoutManager(context)
                searchResultRecyclerView.adapter = searchResultAdapter
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show()
                Log.e("SearchRideFragment: getLatLngFromPlaceId()", exception.toString())
                callback.invoke(null)
            }
    }

    // Function to calculate distance in meters between two locations

}
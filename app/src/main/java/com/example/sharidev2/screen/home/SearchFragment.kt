package com.example.sharidev2.screen.home

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.adapter.SearchRideAdapter
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.databinding.FragmentSearchBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient


class SearchFragment : Fragment() {
    // Global Variables Init
    private lateinit var binding: FragmentSearchBinding



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
        lateinit var searchResultAdapter: SearchRideAdapter
        val searchResultRecyclerView = binding.recyclerSearchPlaceResult
        val placesClient: PlacesClient = Places.createClient(requireContext())

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


                    val bounds = RectangularBounds.newInstance(
                        LatLng(1.093836, 100.934026),  // Southwest corner of Malaysia
                        LatLng(7.358882, 119.308789)  // Northeast corner of Malaysia
                    )

                    // Perform autocomplete predictions
                    val autocompleteRequest = AutocompleteSessionToken.newInstance()

                    val request = FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(autocompleteRequest)
                        .setQuery(query)
                        .setCountries("MY")
                        .setTypeFilter(TypeFilter.ESTABLISHMENT) // Specify the type of places to search for
                        .build()

                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            // Handle the response and update your UI with the predictions
                            val predictions: List<AutocompletePrediction> = response.autocompletePredictions

                            val locationList = predictions.map { prediction ->
                                SearchLocation(
                                    name = prediction.getPrimaryText(null).toString(),
                                    distance = "", // You need to calculate the distance based on your requirements
                                    detailAddress = prediction.getFullText(null).toString()
                                )
                            }

                            // Pass list of data to adapter
                            searchResultAdapter = SearchRideAdapter(locationList)
                            searchResultRecyclerView.layoutManager = LinearLayoutManager(context)
                            searchResultRecyclerView.adapter = searchResultAdapter
                        }
                        .addOnFailureListener { exception ->
                            // Handle errors
                            Toast.makeText(requireContext(), exception.toString(), Toast.LENGTH_SHORT).show()
                            Log.e("EXCEPTION BABIIIIII", exception.toString())
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


}
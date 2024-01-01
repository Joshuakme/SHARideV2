package com.example.sharidev2.screen.home

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentSearchBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class SearchFragment : Fragment() {
    // Variables Init
    private lateinit var binding: FragmentSearchBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)


        // ELEMENT VARIABLES
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val backBtn = binding.imgBtnSearchBack
        val pickUpLocationEditText = binding.editTextOfferRidePickUpLocation
        val pickUpLocationEditTextCard = binding.cardOfferRidePickUpLocation
        val destinationLocationEditText = binding.editTextOfferRideDestinationLocation
        val destinationLocationEditTextCard = binding.cardOfferRideDestinationLocation


        // LAYOUT SETTINGS
        bottomNav?.visibility = View.GONE



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


        // NAVIGATION EVENT LISTENERS
        // Search Fragment -> Home Fragment
        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_homeFragment)
        }

        return binding.root
    }



}
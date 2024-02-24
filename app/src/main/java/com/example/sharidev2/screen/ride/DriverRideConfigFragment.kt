package com.example.sharidev2.screen.ride

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentDriverRideConfigBinding
import com.example.sharidev2.viewmodel.SharedSearchRideViewModel


class DriverRideConfigFragment : Fragment() {
    private lateinit var binding: FragmentDriverRideConfigBinding
    private val searchRideViewModel: SharedSearchRideViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_ride_config, container, false)


        // ELEMENT VARIABLES
        val originText = binding.textDriverRideConfigOrigin
        val destinationText = binding.textDriverRideConfigDestination


        searchRideViewModel.origin.observe(viewLifecycleOwner) {origin ->
            originText.text = origin.name
        }

        searchRideViewModel.destination.observe(viewLifecycleOwner) {destination ->
            destinationText.text = destination.name
        }


        // EVENT LISTENERS



        return binding.root
    }

}
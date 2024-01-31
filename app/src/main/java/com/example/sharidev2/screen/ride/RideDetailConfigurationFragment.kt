package com.example.sharidev2.screen.ride

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentRideDetailConfigurationBinding
import com.example.sharidev2.screen.user.CountryCodeBottomDialogFragment
import com.example.sharidev2.viewmodel.SearchRideViewModel

class RideDetailConfigurationFragment : Fragment() {
    private lateinit var binding: FragmentRideDetailConfigurationBinding
    private val searchRideViewModel: SearchRideViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ride_detail_configuration, container, false)


        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnRideDetailConfigBack
        val originRideDetailText = binding.textRideDetailConfigOrigin
        val destinationRideDetailText = binding.textRideDetailConfigDestination
        val driverGenderSpinner = binding.spinnerRideDetailConfigDriverGender






        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)
        originRideDetailText.text = searchRideViewModel.origin.value?.name
        destinationRideDetailText.text = searchRideViewModel.destination.value?.name


        // EVENT LISTENERS
        driverGenderSpinner.setOnClickListener {
            showDriverGenderDialog()
        }

//        driverGenderSpinner.setOnClickListener {
//            showDriverGenderDialog()
//        }


        // NAVIGATION EVENT LISTENERS
        // Ride Detail Configuration Fragment -> Search Select Origin Fragment
        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_rideDetailConfigurationFragment_to_searchSelectOriginFragment)
        }


        return binding.root
    }


    // Method to show the CountryCodeBottomDialogFragment
    private fun showCountryCodeDialog() {
        val dialogFragment = CountryCodeBottomDialogFragment()
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
    }

    private fun showDriverGenderDialog() {
        val dialogFragment = GenderBottomDialogFragment()
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
    }
}
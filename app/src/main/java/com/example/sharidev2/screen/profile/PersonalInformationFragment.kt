package com.example.sharidev2.screen.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentPersonalInformationBinding
import com.example.sharidev2.viewmodel.PersonalInfoViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class PersonalInformationFragment : Fragment() {
    // Global Variables Init
    private lateinit var binding: FragmentPersonalInformationBinding
    private lateinit var viewModel: PersonalInfoViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_personal_information, container, false)

        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnProfilePersonalInfoNavBack
        val displayName = binding.cardPersonalInfoDisplayName
        val driverLicense = binding.cardPersonalInfoDrivingLicense

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(PersonalInfoViewModel::class.java)

        // Fetch display name from Firestore
        viewModel.fetchDisplayNameFromDatabase()

        // Observe the display name
        viewModel.displayName.observe(viewLifecycleOwner) { displayName ->
            binding.textPersonalInfoItemValueDisplayName.text = displayName
        }


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)


        // NAVIGATION EVENT LISTENERS
        // Personal Information Fragment -> Profile Fragment
        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_personalInformationFragment_to_profileFragment)
        }

        displayName.setOnClickListener {
            findNavController().navigate(R.id.action_personalInformationFragment_to_editDisplayNameFragment)
        }

        driverLicense.setOnClickListener{
            findNavController().navigate(R.id.action_personalInformationFragment_to_drivingLicenseFragment)
        }


        return binding.root
    }

}
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
import com.google.firebase.auth.FirebaseAuth


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
        val gender = binding.cardPersonalInfoGender
        val mobileNumber = binding.cardPersonalInfoMobileNumber
        val driverLicense = binding.cardPersonalInfoDrivingLicense

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[PersonalInfoViewModel::class.java]



        // Fetch display name, gender and mobile from Firestore
        viewModel.fetchDisplayNameFromDatabase()
        viewModel.fetchMobileFromDatabase()
        viewModel.fetchGenderFromDatabase()



        // Observe the display name, gender and mobile
        viewModel.displayName.observe(viewLifecycleOwner) { displayName ->
            binding.textPersonalInfoItemValueDisplayName.text = displayName
        }

        viewModel.mobile.observe(viewLifecycleOwner) { mobile ->
            binding.textPersonalInfoItemValueMobileNumber.text = "+60 " + mobile
        }

        viewModel.gender.observe(viewLifecycleOwner) { gender ->
            binding.textPersonalInfoItemValueGender.text = gender
        }

        binding.textPersonalInfoItemValueUserId.text = FirebaseAuth.getInstance().currentUser?.uid



        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)



        // NAVIGATION EVENT LISTENERS
        // Personal Information Fragment -> Profile Fragment
        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_personalInformationFragment_to_profileFragment)
        }

        // Personal Information Fragment -> Edit Display Name Fragment
        displayName.setOnClickListener {
            findNavController().navigate(R.id.action_personalInformationFragment_to_editDisplayNameFragment)
        }

        // Personal Information Fragment -> Edit Gender Fragment
        gender.setOnClickListener {
            findNavController().navigate(R.id.action_personalInformationFragment_to_editGenderFragment)
        }

        // Personal Information Fragment -> Edit Mobile Fragment
        mobileNumber.setOnClickListener {
            findNavController().navigate(R.id.action_personalInformationFragment_to_editMobileFragment3)
        }

        //TODO：
        // Personal Information Fragment -> Vehicle Documentation Fragment
        driverLicense.setOnClickListener{
            //findNavController().navigate(R.id.)
        }

        // Personal Information Fragment -> Driving License Fragment
        driverLicense.setOnClickListener{
            findNavController().navigate(R.id.action_personalInformationFragment_to_drivingLicenseFragment)
        }

        return binding.root
    }

}
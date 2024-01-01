package com.example.sharidev2.screen.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class ProfileFragment : Fragment() {
    // Variables Init
    private lateinit var binding : FragmentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        // ELEMENT VARIABLES
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val personalInfoBtn = binding.cardPersonalInfo
        val paymentMethodBtn = binding.cardPaymentMethod
        val addressesdBtn = binding.cardAddresses
        val emergencyContactBtn = binding.cardEmergencyContact


        // LAYOUT SETTINGS
        bottomNav?.visibility = View.VISIBLE


        // NAVIGATION EVENT LISTENERS
        // Profile Fragment -> Personal Information Fragment
        personalInfoBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_personalInformationFragment)
        }

        // Profile Fragment -> Payment Method Fragment
        paymentMethodBtn.setOnClickListener {
            // TODO: Set up nav graph (payment method)
            // findNavController().navigate(R.id.action_profileFragment_to_personalInformationFragment)
        }

        // Profile Fragment -> Addresses Fragment
        addressesdBtn.setOnClickListener {
            // TODO: Set up nav graph (addresses)
            // findNavController().navigate(R.id.action_profileFragment_to_personalInformationFragment)
        }

        // Profile Fragment -> Emergency Contact Fragment
        emergencyContactBtn.setOnClickListener {
            // TODO: Set up nav graph (emergency contact)
            // findNavController().navigate(R.id.action_profileFragment_to_personalInformationFragment)
        }




        return binding.root
    }


}
package com.example.sharidev2.screen.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentProfileBinding
import com.example.sharidev2.utility.FirebaseClient


class ProfileFragment : Fragment() {
    // Variables Init
    private lateinit var binding : FragmentProfileBinding
    private val auth = FirebaseClient.firebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        // ELEMENT VARIABLES
        val profileNameText = binding.textProfileDisplayName
        val profileUserIdText = binding.textProfileUserId
        val personalInfoBtn = binding.cardPersonalInfo
        val paymentMethodBtn = binding.cardPaymentMethod
        val addressesBtn = binding.cardAddresses
        val emergencyContactBtn = binding.cardEmergencyContact
        val logoutBtn = binding.cardProfileLogoutBtn

        // AUTH VARIABLES
        val user = auth.currentUser

        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(true)
        (activity as MainActivity).resetBottomNavPosition()
        profileNameText.text = user?.displayName ?: getString(R.string.profile_log_in)
        profileUserIdText.visibility = if(user == null) View.GONE else View.VISIBLE
        profileUserIdText.text = "@${user?.displayName}"


        // Check if user is logged in and navigate to respective screen



        // NAVIGATION EVENT LISTENERS
        if(auth.currentUser == null) {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        // Profile Fragment -> Login Fragment
        profileNameText.setOnClickListener {
            if(auth.currentUser != null) {}
            else {
                findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            }
        }

        // Profile Fragment -> Personal Information Fragment
        personalInfoBtn.setOnClickListener {
            //findNavController().navigate(R.id.action_profileFragment_to_personalInformationFragment)
            findNavController().navigate(R.id.action_profileFragment_to_personalInformationFragment)
        }

        // Profile Fragment -> Payment Method Fragment
        paymentMethodBtn.setOnClickListener {
             findNavController().navigate(R.id.action_profileFragment_to_paymentMethodFragment)
        }

        // Profile Fragment -> Addresses Fragment
        addressesBtn.setOnClickListener {
            // TODO: Set up nav graph (addresses)
            // findNavController().navigate(R.id.action_profileFragment_to_personalInformationFragment)
        }

        // Profile Fragment -> Emergency Contact Fragment
        emergencyContactBtn.setOnClickListener {
            // TODO: Set up nav graph (emergency contact)
            // findNavController().navigate(R.id.action_profileFragment_to_personalInformationFragment)
        }


        // Log out
        logoutBtn.setOnClickListener {
            // TODO: Dialog to confirm user to logout

            auth.signOut()

            Toast.makeText(requireContext(), "Logged out!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }

        return binding.root
    }

}
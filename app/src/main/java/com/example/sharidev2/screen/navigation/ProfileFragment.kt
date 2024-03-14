package com.example.sharidev2.screen.navigation

import ProfileViewModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentProfileBinding
import com.example.sharidev2.utility.FirebaseClient

class ProfileFragment : Fragment() {
    // Variables Init
    private lateinit var binding: FragmentProfileBinding
    private val auth = FirebaseClient.firebaseAuth

    // Initialize ViewModel
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        // ELEMENT VARIABLES
        val profileNameText = binding.textProfileDisplayName
        val profileUserIdText = binding.textProfileUserId
        val profilePic = binding.imageProfile
        val personalInfoBtn = binding.cardPersonalInfo
        val paymentMethodBtn = binding.cardPaymentMethod
        val addressesBtn = binding.cardAddresses
        val emergencyContactBtn = binding.cardEmergencyContact
        val logoutBtn = binding.cardProfileLogoutBtn

        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(true)
        (activity as MainActivity).resetBottomNavPosition()

        // Observe ViewModel data
        profileViewModel.displayName.observe(viewLifecycleOwner, Observer { displayName ->
            profileNameText.text = displayName ?: getString(R.string.profile_log_in)
        })

        profileViewModel.userId.observe(viewLifecycleOwner, Observer { userId ->
            profileUserIdText.text = "@$userId"
            profileUserIdText.visibility = if (userId == null) View.GONE else View.VISIBLE
        })

        profileViewModel.profilePicUrl.observe(viewLifecycleOwner, Observer { profilePicUrl ->
            profilePicUrl?.let {
                Glide.with(requireContext())
                    .load(profilePicUrl)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                    .into(profilePic)
            }
        })

        // NAVIGATION EVENT LISTENERS
        if (auth.currentUser == null) {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        // Profile Fragment -> Login Fragment
        profileNameText.setOnClickListener {
            if (auth.currentUser != null) {
                // Do something
            } else {
                findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            }
        }

        // Profile Fragment -> Personal Information Fragment
        personalInfoBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_personalInformationFragment)
        }

        // Profile Fragment -> Payment Method Fragment
        paymentMethodBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_paymentMethodFragment)
        }

        // Profile Fragment -> Addresses Fragment
        addressesBtn.setOnClickListener {
            // TODO: Set up nav graph (addresses)
        }

        // Profile Fragment -> Emergency Contact Fragment
        emergencyContactBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_emergencyContactFragment)
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

package com.example.sharidev2.screen.navigation

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentHomeBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.viewmodel.CurrentLocationViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale


class HomeFragment : Fragment() {
    // Variables Init
    private lateinit var binding: FragmentHomeBinding
    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModels()

    private val auth = FirebaseAuth.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)



        // ELEMENT VARIABLES
        val homeNestedScrollView = binding.nsvFragmentHome
        val welcomeHomeText = binding.textHomeWelcomeUser
        val searchBarBtn = binding.cardHomeSearchBar
        val shareThisAppLinkText = binding.textHomeShareThisAppLink
        val shareThisAppCopyBtn = binding.btnHomeShareThisAppCopy
        val shareThisAppShareBtn = binding.btnHomeShareThisAppShare

        // AUTH VARIABLES
        val user = auth.currentUser


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(true)
        (activity as MainActivity).resetBottomNavPosition()
        welcomeHomeText.text = getString(R.string.home_fragment_welcome_user, user?.displayName ?: "back")


        currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) {currentLocation ->
            if(currentLocation != null) {
                fetchAreaFromLocation(Location(LocationManager.GPS_PROVIDER).apply {
                    latitude = currentLocation.latitude
                    longitude = currentLocation.longitude
                })
            }
        }


        homeNestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            val bottomNavContainer = requireActivity().findViewById<LinearLayout>(R.id.ll_bottom_navigation)
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)

            val constraintLayout = (activity as MainActivity).findViewById<ConstraintLayout>(R.id.constraint_main_activity)
            val activityFragmentContainer = (activity as MainActivity).findViewById<FragmentContainerView>(R.id.fragment_container_main)
            val constraintSet = ConstraintSet()

            constraintSet.clone(constraintLayout)

            if(scrollY > 0) {   // Scroll down
                constraintSet.connect(activityFragmentContainer.id, ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM)
            } else {
                constraintSet.connect(activityFragmentContainer.id, ConstraintSet.BOTTOM, bottomNavContainer.id, ConstraintSet.TOP)
            }
            constraintSet.applyTo(constraintLayout)

            // Calculate the scroll change
            val dy = oldScrollY - scrollY

            // Translate the bottom navigation
            bottomNavContainer.translationY = ((bottomNavContainer.translationY + -dy)
                .coerceAtLeast(0f))     // if translation less than 0, then 0
                .coerceAtMost(bottomNav.height.toFloat())   // if translation more than height of bottomNav, then height of bottomNav
        }




        // NAVIGATION EVENT LISTENERS
        // Home Fragment -> Search Fragment
        searchBarBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

        shareThisAppCopyBtn.setOnClickListener {
            CommonUtils().copyLinkToClipboard(requireContext(), getString(R.string.share_app_link))
        }

        shareThisAppShareBtn.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_link))
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        return binding.root
    }


    private fun fetchAreaFromLocation(location: Location) {
        val areaText = binding.textHomeWelcomeUserArea

        lifecycleScope.launch(Dispatchers.IO) {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            var addressText = ""

            try {
                val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                addresses?.let {
                    val address = it[0]
                    addressText = address.locality ?: "Unknown Location"
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            // Update UI on the main thread
            launch(Dispatchers.Main) {
                areaText.text = addressText
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val homeNestedScrollView = binding.nsvFragmentHome
        homeNestedScrollView.scrollY = 0
    }
}
package com.example.sharidev2.screen.navigation

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
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController

import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.adapter.RidePagerAdapter
import com.example.sharidev2.databinding.FragmentHomeBinding
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
//        val tabLayout: TabLayout = binding.tabHomeMainMenu
//        val viewPager: ViewPager2 = binding.viewPagerHomeMainMenu
        val homeNestedScrollView = binding.nsvFragmentHome
        val welcomeHomeText = binding.textHomeWelcomeUser
        val searchBarBtn = binding.cardHomeSearchBar

        // AUTH VARIABLES
        val user = auth.currentUser


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(true)
        welcomeHomeText.text = getString(R.string.home_fragment_welcome_user, user?.displayName ?: "back")


        currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) {currentLocation ->
            fetchAreaFromLocation(Location(LocationManager.GPS_PROVIDER).apply {
                latitude = currentLocation.latitude
                longitude = currentLocation.longitude
            })
        }


        homeNestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            val bottomNavContainer = requireActivity().findViewById<LinearLayout>(R.id.ll_bottom_navigation)
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)

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



        // Set up adapter
        val pagerAdapter = RidePagerAdapter(this)
//        viewPager.adapter = pagerAdapter
//
//        // Set up mediator
//        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
//            when (position) {
//                0 -> {
//                    tab.text = "Find Ride"
//                    tab.customView = null  // Reset custom view
//                    tab.view?.minimumWidth = 0  // Reset minimum width
//                    tab.view?.layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT
//                    )
//                }
//                1 -> {
//                    tab.text = "Offer Ride"
//                    tab.customView = null  // Reset custom view
//                    tab.view?.minimumWidth = 0  // Reset minimum width
//                    tab.view?.layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT
//                    )
//                }
//            }
//        }.attach()
//
//
//        // Select the default tab (e.g., the first tab)
//        tabLayout.getTabAt(0)?.select()



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
}
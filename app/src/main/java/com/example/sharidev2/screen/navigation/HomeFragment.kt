package com.example.sharidev2.screen.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.adapter.RidePagerAdapter
import com.example.sharidev2.databinding.FragmentHomeBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth


class HomeFragment : Fragment(), OnMapReadyCallback {
    // Variables Init
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mGoogleMap: GoogleMap
    private val auth = FirebaseAuth.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_home_container) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // ELEMENT VARIABLES
//        val tabLayout: TabLayout = binding.tabHomeMainMenu
//        val viewPager: ViewPager2 = binding.viewPagerHomeMainMenu
        val welcomeHomeText = binding.textHomeWelcomeUser
        val searchBarBtn = binding.cardHomeSearchBar

        // AUTH VARIABLES
        val user = auth.currentUser


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(true)
        welcomeHomeText.text = getString(R.string.home_fragment_welcome_user, user?.displayName ?: "back")


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

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

    }

}
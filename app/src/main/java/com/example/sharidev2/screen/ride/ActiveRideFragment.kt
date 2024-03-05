package com.example.sharidev2.screen.ride

import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.sharidev2.R
import com.example.sharidev2.data.repository.UserLocationRepository
import com.example.sharidev2.databinding.FragmentActiveRideBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.FirebaseClient.convertFirebaseImageToBitmap
import com.example.sharidev2.utility.GoogleMapUtils
import com.example.sharidev2.viewmodel.ActiveRideViewModel
import com.example.sharidev2.viewmodel.CurrentLocationViewModel
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ActiveRideFragment : Fragment() {
    private lateinit var binding: FragmentActiveRideBinding
    private val activeRideViewModel: ActiveRideViewModel by viewModels()
    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModels()

    private lateinit var googleMapFragment: SupportMapFragment
    private val mHandler: Handler = Handler()
    private lateinit var mRunnable: Runnable
    private val LOCATION_UPDATE_INTERVAL = 3000 as Long

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_active_ride, container, false)


        // Args
        val activeRideId = arguments?.getString("rideId")

        lifecycleScope.launch(Dispatchers.Main) {
            if(activeRideId != null) {
                val activeRide = FirebaseClient.getRideFromRideId(activeRideId)

                if (activeRide != null) {
                    activeRideViewModel.setActiveRide(activeRide)
                }
            }
        }


        // ELEMENT VARIABLES
        googleMapFragment = childFragmentManager.findFragmentById(R.id.map_active_ride_container) as SupportMapFragment



        setupMap()

        return binding.root
    }


    private fun startUserLocationsRunnable() {
        Log.d(
            TAG,
            "startUserLocationsRunnable: starting runnable for retrieving updated locations."
        )
        mHandler.postDelayed(Runnable {
            getUserLocation()
            mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL)
        }.also { mRunnable = it }, LOCATION_UPDATE_INTERVAL)
    }

    private fun stopLocationUpdates() {
        mHandler.removeCallbacks(mRunnable)
    }

    private fun setupMap() {

        currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) { currentLocation ->
            if (currentLocation != null) {
                val myLocationBtn = binding.cardActiveRideMyLocationContainer

                googleMapFragment.getMapAsync { googleMap ->
                    GoogleMapUtils().setupMapListeners(googleMap, currentLocation, myLocationBtn,
                        object : GoogleMapUtils.MyLocationButtonCallback {
                            override fun showMyLocationButton(show: Boolean) {
                                showMyLocationBtn(show)
                            }
                        }
                    )
                }
            }
        }
    }

    private fun getUserLocation() {
        googleMapFragment.getMapAsync { googleMap ->
            activeRideViewModel.activeRideUserLocationList.observe(viewLifecycleOwner) { locationList ->
                if (locationList != null) {
                    for (location in locationList) {
                        if (location.location != null) {
                            googleMap.clear()


                            val currentUserColor = CommonUtils().getThemeColor(
                                requireContext(),
                                com.google.android.material.R.attr.colorPrimary
                            )
                            val otherUserColor = CommonUtils().getThemeColor(
                                requireContext(),
                                com.google.android.material.R.attr.colorSecondary
                            )

                            val firebaseStorage = FirebaseClient.firebaseStorage
                            val storageRef =
                                firebaseStorage.reference.child(location.user?.photoUrl.toString())


                            convertFirebaseImageToBitmap(storageRef,
                                onSuccess = { bitmap ->
                                    val userIcon =
                                        if (location.user?.uid == FirebaseClient.firebaseAuth.uid) {
                                            CommonUtils().createMarkerWithCircularImage(
                                                requireContext(),
                                                bitmap,
                                                currentUserColor
                                            )
                                        } else {
                                            CommonUtils().createMarkerWithCircularImage(
                                                requireContext(),
                                                bitmap,
                                                otherUserColor
                                            )
                                        }

//                                    val userIcon = if(activeRideViewModel.activeRideCurrentUserRole.value != "driver") {
//
//                                    } else {
//                                        CommonUtils().createMarkerWithCircularImage(requireContext(), bitmap, otherUserColor)
//                                    }

                                    GoogleMapUtils().addMarker(
                                        googleMap,
                                        location.location,
                                        userIcon
                                    )
                                },
                                onFailure = { exception ->
                                    // Error occurred, handle it
                                }
                            )

                        }
                    }
                }
            }
        }
    }


    private fun showMyLocationBtn(show: Boolean) {
        val myLocationBtn = binding.cardActiveRideMyLocationContainer

        myLocationBtn.visibility = if (show) View.VISIBLE else View.GONE
    }


    override fun onResume() {
        super.onResume()

        startUserLocationsRunnable()
    }
}
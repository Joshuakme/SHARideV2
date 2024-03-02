package com.example.sharidev2.screen.ride

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.databinding.FragmentRideDetailBinding
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.viewmodel.RideViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RideDetailFragment : Fragment() {
    private lateinit var binding: FragmentRideDetailBinding
    private val rideViewModel: RideViewModel by viewModels()
    private lateinit var navController: NavController


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        // Navigate to the BookingFragment
        navController.navigate(R.id.action_rideDetailFragment_to_bookingFragment)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ride_detail, container, false)


        // DATA
        val rideId = arguments?.getString("rideId")
        Log.e("", "RideId: $rideId")
        val currentUser = FirebaseClient.firebaseAuth.currentUser

        // ELEMENT VARIABLES
        val requestBtn = binding.btnRideDetailRequestRide
        val navController = Navigation.findNavController(requireActivity(), R.id.fragment_container_main)


        requestBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {

                if((rideId != null) && (currentUser != null)) {
                    val passenger = Passenger(
                        userUid = currentUser.uid,
                        ridePrice = 0.0
                    )

                    val responseStatus = rideViewModel.addPassengerToRide(passenger, rideId)

                    when(responseStatus) {
                        Constants.FIREBASE_REQUEST_SUCCESS -> {
                            Toast.makeText(requireContext(), "Ride requested successfully!", Toast.LENGTH_SHORT).show()

                            navController.navigate(R.id.action_rideDetailFragment_to_bookingFragment)
                        }

                        Constants.FIREBASE_REQUEST_EXCEPTION -> {
                            Toast.makeText(requireContext(), "Ride requested failed!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }


        return binding.root
    }

}
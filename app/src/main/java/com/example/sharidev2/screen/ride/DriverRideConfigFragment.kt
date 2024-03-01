package com.example.sharidev2.screen.ride

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentDriverRideConfigBinding
import com.example.sharidev2.viewmodel.SharedCreateRideViewModel
import com.example.sharidev2.viewmodel.SharedSearchRideViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale


class DriverRideConfigFragment : Fragment() {
    private lateinit var binding: FragmentDriverRideConfigBinding
    private val createRideViewModel: SharedCreateRideViewModel by activityViewModels()

    private val dateFormatter = SimpleDateFormat("yyyy MMM dd", Locale.ENGLISH)
    private val timeFormatter = SimpleDateFormat("hh : mm a", Locale.ENGLISH)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_ride_config, container, false)


        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnDriverRideConfigNavBack



        setupViewModelObserver()

        setupOnClickListeners()

        // EVENT LISTENERS
        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }


        return binding.root
    }


    private fun setupViewModelObserver() {
        val originText = binding.textDriverRideConfigOrigin
        val destinationText = binding.textDriverRideConfigDestination
        val vehicleSpinnerText = binding.textDriverRideConfigSpinnerVehicle
        val passengerCapacitySpinnerText = binding.textDriverRideConfigSpinnerRideCapacity
        val rideDateSpinnerText = binding.textDriverRideConfigSpinnerScheduleDate
        val rideTimeSpinnerText = binding.textDriverRideConfigSpinnerScheduleTime





        createRideViewModel.origin.observe(viewLifecycleOwner) {origin ->
            originText.text = origin.name
        }

        createRideViewModel.destination.observe(viewLifecycleOwner) {destination ->
            destinationText.text = destination.name
        }

        createRideViewModel.vehicle.observe(viewLifecycleOwner) {vehicle ->
            val passengerCapacitySpinner = binding.spinnerDriverRideConfigRideCapacity

            if(vehicle != null) {
                vehicleSpinnerText.text = vehicle.plateNumber

                passengerCapacitySpinner.isEnabled = true
                passengerCapacitySpinner.isClickable = true
            } else {
                passengerCapacitySpinner.isEnabled = false
                passengerCapacitySpinner.isClickable = false
            }
        }

        createRideViewModel.capacity.observe(viewLifecycleOwner) {capacity ->
            passengerCapacitySpinnerText.text = getString(R.string.driver_ride_config_fragment_passenger_capacity_value, capacity)
        }

        createRideViewModel.rideDateTime.observe(viewLifecycleOwner) {rideDate ->
            rideDateSpinnerText.text =  dateFormatter.format(rideDate.toDate())
            rideTimeSpinnerText.text = timeFormatter.format(rideDate.toDate())
        }



    }

    private fun setupOnClickListeners() {
        val vehicleSpinner = binding.spinnerDriverRideConfigVehicle
        val passengerCapacitySpinner = binding.spinnerDriverRideConfigRideCapacity
        val createRideBtn = binding.btnDriverRideConfigCtaCreateRide
        val createRideBtnCtaText = binding.textDriverRideConfigCtaCreateRide
        val createRideBtnLoadingProgressBar = binding.progressBarDriverRideCtaCreateRide

        vehicleSpinner.setOnClickListener {
            showVehicleDialog()
        }

        passengerCapacitySpinner.setOnClickListener {
            showPassengerCapacityDialog()
        }

        createRideBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                createRideViewModel.createRide()
            }

            createRideViewModel.createRideStatus.observe(viewLifecycleOwner) {response ->
                // RIDE STATUS
                val CREATE_RIDE_PENDING = 0
                val CREATE_RIDE_SUCCESS = 1
                val CREATE_RIDE_FAILED = -1

                when(response) {
                    CREATE_RIDE_PENDING -> {
                        createRideBtnCtaText.visibility = View.INVISIBLE
                        createRideBtnLoadingProgressBar.visibility = View.VISIBLE
                    }
                    CREATE_RIDE_SUCCESS -> {
                        createRideBtnCtaText.visibility = View.VISIBLE
                        createRideBtnLoadingProgressBar.visibility = View.GONE

                        createRideViewModel.resetData()

                        findNavController().navigate(R.id.action_driverRideConfigFragment_to_bookingFragment)
                        Toast.makeText(requireContext(), "Ride created successfully!", Toast.LENGTH_SHORT).show()
                    }
                    CREATE_RIDE_FAILED -> {
                        createRideBtnCtaText.visibility = View.INVISIBLE
                        createRideBtnLoadingProgressBar.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), "Ride created failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    // BOTTOM DIALOG
    private fun showVehicleDialog() {
        val dialogFragment = VehicleBottomDialogFragment()
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
    }

    private fun showPassengerCapacityDialog() {
        val dialogFragment = PassengerCapacityBottomDialogFragment()
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
    }
}
package com.example.sharidev2.screen.ride

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.viewmodel.CurrentLocationViewModel
import com.example.sharidev2.viewmodel.SharedCreateRideViewModel
import com.example.sharidev2.viewmodel.SharedSearchRideViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale


class DriverRideConfigFragment : Fragment() {
    private lateinit var binding: FragmentDriverRideConfigBinding
    private val createRideViewModel: SharedCreateRideViewModel by activityViewModels()
    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModels()

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
        val passengerCapacitySpinner = binding.spinnerDriverRideConfigRideCapacity
        val passengerCapacitySpinnerText = binding.textDriverRideConfigSpinnerRideCapacity
        val rideDateSpinnerText = binding.textDriverRideConfigSpinnerScheduleDate
        val rideTimeSpinnerText = binding.textDriverRideConfigSpinnerScheduleTime



        createRideViewModel.origin.observe(viewLifecycleOwner) {origin ->
            originText.text = origin.name
        }

        createRideViewModel.destination.observe(viewLifecycleOwner) {destination ->
            destinationText.text = destination.name
        }


        if(!createRideViewModel.vehicle.isInitialized) {
            vehicleSpinnerText.text = " - "
            passengerCapacitySpinnerText.text = " - "

            passengerCapacitySpinner.isEnabled = false
            passengerCapacitySpinner.isClickable = false

            // Set disabled color
            disableCapacitySpinner(true)
        }

        createRideViewModel.vehicle.observe(viewLifecycleOwner) {vehicle ->
            vehicleSpinnerText.text = vehicle.plateNumber

            passengerCapacitySpinner.isEnabled = true
            passengerCapacitySpinner.isClickable = true

            disableCapacitySpinner(false)
        }

        createRideViewModel.capacity.observe(viewLifecycleOwner) {capacity ->
            passengerCapacitySpinnerText.text = getString(R.string.driver_ride_config_fragment_passenger_capacity_value, capacity)
        }

        createRideViewModel.rideDateTime.observe(viewLifecycleOwner) {rideDate ->
            rideDateSpinnerText.text = CommonUtils.formatDate(rideDate)
            rideTimeSpinnerText.text = CommonUtils.formatTime(rideDate)
//            rideDateSpinnerText.text =  dateFormatter.format(rideDate.toDate())
//            rideTimeSpinnerText.text = timeFormatter.format(rideDate.toDate())
        }



    }

    private fun setupOnClickListeners() {
        val vehicleSpinner = binding.spinnerDriverRideConfigVehicle
        val passengerCapacitySpinner = binding.spinnerDriverRideConfigRideCapacity
        val rideDateSpinner = binding.spinnerDriverRideConfigScheduleDate
        val rideTimeSpinner = binding.spinnerDriverRideConfigScheduleTime
        val createRideBtn = binding.btnDriverRideConfigCtaCreateRide
        val createRideBtnCtaText = binding.textDriverRideConfigCtaCreateRide
        val createRideBtnLoadingProgressBar = binding.progressBarDriverRideCtaCreateRide

        vehicleSpinner.setOnClickListener {
            showVehicleDialog()
        }

        passengerCapacitySpinner.setOnClickListener {
            showPassengerCapacityDialog()
        }

        rideDateSpinner.setOnClickListener {
            showTimingDialog()
        }

        rideTimeSpinner.setOnClickListener {
            showTimingDialog()
        }


        createRideBtn.setOnClickListener {
            currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) {currentLocation ->
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    createRideViewModel.createRide(currentLocation)
                }
            }

            createRideViewModel.createRideStatus.observe(viewLifecycleOwner) {response ->

                when(response) {
                    Constants.UI_DATA_LOADING -> {
                        createRideBtnCtaText.visibility = View.INVISIBLE
                        createRideBtnLoadingProgressBar.visibility = View.VISIBLE
                    }
                    Constants.UI_DATA_SUCCESS -> {
                        createRideBtnCtaText.visibility = View.VISIBLE
                        createRideBtnLoadingProgressBar.visibility = View.GONE

                        createRideViewModel.resetData()

                        findNavController().popBackStack(R.id.homeFragment, false)
                        Toast.makeText(requireContext(), "Ride created successfully!", Toast.LENGTH_SHORT).show()
                    }
                    Constants.UI_DATA_FAILED -> {
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

    private fun showTimingDialog() {
        val dialogFragment = TimingBottomDialogFragment(object: TimingBottomDialogFragment.DialogClickListener {
            override fun onCancelClick() {
                // Do nothing
            }

            override fun onConfirmClick(datetime: Timestamp) {
                createRideViewModel.setRideDateTime(datetime)
            }

        })
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
        dialogFragment.isCancelable = false
    }

    private fun disableCapacitySpinner(disable: Boolean) {
        val passengerCapacitySpinnerText = binding.textDriverRideConfigSpinnerRideCapacity
        val chooseCapacityImageButton = binding.imgBtnDriverRideConfigSpinnerChooseRideCapacity

        if(disable) {
            passengerCapacitySpinnerText.setTextColor(CommonUtils().getAndroidThemeColor(requireContext(), android.R.attr.textColorHint))
            chooseCapacityImageButton.setColorFilter(
                CommonUtils().getAndroidThemeColor(requireContext(),
                    com.google.android.material.R.attr.colorSurfaceVariant),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            passengerCapacitySpinnerText.setTextColor(CommonUtils().getAndroidThemeColor(requireContext(),
                com.google.android.material.R.attr.colorOnSurface))

            chooseCapacityImageButton.setColorFilter(
                CommonUtils().getAndroidThemeColor(requireContext(),
                    com.google.android.material.R.attr.colorOnSurface),
                PorterDuff.Mode.SRC_IN
            )
        }
    }
}
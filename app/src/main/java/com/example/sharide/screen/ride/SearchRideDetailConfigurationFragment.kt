package com.example.sharide.screen.ride


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.sharide.MainActivity
import com.example.sharide.R
import com.example.sharide.databinding.FragmentSearchRideDetailConfigurationBinding
import com.example.sharide.utility.CommonUtils
import com.example.sharide.viewmodel.SharedSearchRideViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class SearchRideDetailConfigurationFragment : Fragment(){
    private lateinit var binding: FragmentSearchRideDetailConfigurationBinding
    private val searchRideViewModel by activityViewModels<SharedSearchRideViewModel>()

    private lateinit var context: Context
    private val dateFormatter = SimpleDateFormat("yyyy MMM dd", Locale.ENGLISH)
    private val timeFormatter = SimpleDateFormat("hh : mm a", Locale.ENGLISH)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_ride_detail_configuration, container, false)


        context = if(getContext() != null) {
            requireContext()
        } else {
            requireActivity().applicationContext
        }


        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnRideDetailConfigBack
        val findRideButton = binding.btnRideDetailConfigurationCtaFindRide


        // LAYOUT SETTINGS
        val activity = activity as MainActivity
        activity.setStatusBarColor(CommonUtils().getThemeColor(context, android.R.attr.colorBackground))
        activity.setBottomNavVisible(false)

        // VIEW MODEL OBSERVATION
        setupViewModelObservers()

        // EVENT LISTENERS
        setupOnClickListeners()


        // NAVIGATION EVENT LISTENERS
        // Ride Detail Configuration Fragment -> Search Select Origin Fragment
        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_rideDetailConfigurationFragment_to_searchSelectOriginFragment)
        }

        // Ride Detail Configuration Fragment -> Matched Ride Fragment
        findRideButton.setOnClickListener {
            searchRideViewModel.setSearchRide()

            findNavController().navigate(R.id.action_rideDetailConfigurationFragment_to_matchedRideFragment)
        }


        return binding.root
    }



    private fun setupViewModelObservers() {
        val originRideDetailText = binding.textRideDetailConfigOrigin
        val destinationRideDetailText = binding.textRideDetailConfigDestination
        val driverGenderSelectText = binding.textRideDetailConfigSpinnerDriverGender
        val vehicleTypeSelectText = binding.textRideDetailConfigSpinnerVehicleType
        val rideDateSelectText = binding.textRideDetailConfigSpinnerScheduleDate
        val rideTimeSelectText = binding.textRideDetailConfigSpinnerScheduleTime


        // Search Ride Origin Location
        searchRideViewModel.origin.observe(viewLifecycleOwner) { origin ->
            if(origin != null) {
                originRideDetailText.text = origin.name
            }
        }

        // Search Ride Destination Location
        searchRideViewModel.destination.observe(viewLifecycleOwner) { destination ->
            if(destination != null) {
                destinationRideDetailText.text = destination.name
            }
        }

        // Search Ride Driver's Gender
        searchRideViewModel.driverGender.observe(viewLifecycleOwner) { driverGender ->
            driverGenderSelectText.text = driverGender.toString()
        }

        // Search Ride Vehicle Type
        searchRideViewModel.vehicleType.observe(viewLifecycleOwner) { vehicleType ->
            vehicleTypeSelectText.text = vehicleType.toString()
        }

        // Search Ride Date
        searchRideViewModel.rideDateTime.observe(viewLifecycleOwner) { rideDateTime ->
            rideDateSelectText.text = dateFormatter.format(rideDateTime.toDate())
            rideTimeSelectText.text = timeFormatter.format(rideDateTime.toDate())
        }

    }

    private fun setupOnClickListeners() {
        val driverGenderSpinner = binding.spinnerRideDetailConfigDriverGender
        val vehicleTypeSpinner = binding.spinnerRideDetailConfigVehicleType
        val rideDateSpinner = binding.spinnerRideDetailConfigScheduleDate
        val rideTimeSpinner = binding.spinnerRideDetailConfigScheduleTime


        driverGenderSpinner.setOnClickListener {
            showDriverGenderDialog()
        }

        vehicleTypeSpinner.setOnClickListener {
            showVehicleTypeDialog()
        }

        rideDateSpinner.setOnClickListener {
            showTimingDialog()
        }

        rideTimeSpinner.setOnClickListener {
            showTimingDialog()
        }
    }

    // Method to show the Bottom Dialog Fragment
    private fun showDriverGenderDialog() {
        val dialogFragment = GenderBottomDialogFragment()
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
    }

    private fun showVehicleTypeDialog() {
        val dialogFragment = VehicleTypeBottomDialogFragment()
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
    }

    private fun showTimingDialog() {
        val dialogFragment = TimingBottomDialogFragment(object: TimingBottomDialogFragment.DialogClickListener {
            override fun onCancelClick() {
                // Do nothing
            }

            override fun onConfirmClick(datetime: Timestamp) {
                searchRideViewModel.setRideDateTime(datetime)
            }

        })
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
        dialogFragment.isCancelable = false
    }



    private fun getLastDayOfYear(calendar: Calendar): Calendar {
        // Create a copy of the calendar to avoid modifying the original
        val calendarCopy = calendar.clone() as Calendar

        // Set the copy to the first day of the next year
        calendarCopy.add(Calendar.YEAR, 1)
        calendarCopy.set(Calendar.MONTH, Calendar.JANUARY)
        calendarCopy.set(Calendar.DAY_OF_MONTH, 1)

        // Subtract one day to get the last day of the current year
        calendarCopy.add(Calendar.DAY_OF_MONTH, -1)

        return calendarCopy
    }
}
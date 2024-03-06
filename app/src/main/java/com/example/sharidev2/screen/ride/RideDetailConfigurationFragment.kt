package com.example.sharidev2.screen.ride


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentRideDetailConfigurationBinding
import com.example.sharidev2.viewmodel.SharedSearchRideViewModel
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import java.util.Locale


class RideDetailConfigurationFragment : Fragment(){
    private lateinit var binding: FragmentRideDetailConfigurationBinding
    private val searchRideViewModel by activityViewModels<SharedSearchRideViewModel>()
    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("yyyy MMM dd", Locale.ENGLISH)
    private val timeFormatter = SimpleDateFormat("hh : mm a", Locale.ENGLISH)

    private lateinit var selectedDate: Date
    private lateinit var selectedTime: LocalTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ride_detail_configuration, container, false)

        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnRideDetailConfigBack


        val findRideButton = binding.btnRideDetailConfigurationCtaFindRide


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)

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
            originRideDetailText.text = origin.name
        }

        // Search Ride Destination Location
        searchRideViewModel.destination.observe(viewLifecycleOwner) { destination ->
            destinationRideDetailText.text = destination.name
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
            //showDatePickerDialog()
        }

        rideTimeSpinner.setOnClickListener {
            //showTimePickerDialog()
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
package com.example.sharidev2.screen.ride


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.data.model.SearchRide
import com.example.sharidev2.databinding.FragmentRideDetailConfigurationBinding
import com.example.sharidev2.viewmodel.SharedSearchRideViewModel
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.time.Timepoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class RideDetailConfigurationFragment :
    Fragment(),
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: FragmentRideDetailConfigurationBinding
    private val searchRideViewModel by activityViewModels<SharedSearchRideViewModel>()
    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy MMM dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("hh : mm a")


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
        val originRideDetailText = binding.textRideDetailConfigOrigin
        val destinationRideDetailText = binding.textRideDetailConfigDestination
        val driverGenderSelectText = binding.textRideDetailConfigSpinnerDriverGender
        val vehicleTypeSelectText = binding.textRideDetailConfigSpinnerVehicleType
        val rideDateSelectText = binding.textRideDetailConfigSpinnerScheduleDate
        val rideTimeSelectText = binding.textRideDetailConfigSpinnerScheduleTime
        val driverGenderSpinner = binding.spinnerRideDetailConfigDriverGender
        val vehicleTypeSpinner = binding.spinnerRideDetailConfigVehicleType
        val rideDateSpinner = binding.spinnerRideDetailConfigScheduleDate
        val rideTimeSpinner = binding.spinnerRideDetailConfigScheduleTime
        val findRideButton = binding.btnRideDetailConfigurationCtaFindRide


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)

        // VIEW MODEL OBSERVATION
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
        searchRideViewModel.rideDate.observe(viewLifecycleOwner) { rideDate ->
            rideDateSelectText.text = rideDate.format(dateFormatter)
        }

        // Search Ride Time
        searchRideViewModel.rideTime.observe(viewLifecycleOwner) { rideTime ->
            rideTimeSelectText.text = rideTime.format(timeFormatter)
        }


        // EVENT LISTENERS
        driverGenderSpinner.setOnClickListener {
            showDriverGenderDialog()
        }

        vehicleTypeSpinner.setOnClickListener {
            showVehicleTypeDialog()
        }

        rideDateSpinner.setOnClickListener {
            showDatePickerDialog()
        }

        rideTimeSpinner.setOnClickListener {
            showTimePickerDialog()
        }


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


    // Method to show the Bottom Dialog Fragment
    private fun showDriverGenderDialog() {
        val dialogFragment = GenderBottomDialogFragment()
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
    }

    private fun showVehicleTypeDialog() {
        val dialogFragment = VehicleTypeBottomDialogFragment()
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog.newInstance(
            this,
            calendar.get(Calendar.YEAR),  // Initial year selection
            calendar.get(Calendar.MONTH),  // Initial month selection
            calendar.get(Calendar.DAY_OF_MONTH) // Inital day selection
        )

        datePickerDialog.version = DatePickerDialog.Version.VERSION_2
        datePickerDialog.minDate = calendar
        datePickerDialog.maxDate = getLastDayOfYear(calendar)
        datePickerDialog.isThemeDark = true


        datePickerDialog.show(childFragmentManager, "Datepickerdialog")
    }

    private fun showTimePickerDialog() {
        val currentDate = searchRideViewModel.rideDate.value ?: LocalDate.now()

        val timePickerDialog = TimePickerDialog.newInstance(
            this,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )

        timePickerDialog.setTimeInterval(1, 5)

        if (currentDate.isEqual(LocalDate.now())) {
            timePickerDialog.setMinTime(Timepoint(Calendar.HOUR_OF_DAY, Calendar.MINUTE))
        }

        timePickerDialog.show(childFragmentManager, "Timepickerdialog")
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)

        searchRideViewModel.setRideDate(selectedDate)
    }

    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        val selectedTime = LocalTime.of(hourOfDay, minute)

        searchRideViewModel.setRideTime(selectedTime)
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
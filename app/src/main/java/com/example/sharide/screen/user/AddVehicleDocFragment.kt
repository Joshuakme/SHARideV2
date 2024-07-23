package com.example.sharide.screen.user

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sharide.R
import com.example.sharide.data.model.VehicleType
import com.example.sharide.databinding.FragmentAddVehicleDocBinding
import com.example.sharide.utility.CommonUtils
import com.example.sharide.viewmodel.VehicleDocViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddVehicleDocFragment: Fragment() {
    private lateinit var binding: FragmentAddVehicleDocBinding
    private val viewModel: VehicleDocViewModel by activityViewModels()
    private var vehicleId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddVehicleDocBinding.inflate(inflater, container, false)
        vehicleId = arguments?.getString("vehicleId")



        // Gather user input
        setupTextChangeListeners()
        setupOnClickListeners()
        return binding.root
    }

    private fun setupTextChangeListeners() {
        val firstNameTextField = binding.inputVehicleFirstName
        val lastNameTextField = binding.inputVehicleLastName
        val vehicleCapacityTextField = binding.inputVehicleCapacity
        val carPlateTextField = binding.inputCarPlate
        val vehicleBrandTextField = binding.inputVehicleBrand
        val vehicleModelTextField = binding.inputVehicleModel
        val vehicleColorTextField = binding.inputVehicleColor
        val vehicleTypeSpinner = binding.spinnerVehicleType


        vehicleTypeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedVehicleType = parent?.getItemAtPosition(position).toString()
                viewModel.setVehicleType(VehicleType.valueOf(selectedVehicleType))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // do nothing
            }

        }

        // OnTextChange Listener
        firstNameTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun afterTextChanged(e: Editable?) {
                viewModel.setFirstName(e.toString())
            }
        })

        lastNameTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun afterTextChanged(e: Editable?) {
                viewModel.setLastName(e.toString())
            }
        })

        vehicleCapacityTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun afterTextChanged(e: Editable?) {
                viewModel.setCapacity(e.toString().toInt())
            }
        })

        vehicleBrandTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun afterTextChanged(e: Editable?) {
                viewModel.setVehicleBrand(e.toString())
            }
        })

        vehicleModelTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun afterTextChanged(e: Editable?) {
                viewModel.setVehicleModel(e.toString())
            }
        })

        vehicleColorTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun afterTextChanged(e: Editable?) {
                viewModel.setVehicleColor(e.toString())
            }
        })


        carPlateTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun afterTextChanged(e: Editable?) {
                viewModel.setPlateNumber(e.toString())
            }
        })

        viewModel.manufactureDate.observe(viewLifecycleOwner) {manufactureDate ->
            binding.dateManufacture.text = CommonUtils.formatDate(manufactureDate, "yyyy-MM-dd")
        }
    }

    private fun setupOnClickListeners() {
        // ELEMENT VARIABLES
        val manufactureDate = binding.dateManufacture
        val nextVehicle = binding.cardNextVehicleDoc
        val backButton = binding.btnBackAddVehicleDoc


        manufactureDate.setOnClickListener {
            showDatePicker()
        }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_addVehicleDocFragment_to_vehicleDocFragment)
        }

        // Save vehicle documentation data
        nextVehicle.setOnClickListener {
            //check if all fields are valid
            if(isAllFieldValid()) {

                lifecycleScope.launch {
                    //val response = viewModel.addVehicleDoc()

                   // when(response) {
                       // Constants.FIREBASE_REQUEST_SUCCESS -> {
                            // Success message
//                            Toast.makeText(context, "Vehicle Documentation Submitted", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_addVehicleDocFragment_to_addVehicleDocImgFragment)
//                        }
//
//                        Constants.FIREBASE_REQUEST_FAILED -> {
//                            // Failed message
//                            Toast.makeText(context, "Please Try Again", Toast.LENGTH_SHORT).show()
//                        }
//
//                        else -> {
//                            Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
//                        }
                    }
                }
            }
        }




    private fun isAllFieldValid(): Boolean {
        val firstName = binding.inputVehicleFirstName.text.toString()
        val lastName = binding.inputVehicleLastName.text.toString()
        val vehicleBrand = binding.inputVehicleBrand.text.toString()
        val vehicleColor = binding.inputVehicleColor.text.toString()
        val vehicleCapacity = binding.inputVehicleCapacity.text.toString()
        val carPlate = binding.inputCarPlate.text.toString()
        val selectedVehicleType = binding.spinnerVehicleType.selectedItem.toString()
        val vehicleModel = binding.inputVehicleModel.text.toString()
        val manufactureDate = binding.dateManufacture.text.toString()

        // Validate first name
        if (firstName.isEmpty()) {
            Toast.makeText(context, "Please enter first name", Toast.LENGTH_SHORT).show()
            return false
        } else if (!firstName.matches(Regex("^[a-zA-Z ]*$"))) {
            binding.inputVehicleFirstName.error = "First name must only contain characters"
            return false
        }

        // Validate last name
        if (lastName.isEmpty()) {
            Toast.makeText(context, "Please enter last name", Toast.LENGTH_SHORT).show()
            return false
        } else if (!lastName.matches(Regex("^[a-zA-Z ]*$"))) {
            binding.inputVehicleLastName.error = "Last name must only contain characters"
            return false
        }

        // Validate vehicle type selection
        if (selectedVehicleType.isEmpty() || selectedVehicleType == "Select Vehicle Type") {
            Toast.makeText(context, "Please select a vehicle type", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate vehicle brand
        if (vehicleBrand.isEmpty()) {
            Toast.makeText(context, "Please enter vehicle brand", Toast.LENGTH_SHORT).show()
            return false
        } else if (!lastName.matches(Regex("^[a-zA-Z]*$"))) {
            binding.inputVehicleBrand.error = "Vehicle brand must only contain characters"
            return false
        }

        // Validate vehicle color
        if (vehicleColor.isEmpty()) {
            Toast.makeText(context, "Please enter vehicle color", Toast.LENGTH_SHORT).show()
            return false
        } else if (!lastName.matches(Regex("^[a-zA-Z]*$"))) {
            binding.inputVehicleColor.error = "Vehicle color must only contain characters"
            return false
        }

        // Validate vehicle capacity
        if (vehicleCapacity.isEmpty()) {
            Toast.makeText(context, "Please enter vehicle capacity", Toast.LENGTH_SHORT).show()
            return false
        } else if (!vehicleCapacity.matches(Regex("^[1-9]*$"))) {
            if(vehicleCapacity.isEmpty()){
                binding.inputVehicleCapacity.error = "Please enter the vehicle capacity"
            }else{
                binding.inputVehicleCapacity.error = "Vehicle capacity must only contain number"
            }
            return false
        }

        // Validate vehicle model
        if (vehicleModel.isEmpty()) {
            Toast.makeText(context, "Please enter vehicle model", Toast.LENGTH_SHORT).show()
            return false
        } else if (!vehicleModel.matches(Regex("^[a-zA-Z]*$"))) {
            binding.inputVehicleModel.error = "Vehicle model must only contain characters"
            return false
        }

        // Validate car plate
        if (carPlate.isEmpty()) {
            Toast.makeText(context, "Please enter car plate number", Toast.LENGTH_SHORT).show()
            return false
        } else if (!carPlate.matches(Regex("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]*$"))) {
            binding.inputCarPlate.error = "Car plate must contain at least one letter and one number"
            return false
        }

        // Validate manufacture date
        if (manufactureDate.isEmpty()) {
            Toast.makeText(context, "Please select manufacture date", Toast.LENGTH_SHORT).show()
            return false
        } else {
            val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(manufactureDate)
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate
            if (calendar.get(Calendar.YEAR) < 2011) {
                // Set red text color for the manufacture date field
                binding.dateManufacture.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_color))
                Toast.makeText(context, "Manufacture date must be after 2011", Toast.LENGTH_SHORT).show()
                return false
            } else {
                // Reset the text color if the date is valid
                binding.dateManufacture.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            }
        }

        // All fields are valid
        return true
    }

    private fun showDatePicker() {
        val today = Calendar.getInstance()

        val initialYear = today.get(Calendar.YEAR)
        val initialMonth = today.get(Calendar.MONTH)
        val initialDayOfMonth = today.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener {view, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month,dayOfMonth)
                }
                val selectedInstant = selectedCalendar.toInstant()

                val timestamp = Timestamp(Date.from(selectedInstant))

                viewModel.setManufactureDate(timestamp)

            },initialYear, initialMonth, initialDayOfMonth
        )


        val minDate = Calendar.getInstance()    // set minimum date to 1990 Jan 01
        minDate.set(1900, Calendar.JANUARY, 1)
        val maxDate = Calendar.getInstance()    // Today

        datePickerDialog.datePicker.minDate = minDate.timeInMillis
        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis
        datePickerDialog.datePicker.calendarViewShown = true
        datePickerDialog.datePicker.spinnersShown = false


        datePickerDialog.show()
    }
}

//    fun onDateSet(view: DateTimePicker?, year: Int, month: Int, dayOfMonth: Int) {
//        val calendar = Calendar.getInstance()
//        calendar.set(year, month, dayOfMonth)
//        val formattedDate = "${calendar.get(Calendar.DAY_OF_MONTH)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(
//            Calendar.YEAR)}"
//        binding.dateManufacture.setText(formattedDate)
//    }

//    private fun showDatePickerDialog(requireContext: Context) {
//        // Get the current date
//        val calendar = Calendar.getInstance()
//
//        // Create a DatePickerDialog with the current date as default
//        val datePickerDialog = DatePickerDialog(
//            requireContext(),
//            this,
//            calendar.get(Calendar.YEAR),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.DAY_OF_MONTH)
//        )
//
//        // Set a minimum date (January 1, 2011)
//        val minCalendar = Calendar.getInstance().apply {
//            set(2011, Calendar.JANUARY, 1)
//        }
//        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis
//
//        // Show the DatePickerDialog
//        datePickerDialog.show()
//    }

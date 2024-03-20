package com.example.sharidev2.screen.ride


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.sharidev2.R
import com.example.sharidev2.viewmodel.SharedSearchRideViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView

class TimingBottomDialogFragment(
    private val dialogClickListener: DialogClickListener
) : BottomSheetDialogFragment() {
    private val searchRideViewModel: SharedSearchRideViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_dialog_date, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the recyclerView here

        // ELEMENT VARIABLES
        //val dateTimePicker = view.findViewById<DateTimePicker>(R.id.date_picker_bottom_dialog_date)
        val cancelButton = view.findViewById<MaterialCardView>(R.id.btn_search_timing_cta_cancel)
        val confirmButton = view.findViewById<MaterialCardView>(R.id.btn_search_timing_cta_confirm)


        // LAYOUT


        // Set up NumberPicker for month and day
        //dateTimePicker.maxDaysFromToday(12)





        cancelButton.setOnClickListener {
            dialogClickListener.onCancelClick()
            dismiss()
        }

        confirmButton.setOnClickListener {
//            // Get the selected day string from the NumberPicker
//            val selectedDayIndex = numPickerMonthDay.value
//            val selectedDay = "2024 " + dayList[selectedDayIndex] // Assuming dayList contains date strings in the format "MMM dd"
//
//            // Parse the selected date string to a LocalDate object
//            val formatter = DateTimeFormatter.ofPattern("yyyy MMM dd", Locale.ENGLISH) // Use Locale.ENGLISH to ensure consistent month names
//            val selectedDate = LocalDate.parse(selectedDay, formatter)
//
//            // Get the selected hour and minute from the NumberPickers
//            val selectedHour = numPickerHour.value
//            val selectedMinute = numPickerMinute.value * 5 // Since the minute picker has intervals of 5 minutes
//
//            // Create a LocalTime object representing the selected time
//            val selectedTime = LocalTime.of(selectedHour, selectedMinute)
//
//            // Pass both the selected date and time to the ViewModel
//            val datetime = Date((selectedDate.atTime(selectedTime).toInstant(ZoneOffset.UTC).toEpochMilli()))
//            searchRideViewModel.setRideDateTime(Timestamp(datetime))


            // Dismiss the dialog
            dismiss()
        }

        // Set up initial hour and minute pickers


    }






    interface DialogClickListener {
        fun onCancelClick()
    }
}
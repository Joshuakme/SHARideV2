package com.example.sharidev2.screen.ride


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.activityViewModels
import com.example.sharidev2.R
import com.example.sharidev2.viewmodel.SharedSearchRideViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.time.ZoneOffset

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
        val numPickerMonthDay = view.findViewById<NumberPicker>(R.id.numPickerMonthDay)
        val numPickerHour = view.findViewById<NumberPicker>(R.id.numPickerHour)
        val numPickerMinute = view.findViewById<NumberPicker>(R.id.numPickerMinute)
        val cancelButton = view.findViewById<MaterialCardView>(R.id.btn_search_timing_cta_cancel)
        val confirmButton = view.findViewById<MaterialCardView>(R.id.btn_search_timing_cta_confirm)


        // LAYOUT

        val dayList = getDayList()

        // Set up NumberPicker for month and day
        numPickerMonthDay.minValue = 0
        numPickerMonthDay.maxValue = dayList.size - 1
        numPickerMonthDay.displayedValues = dayList.toTypedArray()
        numPickerMonthDay.wrapSelectorWheel = false


        // EVENT LISTENERS
        // Set up listeners for month and day picker
        numPickerMonthDay.setOnValueChangedListener { _, _, _ ->
            // Update hour and minute pickers based on selected day
            updateHourMinutePickers()
        }

        numPickerHour.setOnValueChangedListener { _, _, _ ->
            adjustMinutePicker(Calendar.getInstance(), numPickerHour.value, numPickerMinute)
        }

        cancelButton.setOnClickListener {

            dialogClickListener.onCancelClick()
            dismiss()
        }

        confirmButton.setOnClickListener {
            // Get the selected day string from the NumberPicker
            val selectedDayIndex = numPickerMonthDay.value
            val selectedDay = "2024 " + dayList[selectedDayIndex] // Assuming dayList contains date strings in the format "MMM dd"

            // Parse the selected date string to a LocalDate object
            val formatter = DateTimeFormatter.ofPattern("yyyy MMM dd", Locale.ENGLISH) // Use Locale.ENGLISH to ensure consistent month names
            val selectedDate = LocalDate.parse(selectedDay, formatter)

            // Get the selected hour and minute from the NumberPickers
            val selectedHour = numPickerHour.value
            val selectedMinute = numPickerMinute.value * 5 // Since the minute picker has intervals of 5 minutes

            // Create a LocalTime object representing the selected time
            val selectedTime = LocalTime.of(selectedHour, selectedMinute)

            // Pass both the selected date and time to the ViewModel
            val datetime = Date((selectedDate.atTime(selectedTime).toInstant(ZoneOffset.UTC).toEpochMilli()))
            searchRideViewModel.setRideDateTime(Timestamp(datetime))


            // Dismiss the dialog
            dismiss()
        }

        // Set up initial hour and minute pickers
        updateHourMinutePickers()

    }

    private fun getDayList(): List<String> {
        val dayList = mutableListOf<String>()

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        // Add today
        dayList.add(dateFormat.format(calendar.time))

        // Add days for the next two weeks
        repeat(6) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            dayList.add(dateFormat.format(calendar.time))
        }

        return dayList
    }


    private fun updateHourMinutePickers() {
        val numPickerMonthDay = requireView().findViewById<NumberPicker>(R.id.numPickerMonthDay)
        val numPickerHour = requireView().findViewById<NumberPicker>(R.id.numPickerHour)
        val numPickerMinute = requireView().findViewById<NumberPicker>(R.id.numPickerMinute)

        val selectedDay = Calendar.getInstance()
        selectedDay.add(Calendar.DAY_OF_MONTH, numPickerMonthDay.value) // Adjust to selected day

        // Get the hour and minute range based on the selected day
        val (minHourMinute, maxHourMinute) = getHourMinuteRange(selectedDay)
        // TODO: Fix the display problem of hour and minute
        // Set minimum and maximum values for the hour NumberPicker
        numPickerHour.minValue = minHourMinute.first
        numPickerHour.maxValue = maxHourMinute.first
        numPickerHour.wrapSelectorWheel = false

        // Set minimum and maximum values for the minute NumberPicker
        numPickerMinute.minValue = minHourMinute.second
        numPickerMinute.maxValue = maxHourMinute.second
        numPickerMinute.wrapSelectorWheel = false

        // Adjust minute picker based on selected day and hour
        adjustMinutePicker(selectedDay, numPickerHour.value, numPickerMinute)

        // Select the first value in the hour or minute NumberPicker
        if (selectedDay.get(Calendar.DAY_OF_YEAR) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR) || numPickerHour.value != Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            numPickerHour.value = minHourMinute.first
            numPickerMinute.value = minHourMinute.second
        }
    }

    private fun adjustMinutePicker(selectedDay: Calendar, selectedHour: Int, numPickerMinute: NumberPicker) {
        val currentCalendar = Calendar.getInstance()
        val currentDay = currentCalendar.get(Calendar.DAY_OF_YEAR)
        val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentCalendar.get(Calendar.MINUTE)

        if (selectedDay.get(Calendar.DAY_OF_YEAR) == currentDay && selectedHour == currentHour) {
            // If selected day is today and hour is the current hour
            // Round up current time to next 5-minute interval
            val nextFiveMinute = ((currentMinute + 5 - 1) / 5) * 5

            // Calculate the number of intervals needed to cover the remaining minutes until the next hour
            val remainingMinutes = 60 - currentMinute
            val remainingIntervals = if (remainingMinutes <= 5) {
                // If remaining minutes is less than or equal to 5, only one interval is needed
                1
            } else {
                // Otherwise, calculate the number of intervals needed
                (remainingMinutes + 4) / 5
            }

            // Set the minimum and maximum values for the minute picker
            numPickerMinute.minValue = nextFiveMinute / 5
            numPickerMinute.maxValue = (nextFiveMinute / 5 + remainingIntervals - 1).coerceAtMost(11) // Ensure the max value doesn't exceed 11

            val displayedMinutes = (0 until numPickerMinute.maxValue + 1).map { it * 5 + nextFiveMinute }.toTypedArray()
            numPickerMinute.displayedValues = displayedMinutes.map { String.format("%02d", it % 60) }.toTypedArray()
        } else {
            // If selected day is not today or hour is not the current hour, set the minute picker to intervals of 5 minutes
            numPickerMinute.minValue = 0
            numPickerMinute.maxValue = 11 // 11 intervals of 5 minutes (0-55)

            val displayedMinutes = (0..11).map { it * 5 }.toTypedArray()
            numPickerMinute.displayedValues = displayedMinutes.map { String.format("%02d", it) }.toTypedArray()
        }
    }



    // Function to get the minimum and maximum selectable hour and minute values
    private fun getHourMinuteRange(selectedDay: Calendar): Pair<Pair<Int, Int>, Pair<Int, Int>> {
        val calendar = Calendar.getInstance()

        // Set minimum selectable hour and minute based on the selected day
        val minHour: Int
        val minMinute: Int

        if (selectedDay.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)) {
            // If selected day is today, set minimum hour and minute to current time plus 5 minutes
            calendar.add(Calendar.MINUTE, 5)
            minHour = calendar.get(Calendar.HOUR_OF_DAY)
            minMinute = calendar.get(Calendar.MINUTE)
        } else {
            // If selected day is in the future, set minimum hour and minute to 0
            minHour = 0
            minMinute = 0
        }

        // Set maximum selectable hour and minute to 23 hours and 59 minutes
        val maxHour = 23
        val maxMinute = 59

        return Pair(Pair(minHour, minMinute), Pair(maxHour, maxMinute))
    }

    interface DialogClickListener {
        fun onCancelClick()
    }
}
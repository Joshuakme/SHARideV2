package com.example.sharidev2.utility

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.NumberPicker
import java.text.SimpleDateFormat
import java.util.*
import com.example.sharidev2.R

class DateTimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        // DEFAULT VALUES
        private const val DEFAULT_MAX_DAYS_FROM_MIN_DATE = 12

    }

    // Config
    var defaultDateFormat = "MMM dd"
    var defaultHourFormat = "hh"
    var defaultMinuteFormat = "mm"
    var defaultTimeFormat = "hh:mm a"

    private val today = Calendar.getInstance()
    var minDate: Calendar = today                                   // Default is today
    var maxDate: Calendar = getDefaultMaxDate()                     // Default is 2100/12/31
    var maxDaysFromMinDate: Int = DEFAULT_MAX_DAYS_FROM_MIN_DATE     // Default is 12 days from $minDate

    private lateinit var selectedDate: Calendar


    // Current selected date and time
    private var datePicker: NumberPicker
    private var timePickerHour: NumberPicker
    private var timePickerMinute: NumberPicker


    private lateinit var dateList: List<String>
    private var hourArray = getHourArray()
    private var minuteArray = getMinuteArray()

    private var selectedDateIndex = 0
    private var selectedHour = 0
    private var selectedMin = 0



    init {
        // Inflate the layout
        val view = LayoutInflater.from(context).inflate(R.layout.date_time_picker, this, true)

        // Init Number Pickers
        datePicker = view.findViewById(R.id.numPickerMonthDay)
        timePickerHour = view.findViewById(R.id.numPickerHour)
        timePickerMinute = view.findViewById(R.id.numPickerMinute)


        // Init Data
        selectedDate = minDate
        dateList = getMonthDayList(minDate, maxDate)
        selectedDateIndex = getDateIndex(minDate, dateList)
        Log.e("DateTimePicker","SelectedDateIndex: " + selectedDateIndex.toString())


        // Setup Number Pickers
        setupDatePicker()
    }

    // Set up date picker



    private fun setupDatePicker() {
        // Format date picker

        val todayIndex = getTodayIndex(dateList)
        val newHourArray = getHourArray(today)

        if(selectedDateIndex == todayIndex) {
            hourArray = newHourArray
            timePickerHour.maxValue = newHourArray.lastIndex

        } else {
            hourArray = getHourArray()
            timePickerHour.maxValue = hourArray.lastIndex
        }

        datePicker.displayedValues = dateList.toTypedArray()
        datePicker.value = selectedDateIndex
        datePicker.maxValue = dateList.lastIndex
        datePicker.wrapSelectorWheel = false

        setupTimeHourPicker()

        // Set a listener for date picker changes
        datePicker.setOnValueChangedListener { numPicker, oldValue, newValue ->
            numPicker.value = newValue

            if(newValue != todayIndex) {
                hourArray = getHourArray()
                timePickerHour.maxValue = hourArray.lastIndex

                setupTimeHourPicker()
            } else {
                hourArray = newHourArray
                timePickerHour.maxValue = newHourArray.lastIndex


                setupTimeHourPicker()
            }
        }
    }


    private fun setupTimeHourPicker() {
        timePickerHour.displayedValues = hourArray
        timePickerHour.value = selectedHour
        timePickerHour.minValue = 0
        timePickerHour.wrapSelectorWheel = false


        val newMinuteArray = getMinuteArray(today)

        val currentHourIndex = getHourIndex(today, hourArray.toList())

        if(selectedHour != currentHourIndex) {
            minuteArray = getMinuteArray()
            timePickerMinute.maxValue = minuteArray.lastIndex
        } else {
            minuteArray = newMinuteArray
            timePickerMinute.maxValue = newMinuteArray.lastIndex
        }
        setupTimeMinutePicker()


        timePickerHour.setOnValueChangedListener {  numPicker, oldValue, newValue ->
            numPicker.value = newValue

            if(newValue != currentHourIndex) {
                minuteArray = getMinuteArray()
                timePickerMinute.maxValue = minuteArray.lastIndex

                setupTimeMinutePicker()
            } else {
                minuteArray = newMinuteArray
                timePickerMinute.maxValue = newMinuteArray.lastIndex

                setupTimeMinutePicker()
            }
        }
    }

    private fun setupTimeMinutePicker() {
        timePickerMinute.displayedValues = minuteArray
        timePickerMinute.value = selectedMin
        timePickerMinute.minValue = 0
        timePickerMinute.wrapSelectorWheel = false

        timePickerMinute.setOnValueChangedListener {  numPicker, oldValue, newValue ->
            numPicker.value = newValue
        }
    }


    private fun getMonthDayList(minDate: Calendar, maxDate: Calendar): List<String> {
        val dateFormat = SimpleDateFormat(defaultDateFormat, Locale.getDefault())
        val dateList = mutableListOf<String>()

        val newMinDate = minDate.clone() as Calendar
        val newMaxDate = maxDate.clone() as Calendar

        if(isLastFiveMinute(minDate)) {
            newMinDate.add(Calendar.DAY_OF_MONTH, 1)
            newMaxDate.add(Calendar.DAY_OF_MONTH, 1)
        }

        while (newMinDate.before(newMaxDate) || newMinDate != newMaxDate) {
            dateList.add(dateFormat.format(newMinDate.time))
            newMinDate.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dateList.take(15)
    }

    private fun getHourArray(selectedDate: Calendar): Array<String> {
        val hourList = mutableListOf<String>()

        if(selectedDate.get(Calendar.MINUTE) >= 55) {
            for(hour in (selectedDate.get(Calendar.HOUR_OF_DAY) + 1)..23) {
                hourList.add(String.format("%02d", hour))
            }
        } else {
            for(hour in (selectedDate.get(Calendar.HOUR_OF_DAY))..23) {
                hourList.add(String.format("%02d", hour))
            }
        }

        return hourList.toTypedArray()
    }

    private fun getHourArray(): Array<String> {
        val hourList = mutableListOf<String>()

        for(hour in 0..23) {
            hourList.add(String.format("%02d", hour))
        }
        return hourList.toTypedArray()
    }

    private fun getMinuteArray(selectedDate: Calendar): Array<String> {
        val currentMinute = selectedDate.get(Calendar.MINUTE)

       val intervalMinuteArr = (0..11).map { it * 5 }.toTypedArray()

        val filteredIntervalMinuteArr = intervalMinuteArr.filter {it > currentMinute }.toTypedArray()
        return filteredIntervalMinuteArr.map { String.format("%02d", it) }.toTypedArray()
    }

    private fun getMinuteArray(): Array<String> {
        val intervalMinuteArr = (0..11).map { it * 5 }.toTypedArray()

        return intervalMinuteArr.map { String.format("%02d", it) }.toTypedArray()
    }

    private fun getDefaultMaxDate(): Calendar {
        val maxDate = Calendar.getInstance()

        maxDate.set(Calendar.YEAR, 2100)
        maxDate.set(Calendar.MONTH, Calendar.DECEMBER)
        maxDate.set(Calendar.DAY_OF_MONTH, 31)

        return maxDate
    }

    private fun getMaxDateFromMin(minDate: Calendar): Calendar {
        // Clone the minDate to avoid modifying it directly
        val maxDate = minDate.clone() as Calendar

        // Add 15 days to minDate to get maxDate
        maxDate.add(Calendar.DAY_OF_MONTH, maxDaysFromMinDate)

        return maxDate
    }

    private fun getTodayIndex(dateList: List<String>): Int {
        val today = SimpleDateFormat(defaultDateFormat, Locale.getDefault()).format(today.time)
        return dateList.indexOf(today)
    }

    private fun getDateIndex(date: Calendar, dateList: List<String>): Int {
        val date = SimpleDateFormat(defaultDateFormat, Locale.getDefault()).format(date.time)
        return dateList.indexOf(date)
    }

    private fun getHourIndex(date: Calendar, hourList: List<String>): Int {
        val date = SimpleDateFormat(defaultHourFormat, Locale.getDefault()).format(date.time)
        return hourList.indexOf(date)
    }



    private fun isLastFiveMinute(date: Calendar): Boolean {
        return (date.get(Calendar.HOUR_OF_DAY) == 23) &&
                (date.get(Calendar.MINUTE) >= 55)
    }


    // SETTER METHODS
    fun maxDaysFromToday(days: Int) {
        maxDaysFromMinDate = days

        maxDate = getMaxDateFromMin(minDate)
    }

    fun maxDate(calendar: Calendar) {
        maxDate = calendar
        setupDatePicker() // Re-setup the date picker with the new maxDate
    }

}

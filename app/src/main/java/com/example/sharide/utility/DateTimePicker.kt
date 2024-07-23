package com.example.sharide.utility

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.NumberPicker
import java.text.SimpleDateFormat
import java.util.*
import com.example.sharide.R
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DateTimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        // DEFAULT VALUES
        private const val DEFAULT_MAX_DAYS_FROM_MIN_DATE = 12

    }

    // Config
    var defaultDateFormat = "MMM dd"
    var defaultHourFormat = "HH"
    var defaultMinuteFormat = "mm"
    var defaultTimeFormat = "hh:mm a"

    private val today = Calendar.getInstance()
    var minDate: Calendar = today                                   // Default is today
    var maxDate: Calendar = getDefaultMaxDate()                    // Default is 2100/12/31
    var maxDaysFromMinDate: Int = DEFAULT_MAX_DAYS_FROM_MIN_DATE     // Default is 12 days from $minDate

    private lateinit var selectedDate: Calendar


    // Current selected date and time
    private var datePicker: NumberPicker
    private var timePickerHour: NumberPicker
    private var timePickerMinute: NumberPicker


    private val defaultHourArray = getHourArray()
    private val todayHourArray = getHourArray(today)
    private var defaultMinuteArray = getMinuteArray()
    private val todayMinuteArray = getMinuteArray(today)

    private var dateList: List<String>
    private var hourArray: Array<String>
    private var minuteArray: Array<String>

    private var selectedDateIndex = 0
    private var selectedHourIndex = 0
    private var selectedMinIndex = 0

    private var todayIndex: Int = 0
    private val currentHourIndex = getHourIndex(today, todayHourArray.toList())



    val view: View = inflate(context, R.layout.date_time_picker, this)
    init {
        // Init Number Pickers
        datePicker = view.findViewById(R.id.numPickerMonthDay)
        timePickerHour = view.findViewById(R.id.numPickerHour)
        timePickerMinute = view.findViewById(R.id.numPickerMinute)


        // Init Data
        dateList = getMonthDayList(minDate, maxDate)
        hourArray = defaultHourArray
        minuteArray = defaultMinuteArray

        selectedDate = minDate
        selectedDateIndex = getDateIndex(minDate, dateList)


        // Setup Number Pickers
        setupDatePicker()
        setupTimeHourPicker()
    }




    private fun setupDatePicker() {
        todayIndex = getTodayIndex(dateList)

        datePicker.displayedValues = dateList.toTypedArray()
        datePicker.value = selectedDateIndex
        datePicker.maxValue = dateList.lastIndex
        datePicker.wrapSelectorWheel = true


        // Init Hour Picker
        if(selectedDateIndex == todayIndex) {
            timePickerHour.displayedValues = todayHourArray
            timePickerHour.maxValue = todayHourArray.lastIndex
            hourArray = todayHourArray
        } else {
            timePickerHour.displayedValues = defaultHourArray
            timePickerHour.maxValue = defaultHourArray.lastIndex
            hourArray = defaultHourArray
        }
        setupTimeHourPicker()


        // Set a listener for date picker changes
        datePicker.setOnValueChangedListener { numPicker, oldValue, newValue ->
            selectedDateIndex = newValue

            val prevHourArr: Array<String>
            val nextHourArr: Array<String>

            if(selectedDateIndex == todayIndex) {
                selectedHourIndex = 0

                prevHourArr = defaultHourArray
                nextHourArr = todayHourArray
                hourArray = todayHourArray
            } else {
                prevHourArr = todayHourArray
                nextHourArr = defaultHourArray
                hourArray = defaultHourArray
            }

            // If new Array size is smaller
            if(prevHourArr.size > nextHourArr.size) {
                timePickerHour.maxValue = nextHourArr.lastIndex
                timePickerHour.displayedValues = nextHourArr
                timePickerHour.value = selectedHourIndex
            } else {
                // If new Array size is larger
                timePickerHour.displayedValues = nextHourArr
                timePickerHour.maxValue = nextHourArr.lastIndex
                timePickerHour.value = selectedHourIndex
            }
            setupTimeHourPicker()
        }
    }


    private fun setupTimeHourPicker() {
//        timePickerHour.value = selectedHourIndex
        timePickerHour.minValue = 0
        timePickerHour.wrapSelectorWheel = true


        if(selectedDateIndex == todayIndex && selectedHourIndex == currentHourIndex) {
            timePickerMinute.maxValue = todayMinuteArray.lastIndex
            timePickerMinute.displayedValues = todayMinuteArray
            minuteArray = todayMinuteArray
        } else {
            timePickerMinute.displayedValues = defaultMinuteArray
            timePickerMinute.maxValue = defaultMinuteArray.lastIndex
            minuteArray = defaultMinuteArray
        }
        setupTimeMinutePicker()


        timePickerHour.setOnValueChangedListener {  numPicker, oldValue, newValue ->
            selectedHourIndex = newValue

            val prevMinArr: Array<String>
            val nextMinArr: Array<String>

            if(newValue == currentHourIndex) {
                selectedMinIndex = 0

                prevMinArr = defaultMinuteArray
                nextMinArr = todayMinuteArray
            } else {
                prevMinArr = todayMinuteArray
                nextMinArr = defaultMinuteArray
            }

            // If new Array size is smaller
            if(prevMinArr.size > nextMinArr.size) {
                timePickerMinute.maxValue = nextMinArr.lastIndex
                timePickerMinute.displayedValues = nextMinArr
            } else {
                // If new Array size is larger
                timePickerMinute.displayedValues = nextMinArr
                timePickerMinute.maxValue = nextMinArr.lastIndex
            }

            setupTimeMinutePicker()
        }
    }

    private fun setupTimeMinutePicker() {
        timePickerMinute.value = selectedMinIndex
        timePickerMinute.minValue = 0
        timePickerMinute.wrapSelectorWheel = true

        timePickerMinute.setOnValueChangedListener {  numPicker, oldValue, newValue ->
            selectedMinIndex = newValue
        }
    }


    private fun getMonthDayList(minDate: Calendar, maxDate: Calendar): List<String> {
        val dateFormat = SimpleDateFormat(defaultDateFormat, Locale.getDefault())
        val dateList = mutableListOf<String>()

        val startDay = minDate.clone() as Calendar
        val endDay = maxDate.clone() as Calendar

        if (isLastFiveMinuteOfDay(minDate)) {
            startDay.add(Calendar.DAY_OF_MONTH, 1)
            endDay.add(Calendar.DAY_OF_MONTH, 1)
        }

        while (startDay.before(endDay) || startDay == endDay) {
            dateList.add(dateFormat.format(startDay.time))
            startDay.add(Calendar.DAY_OF_MONTH, 1)
            if (dateList.size >= 15) break // Exit loop if 15 dates are added
        }

        return dateList
    }

    private fun getHourArray(selectedDate: Calendar): Array<String> {
        val hourList = mutableListOf<String>()

        if(isLastFiveMinuteOfHour(selectedDate)) {
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

        return if(isLastFiveMinuteOfHour(selectedDate)) {
            getMinuteArray()
        } else {
            (0..11).map { it * 5 }
                .filter { it > currentMinute }
                .map { String.format("%02d", it) }.toTypedArray()
        }
    }

    private fun getMinuteArray(): Array<String> {
        return  (0..11).map { it * 5 }
                .map { String.format("%02d", it) }
                .toTypedArray()
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
        if(isLastFiveMinuteOfHour(date))
            date.add(Calendar.HOUR_OF_DAY, 1)

        val formattedDate = SimpleDateFormat(defaultHourFormat, Locale.getDefault()).format(date.time)

        return hourList.indexOf(formattedDate)
    }


    private fun isLastFiveMinuteOfDay(date: Calendar): Boolean {
        return (date.get(Calendar.HOUR_OF_DAY) == 23) &&
                (date.get(Calendar.MINUTE) >= 55)
    }

    private fun isLastFiveMinuteOfHour(date: Calendar): Boolean {
        return date.get(Calendar.MINUTE) >= 55
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

    fun getDateValue(): String {
        val currentYear = today.get(Calendar.YEAR)

        val monthDayFormatter = DateTimeFormatter.ofPattern("yyyy MMM dd", Locale.ENGLISH)
        val selectedDate = LocalDate.parse("$currentYear ${dateList[datePicker.value]}", monthDayFormatter)
        val monthDayCalendar = getCalendarFromLocalDate(selectedDate)

        val selectedDay = "${dateList[datePicker.value]}, ${hourArray[timePickerHour.value]}:${minuteArray[timePickerMinute.value]}"

        return if(monthDayCalendar.get(Calendar.MONTH) >= today.get(Calendar.MONTH)) {
            "$currentYear $selectedDay"
        } else {
            "${currentYear + 1} $selectedDay"
        }
    }

    private fun getCalendarFromLocalDate(localDate: LocalDate): Calendar {
        val zoneId: ZoneId = ZoneId.systemDefault() // Or specify a specific time zone if needed
        val zonedDateTime = localDate.atStartOfDay(zoneId)
        val calendar = Calendar.getInstance()
        calendar.time = Date.from(zonedDateTime.toInstant())
        return calendar
    }
}

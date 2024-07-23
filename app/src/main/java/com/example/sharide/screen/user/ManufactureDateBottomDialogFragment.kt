package com.example.sharide.screen.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import com.example.sharide.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date

class ManufactureDateBottomDialogFragment(
    private val dialogClickListener: DialogClickListener
) : BottomSheetDialogFragment() {


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
        val datePicker = view.findViewById<DatePicker>(R.id.date_picker_bottom_dialog_date)
        val cancelButton = view.findViewById<MaterialCardView>(R.id.btn_search_timing_cta_cancel)
        val confirmButton = view.findViewById<MaterialCardView>(R.id.btn_search_timing_cta_confirm)


        // LAYOUT

        // EVENT LISTENERS
        val selectedDateCalendar = Calendar.getInstance()
        val minDate = Calendar.getInstance()    // set minimum date to 1990 Jan 01
        minDate.set(1900, Calendar.JANUARY, 1)
        val maxDate = Calendar.getInstance()    // Today

        datePicker.spinnersShown = false
        datePicker.minDate = minDate.timeInMillis
        datePicker.maxDate = maxDate.timeInMillis

        datePicker.setOnDateChangedListener {pview, year, month, dayOfMonth ->
            selectedDateCalendar.set(Calendar.YEAR, year)
            selectedDateCalendar.set(Calendar.MONTH, month)
            selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }


        cancelButton.setOnClickListener {

            dialogClickListener.onCancelClick()
            dismiss()
        }

        confirmButton.setOnClickListener {
            val selectedInstant = selectedDateCalendar.toInstant()

            val timestamp = Timestamp(Date.from(selectedInstant))

            println(timestamp)

            dialogClickListener.onSaveClick(timestamp)

            // Dismiss the dialog
            dismiss()
        }
    }

    interface DialogClickListener {
        fun onCancelClick()
        fun onSaveClick(date: Timestamp)
    }
}
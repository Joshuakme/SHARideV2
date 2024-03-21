package com.example.sharidev2.screen.ride


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.sharidev2.R
import com.example.sharidev2.utility.DateTimePicker
import com.example.sharidev2.viewmodel.SharedSearchRideViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

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
        val dateTimePicker = view.findViewById<DateTimePicker>(R.id.date_picker_bottom_dialog_date)
        val cancelButton = view.findViewById<MaterialCardView>(R.id.btn_search_timing_cta_cancel)
        val confirmButton = view.findViewById<MaterialCardView>(R.id.btn_search_timing_cta_confirm)


        // LAYOUT


        // Set up NumberPicker for month and day

        cancelButton.setOnClickListener {
            dialogClickListener.onCancelClick()
            dismiss()
        }

        confirmButton.setOnClickListener {
            val selectedDateString = dateTimePicker.getDateValue()

            val formatter = DateTimeFormatter.ofPattern("yyyy MMM dd, HH:mm", Locale.ENGLISH) // Use Locale.ENGLISH to ensure consistent month names
            val selectedDate = LocalDateTime.parse(selectedDateString, formatter)

            // Pass both the selected date and time to the ViewModel
            val datetime = Date((selectedDate.toInstant(ZoneOffset.UTC).toEpochMilli()))

            dialogClickListener.onConfirmClick(Timestamp(datetime))

            // Dismiss the dialog
            dismiss()
        }

        // Set up initial hour and minute pickers


    }


    interface DialogClickListener {
        fun onCancelClick()

        fun onConfirmClick(datetime: Timestamp)
    }
}
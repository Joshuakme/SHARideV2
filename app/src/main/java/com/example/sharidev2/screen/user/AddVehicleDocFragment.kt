package com.example.sharidev2.screen.user

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.sharidev2.adapter.VehicleDocAdapter
import com.example.sharidev2.data.model.VehicleDoc
import com.example.sharidev2.databinding.FragmentAddVehicleDocBinding
import com.example.sharidev2.screen.ride.VehicleTypeBottomDialogFragment
import com.google.android.material.card.MaterialCardView
import java.util.Calendar

class AddVehicleDocFragment: Fragment() {
    private lateinit var binding: FragmentAddVehicleDocBinding
    private lateinit var vehicleDocAdapter: VehicleDocAdapter // Assuming you have a RecyclerView adapter
    private val vehicleDocs = mutableListOf<VehicleDoc>()


    private lateinit var vehicleRegisCertImageView: ImageView
    private lateinit var roadtaxImageView: ImageView
    private lateinit var insuranceImageView: ImageView
    private lateinit var uploadVehicelRegisCertButton: Button
    private lateinit var uploadRoadtaxButton: Button
    private lateinit var uploadInsuranceButton: Button
    private lateinit var saveButton: MaterialCardView
    private var vehicelRegisCertUri: Uri? = null
    private var roadtaxUri: Uri? = null
    private var insuranceUri: Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddVehicleDocBinding.inflate(inflater, container, false)

        // Initialize views
        vehicleRegisCertImageView = binding.vehicleRegCert
        roadtaxImageView = binding.vehicleRoadtax
        insuranceImageView = binding.vehicleInsurance
        uploadVehicelRegisCertButton = binding.btnUploadCert
        uploadRoadtaxButton = binding.btnUploadRoadtax
        uploadInsuranceButton = binding.btnUploadInsurance
        saveButton = binding.saveVehicleRecord

        val backButton = binding.btnBackEditVehicleDoc




        return binding.root
    }






    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun showManufactureDateDialog() {
        val dialogFragment = ManufactureDateBottomDialogFragment(
            object: ManufactureDateBottomDialogFragment.DialogClickListener{
            override fun onCancelClick() {
                // Do nothing
            }
        })
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
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



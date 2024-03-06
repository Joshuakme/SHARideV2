package com.example.sharidev2.screen.emergency

import EmergencyContactViewModel
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Contact
import com.example.sharidev2.data.repository.EmergencyContactRepository
import com.example.sharidev2.databinding.FragmentEditContactBinding
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class EditContactFragment : Fragment() {
    private lateinit var binding: FragmentEditContactBinding
    private lateinit var contact: Contact
    private val viewModel: EmergencyContactViewModel by viewModels()
    // TODO: Move data dealing in viewmodel, only call repository in viewmodel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //ELEMENT VARIABLE
        val backContactBtn = binding.btnBackEditContact


        //NAVIGATION EVENT LISTENERS
        //Enter Emergency Contact Details -> Emergency Contact Fragment
        backContactBtn.setOnClickListener{
            findNavController().navigate(R.id.action_editContactFragment_to_emergencyContactFragment)
        }


        // Retrieve the Parcelable contact from arguments
        contact = arguments?.getParcelable("contact")!!


        contact.let {
            val contactId = it.contactId
            val contactName = it.contactName
            val contactPhone = it.contactPhone
            val userUid = it.userUid


             contact = Contact(
                contactId,
                contactName,
                contactPhone,
                userUid
            )
        }

        contact.let {
            binding.inputEditEmergencyName.setText(it.contactName)
            binding.inputEditEmergencyPhoneNo.setText(it.contactPhone)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_contact, container, false)


        //ELEMENT VARIABLE
        val updateContactButton = binding.btnUpdateContactDetail
        val contactPhoneNo = binding.inputEditEmergencyPhoneNo
        val contactName = binding.inputEditEmergencyName
        val deleteContactDialog = binding.imageDeleteContact
        val bottomNavBar = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)



        // DATA VARIABLES
        var isValidNumber: Boolean = false
        val phoneNumMaxLength = 12



        // LAYOUT SETTINGS
        bottomNavBar?.visibility = View.GONE

        // Set a maximum length for the EditText (e.g., 13 characters)
        val phoneNumberFilters = arrayOf<InputFilter>(InputFilter.LengthFilter(phoneNumMaxLength))
        contactPhoneNo.filters = phoneNumberFilters





        // Check if the mobile number input is valid
        contactPhoneNo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed in this case
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed in this case
            }

            override fun afterTextChanged(s: Editable?) {
                // Check if the input is a valid number
                val input = s.toString().replace(" ", "")
                isValidNumber =
                    input.isNotEmpty() && input.toDoubleOrNull() != null && (input.length == 10 || input.length == 11)   // Exclude starting "0"

                // Add new spacing
                val formattedText = formatMobileNumber(input)

                // Update the EditText with the formatted text
                if (formattedText != s.toString()) {
                    contactPhoneNo.setText(formattedText)
                    contactPhoneNo.setSelection(formattedText.length)
                }

            }

        })


        // Delete Contact
        deleteContactDialog.setOnClickListener {
            val message: String? = "Are you sure you want to remove this emergency contact?"
            showDeleteContactDialog(message)
        }


        // Check if the input mobile number is valid, then save the edited contact
        updateContactButton.setOnClickListener {
            val completePhoneNumber = "0"+contactPhoneNo.text.toString().replace(" ", "")

            if(completePhoneNumber.length == 10 || completePhoneNumber.length == 11) {
                // Able to Save the Edited Emergency Contact
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    updateContact()

                }
                Toast.makeText(requireContext(), "Edit Contact Successfully!", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(requireContext(), "Invalid Phone Number", Toast.LENGTH_SHORT).show()
            }
        }





        return binding.root

    }



    //Delete Emergency Contact Function
    private fun showDeleteContactDialog(message: String?){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.delete_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage: TextView = dialog.findViewById(R.id.tv_message_delete_contact)
        val btnDelete : Button = dialog.findViewById(R.id.btn_delete_contact)
        val btnCancel : Button = dialog.findViewById(R.id.btn_cancel)

        tvMessage.text = message

        btnDelete.setOnClickListener{
            val user = Firebase.auth.currentUser

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                val isDeleted = viewModel.deleteContact(contact.contactId!!)

                when(isDeleted) {
                    Constants.FIREBASE_REQUEST_SUCCESS -> {
                        // Deletion successful
                        Toast.makeText(context, "Contact Deleted", Toast.LENGTH_SHORT).show()

                        findNavController().navigate(R.id.action_editContactFragment_to_emergencyContactFragment)
                    }

                    Constants.FIREBASE_REQUEST_NOT_BELONG_USER,
                    Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED,
                    Constants.FIREBASE_REQUEST_EXCEPTION -> {
                        // Contact doesn't belong to the current user
                        Toast.makeText(context, "Failed to remove contact", Toast.LENGTH_SHORT).show()
                        Log.e("Delete Contact", isDeleted.toString())
                    }
                }
            }

            dialog.dismiss()
        }

        btnCancel.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }



    private suspend fun updateContact() {
        val contactName = binding.inputEditEmergencyName
        val contactPhoneNo = binding.inputEditEmergencyPhoneNo

        try {
            val newContact = Contact(contact.contactId, contactName.text.toString(), contactPhoneNo.text.toString(), FirebaseClient.firebaseAuth.currentUser?.uid)
            val updateContact = viewModel.updateContact(newContact)
            Log.d("UpdateContact", "Update successful: $updateContact")

            // Navigate back to the EmergencyContactFragment
            findNavController().popBackStack()
        } catch (e: Exception) {
            Log.e("UpdateContact", "Error updating contact: ${e.message}", e)
        }
    }




    private fun formatMobileNumber(originalText: String): String {
        val formattedText = StringBuilder()

        for (i in originalText.indices) {
            if(originalText.length in 1..9) {
                if (i > 0 && i == 2 || i > 0 && i == 5) {
                    formattedText.append(" ") // Add a space after every 4 characters
                }
            } else if (originalText.length == 10) {
                if (i > 0 && i == 2 || i > 0 && i == 6) {
                    formattedText.append(" ") // Add a space after every 4 characters
                }
            }

            formattedText.append(originalText[i])
        }
        return formattedText.toString()
    }
}
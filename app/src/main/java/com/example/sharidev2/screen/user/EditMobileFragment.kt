package com.example.sharidev2.screen.user

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.R
import com.example.sharidev2.data.model.User
import com.example.sharidev2.databinding.FragmentEditDisplayNameBinding
import com.example.sharidev2.databinding.FragmentEditMobileBinding
import com.example.sharidev2.viewmodel.PersonalInfoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditMobileFragment: Fragment() {
    private lateinit var binding: FragmentEditMobileBinding
    private lateinit var viewModel: PersonalInfoViewModel
    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edit_mobile, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(PersonalInfoViewModel::class.java)
        val saveMobileBtn = binding.btnUpdateMobile
        val backMobileBtn = binding.btnBackEditMobile
        val mobileNumber = binding.inputEditMobile

        // DATA VARIABLES
        var isValidNumber: Boolean = false
        val phoneNumMaxLength = 12

        // Set a maximum length for the EditText (e.g., 13 characters)
        val phoneNumberFilters = arrayOf<InputFilter>(InputFilter.LengthFilter(phoneNumMaxLength))
        mobileNumber.filters = phoneNumberFilters

// Fetch current display name from the database
        viewModel.fetchMobileFromDatabase()


        // Observe the current display name
        viewModel.mobile.observe(viewLifecycleOwner) { mobile ->
            binding.inputEditMobile.setText(mobile)
        }

        // Set up click listener for Save button
        saveMobileBtn.setOnClickListener {
            val newMobile = binding.inputEditMobile.text.toString()

            // Update the mobile in the ViewModel
            if(newMobile.length == 11 || newMobile.length == 12) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.updateMobile(newMobile)

                    Toast.makeText(
                        requireContext(),
                        "Update mobile phone number successfully!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    findNavController().popBackStack()
                }
            }else{
                Toast.makeText(requireContext(), "Failed to edit mobile phone number!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        backMobileBtn.setOnClickListener {
            findNavController().navigate(R.id.action_editMobileFragment3_to_personalInformationFragment)
        }


        // Check if the mobile number input is valid
        mobileNumber.addTextChangedListener(object : TextWatcher {
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
                    mobileNumber.setText(formattedText)
                    mobileNumber.setSelection(formattedText.length)
                }

            }

        })
    }

    private suspend fun updateMobile() {
        val mobile = binding.inputEditMobile

        try {
            if(user.phoneNumber != null) {
                val updateMobile = viewModel.updateDisplayName(user.phoneNumber!!)

                Log.d("UpdateMobile", "Update successful: $updateMobile")

                // Navigate back to the EmergencyContactFragment
                findNavController().popBackStack()
            }else{
                Toast.makeText(requireContext(), "Failed to edit mobile phone number!", Toast.LENGTH_SHORT)
                    .show()
            }

        } catch (e: Exception) {
            Log.e("UpdateMobileNumber", "Error updating mobile: ${e.message}", e)
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

package com.example.sharidev2.screen.emergency

import EmergencyContactViewModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Contact
import com.example.sharidev2.databinding.FragmentAddContactBinding
import com.example.sharidev2.utility.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddContactFragment : Fragment() {
    private lateinit var binding:FragmentAddContactBinding
    private val viewModel: EmergencyContactViewModel by viewModels()
    private var nodeId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            nodeId = it.getString("contact_id").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =FragmentAddContactBinding.inflate(inflater,container,false)
        val root:View = binding.root

        // ELEMENT VARIABLES
        val inputContactName = binding.inputEmergencyName
        val inputContactPhoneNo = binding.inputEmergencyPhoneNo

        binding.btnSaveContactDetail.setOnClickListener{

            if(!inputContactName.text.isNullOrBlank() &&
                !inputContactPhoneNo.text.isNullOrBlank()
                && (inputContactPhoneNo.length() == 10 || inputContactPhoneNo.length() == 11)) {

                viewLifecycleOwner.lifecycleScope.launch {
                    val respond = viewModel.addContact(
                        Contact(
                            contactName = inputContactName.text.toString(),
                            contactPhone = inputContactPhoneNo.text.toString()
                        )
                    )

                    when(respond)  {
                        Constants.FIREBASE_REQUEST_SUCCESS -> {
                            Toast.makeText(requireContext(), "Contact Added", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_addContactFragment_to_emergencyContactFragment)
                        }

                        Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED -> {
                            Toast.makeText(requireContext(), "Please login to add contact", Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            Toast.makeText(requireContext(), "Failed to add contact", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else if(inputContactName.text.isNullOrBlank() ||
                inputContactPhoneNo.text.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()

            } else if(!(inputContactPhoneNo.length() == 10 || inputContactPhoneNo.length() == 11))  {
                Toast.makeText(requireContext(), "Invalid phone number", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(requireContext(), "Invalid data", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBackContactDetail.setOnClickListener{
            findNavController().popBackStack()
        }
        return root
    }
}
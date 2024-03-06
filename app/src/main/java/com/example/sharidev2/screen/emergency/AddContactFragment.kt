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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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

        binding.btnSaveContactDetail.setOnClickListener(){
            lifecycleScope.launch(Dispatchers.IO) {
                val respond = viewModel.addContact(
                    Contact(
                        contactName = inputContactName.text.toString(),
                        contactPhone = inputContactPhoneNo.text.toString()
                    )
                )

                lifecycleScope.launch(Dispatchers.Main){
                    when(respond) {
                        Constants.FIREBASE_REQUEST_SUCCESS -> {
                            Toast.makeText(requireContext(), "Contact Added", Toast.LENGTH_SHORT).show()
                        }

                        Constants.FIREBASE_REQUEST_NOT_BELONG_USER,
                        Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED,
                        Constants.FIREBASE_REQUEST_EXCEPTION -> {
                            Toast.makeText(requireContext(), "Failed to add contact", Toast.LENGTH_SHORT).show()
                        }
                    }

                    findNavController().navigate(R.id.action_addContactFragment_to_emergencyContactFragment)
                }
            }
        }

        binding.btnBackContactDetail.setOnClickListener{
            findNavController().navigate(R.id.action_addContactFragment_to_emergencyContactFragment)
        }
        return root
    }
}
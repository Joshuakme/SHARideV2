package com.example.sharidev2.screen.emergency


import EmergencyContactViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharidev2.R
import com.example.sharidev2.adapter.ContactAdapter
import com.example.sharidev2.databinding.FragmentEmergencyContactBinding

class EmergencyContactFragment : Fragment(),
    ContactAdapter.OnItemClickListener
{
    private lateinit var binding: FragmentEmergencyContactBinding
    private val viewModel: EmergencyContactViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_emergency_contact, container, false)

        // ELEMENT VARIABLES
        val addContactButton = binding.btnAddContact
        val reminderAddContactCard = binding.cardEmergencyContactReminder
        val emergencyContactProgressBar = binding.progressBarEmergencyContact
        val emergencyContactsRecyclerView = binding.recyclerViewEmergencyContacts
        var adapter: ContactAdapter

        // Observe the LiveData from the ViewModel
        viewModel.emergencyContactList.observe(viewLifecycleOwner) { emergencyContactList ->

            if(emergencyContactList == null) {
                emergencyContactProgressBar.visibility = View.VISIBLE
                emergencyContactsRecyclerView.visibility = View.GONE
                reminderAddContactCard.visibility = View.GONE
            } else {
                emergencyContactList?.let { list ->
                    if (list.isNotEmpty()) {
                        adapter = ContactAdapter(list, this)
                        emergencyContactsRecyclerView.layoutManager = LinearLayoutManager(context)
                        emergencyContactsRecyclerView.adapter = adapter

                        emergencyContactProgressBar.visibility = View.GONE
                        emergencyContactsRecyclerView.visibility = View.VISIBLE
                        reminderAddContactCard.visibility = View.GONE
                    } else {
                        // If the list is empty, hide the RecyclerView and show the reminder card
                        emergencyContactProgressBar.visibility = View.GONE
                        emergencyContactsRecyclerView.visibility = View.GONE
                        reminderAddContactCard.visibility = View.VISIBLE
                    }
                }
            }
        }

        // EVENT LISTENERS
        addContactButton.setOnClickListener {
            findNavController().navigate(R.id.action_emergencyContactFragment_to_addContactFragment)
        }

        binding.btnBackEmergencyContact.setOnClickListener {
            findNavController().navigate(R.id.action_emergencyContactFragment_to_profileFragment)
        }


        return binding.root
    }

    override fun onItemClick(position: Int) {
        // Retrieve the clicked contact from the adapter
        val clickedContact = viewModel.emergencyContactList.value?.get(position)


        // Check if the clicked contact is not null
        clickedContact?.let {
            // Create a bundle to pass contact data to the EditContactFragment
            val bundle = Bundle().apply {
                putParcelable("contact", it) // Put Parcelable contact into the bundle
            }

            // Navigate to the EditContactFragment and pass the bundle
            findNavController().navigate(R.id.action_emergencyContactFragment_to_editContactFragment, bundle)
        }
    }
}
package com.example.sharidev2.screen.user

import android.os.Bundle
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
import com.example.sharidev2.viewmodel.PersonalInfoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditDisplayNameFragment: Fragment() {
    private lateinit var binding: FragmentEditDisplayNameBinding
    private lateinit var viewModel: PersonalInfoViewModel
    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edit_display_name, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(PersonalInfoViewModel::class.java)
        val saveDisplayNameBtn = binding.btnUpdateUsername
        val backDisplayNameBtn = binding.btnBackEditUsername

// Fetch current display name from the database
        viewModel.fetchDisplayNameFromDatabase()


        // Observe the current display name
        viewModel.displayName.observe(viewLifecycleOwner) { displayName ->
            binding.inputEditUsername.setText(displayName)
        }

        // Set up click listener for Save button
        saveDisplayNameBtn.setOnClickListener {
            val newDisplayName = binding.inputEditUsername.text.toString()
            // Update the display name in the ViewModel
            if (viewModel.isDisplayNameValid(newDisplayName)) {

                viewModel.updateDisplayName(newDisplayName)

                Toast.makeText(
                    requireContext(),
                    "Update Username Successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Invalid display name. Please ensure it is 2-24 characters long and does not contain invalid characters.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        backDisplayNameBtn.setOnClickListener {
            findNavController().navigate(R.id.action_editDisplayNameFragment_to_personalInformationFragment)
        }
    }
}
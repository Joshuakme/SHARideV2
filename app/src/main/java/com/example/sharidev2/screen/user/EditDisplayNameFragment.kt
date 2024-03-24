package com.example.sharidev2.screen.user

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.data.model.User
import com.example.sharidev2.databinding.FragmentEditDisplayNameBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.viewmodel.PersonalInfoViewModel
import com.example.sharidev2.viewmodel.SharedCurrentUserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditDisplayNameFragment: Fragment() {
    private lateinit var binding: FragmentEditDisplayNameBinding
    private val currentUserViewModel: SharedCurrentUserViewModel by activityViewModels()
    private lateinit var context: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edit_display_name, container, false)


        context = if(getContext() != null) {
            requireContext()
        } else {
            requireActivity().applicationContext
        }


        // LAYOUT SETTINGS
        val activity = activity as MainActivity
        activity.setStatusBarColor(CommonUtils().getThemeColor(context, android.R.attr.colorBackground))
        activity.setBottomNavVisible(false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // ELEMENT VARIABLES
        val saveDisplayNameBtn = binding.btnUpdateUsername
        val backDisplayNameBtn = binding.btnBackEditUsername


        // Observe the current display name
        currentUserViewModel.displayName.observe(viewLifecycleOwner) { displayName ->
            binding.inputEditUsername.setText(displayName)
        }

        // Set up click listener for Save button
        saveDisplayNameBtn.setOnClickListener {
            val newDisplayName = binding.inputEditUsername.text.toString()
            // Update the display name in the ViewModel
            if (isDisplayNameValid(newDisplayName)) {

                currentUserViewModel.updateDisplayName(newDisplayName)

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
            findNavController().popBackStack()
        }
    }


    //Checks if the provided display name is valid
    private fun isDisplayNameValid(displayName: String): Boolean {
        // Define the regex pattern for valid display names
        val regex = "^[a-zA-Z0-9_\\-\\.\\s]{2,25}$".toRegex()

        // Check if the display name matches the pattern and does not contain invalid characters
        return regex.matches(displayName) && !displayName.contains("!") &&
                !displayName.contains("@") && !displayName.contains("#") &&
                !displayName.contains("$") && !displayName.contains("%") &&
                !displayName.contains("^") && !displayName.contains("&") &&
                !displayName.contains("*") && !displayName.contains("(") &&
                !displayName.contains(")") && !displayName.contains("-") &&
                !displayName.contains("_") && !displayName.contains("=") &&
                !displayName.contains("+") && !displayName.contains("/") &&
                !displayName.contains("<") && !displayName.contains(">") &&
                !displayName.contains("?") && !displayName.contains("`") &&
                !displayName.contains("~")
    }
}
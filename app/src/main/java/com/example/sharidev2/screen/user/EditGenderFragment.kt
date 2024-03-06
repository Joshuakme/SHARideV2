package com.example.sharidev2.screen.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.databinding.FragmentEditGenderBinding
import com.example.sharidev2.viewmodel.PersonalInfoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditGenderFragment : Fragment() {

    private lateinit var binding: FragmentEditGenderBinding
    private lateinit var viewModel: PersonalInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditGenderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backEditGenderBtn = binding.btnBackEditGender
        val saveGenderBtn = binding.btnUpdateGender
        val radioGroupGender = binding.radioGroupGender

        viewModel = ViewModelProvider(requireActivity()).get(PersonalInfoViewModel::class.java)

        backEditGenderBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        saveGenderBtn.setOnClickListener {
            val selectedRadioButtonId = radioGroupGender.checkedRadioButtonId
            if (selectedRadioButtonId != -1) {
                val selectedGender = view.findViewById<RadioButton>(selectedRadioButtonId).text.toString()
                GlobalScope.launch(Dispatchers.Main) {
                    viewModel.updateGender(selectedGender)
                    Toast.makeText(requireContext(), "Gender updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            } else {
                Toast.makeText(requireContext(), "Please select a gender", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.gender.observe(viewLifecycleOwner) { gender ->
            if (gender != null) {
                if (gender == "Male") {
                    binding.radioButtonMale.isChecked = true
                } else {
                    binding.radioButtonFemale.isChecked = true
                }
            }
        }
    }
}

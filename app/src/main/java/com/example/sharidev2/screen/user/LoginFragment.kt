package com.example.sharidev2.screen.user

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.R
import com.example.sharidev2.adapter.LoginSpinnerAdapter
import com.example.sharidev2.databinding.FragmentLoginBinding
import com.example.sharidev2.model.Country
import com.google.android.material.bottomnavigation.BottomNavigationView


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,  R.layout.fragment_login, container, false)


        // ELEMENT VARIABLES
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val backBtn = binding.imgBtnLoginNavBack
        val spinnerCountry: Spinner = binding.spinnerLoginMobileCountryCode

        // DATA VARIABLES
        val countryList = getLoginCountryList()


        // LAYOUT SETTINGS
        bottomNav?.visibility = View.GONE


        // ADAPTER
        val adapter = LoginSpinnerAdapter(requireContext(), countryList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCountry.adapter = adapter


        // EVENT LISTENERS
        spinnerCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCountry: Country = countryList[position]
                // Handle the selected country (e.g., store the code in a variable)
                val selectedCountryCode = selectedCountry.countryCode
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle nothing selected if needed
            }
        }


        // NAVIGATION EVENT LISTENERS
        // Profile Fragment -> Personal Information Fragment
        backBtn.setOnClickListener {
            //findNavController().navigate(R.id.action_profileFragment_to_personalInformationFragment)
            findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
        }




        return binding.root

    }


    private fun getLoginCountryList(): List<Country> {
        return listOf(
            Country("Malaysia", "+60"),
            Country("Singapore", "+65"),
            Country("Indonesia", "+62")
        )
    }
}
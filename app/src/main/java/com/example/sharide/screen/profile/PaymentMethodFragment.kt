package com.example.sharide.screen.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.sharide.MainActivity
import com.example.sharide.R
import com.example.sharide.databinding.FragmentPaymentMethodBinding

class PaymentMethodFragment : Fragment() {
    // Global Variables Init
    private lateinit var binding: FragmentPaymentMethodBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payment_method, container, false)

        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnProfilePaymentMethodNavBack


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)


        // NAVIGATION EVENT LISTENERS
        // Personal Information Fragment -> Profile Fragment
        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_paymentMethodFragment_to_profileFragment)
        }



        return binding.root
    }

}
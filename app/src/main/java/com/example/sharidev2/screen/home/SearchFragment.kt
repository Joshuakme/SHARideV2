package com.example.sharidev2.screen.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    // Variables Init
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)


        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnSearchBack

        // NAVIGATION EVENT LISTENERS
        // Search Fragment -> Home Fragment
        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_homeFragment)
        }

        return binding.root
    }


}
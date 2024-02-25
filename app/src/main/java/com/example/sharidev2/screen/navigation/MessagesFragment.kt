package com.example.sharidev2.screen.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentMessagesBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MessagesFragment : Fragment() {
    // Variables Init
    private lateinit var binding: FragmentMessagesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_messages, container, false)


        // ELEMENT VARIABLES
        val testChatCard = binding.cardChat


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(true)

        // EVENT LISTENERS
        // *** View Ali Chat ***
        testChatCard.setOnClickListener {
            findNavController().navigate(R.id.action_messagesFragment_to_chatFragment)
        }



        return binding.root
    }
}
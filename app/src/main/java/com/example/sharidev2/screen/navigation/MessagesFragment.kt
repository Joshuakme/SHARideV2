package com.example.sharidev2.screen.navigation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.adapter.ChatAdapter
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.databinding.FragmentMessagesBinding
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.viewmodel.ChatViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ListenerRegistration

class MessagesFragment : Fragment() {
    // Variables Init
    private lateinit var binding: FragmentMessagesBinding

    private val chatViewModel: ChatViewModel by viewModels()
    private val currentUser = FirebaseClient.firebaseAuth.currentUser

    private lateinit var context: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_messages, container, false)


        if(isAdded) {
            context = requireContext()
        } else {
            if (activity != null) {
                context = requireActivity().applicationContext
            }
        }


        // ELEMENT VARIABLES
        val testChatCard = binding.cardChat
        val chatsRecyclerView = binding.recyclerChats


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(true)
        (activity as MainActivity).resetBottomNavPosition()
        loadingMessages(Constants.UI_DATA_LOADING)


        if(currentUser?.uid != null) {
            chatViewModel.startListeningForUserChatsUpdates(currentUser.uid, object: (List<Chat>) -> Unit {
                override fun invoke(chatList: List<Chat>) {
                    if(chatList.isNotEmpty()) {
                        chatViewModel.setChatList(chatList)

                        val adapter = ChatAdapter(chatList, object: ChatAdapter.OnChatClickListener {
                            override fun onChatClick(chat: Chat) {
                                val action = MessagesFragmentDirections.actionMessagesFragmentToChatFragment(chat)
                                findNavController().navigate(action)
                            }
                        })

                        chatsRecyclerView.adapter = adapter
                        chatsRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

                        loadingMessages(Constants.UI_DATA_SUCCESS)
                    } else {
                        loadingMessages(Constants.UI_DATA_FAILED)
                    }
                }
            })
        }



        // EVENT LISTENERS
        // *** View Ali Chat ***
        testChatCard.setOnClickListener {
            findNavController().navigate(R.id.action_messagesFragment_to_chatFragment)
        }



        return binding.root
    }


    private fun loadingMessages(status: Int) {
        val chatsRecyclerView = binding.recyclerChats
        val chatLoadingProgressCl = binding.clChatLoadingSpinner
        val chatLoadErrorCard = binding.cardErrorLoadChat

        when(status) {
            Constants.UI_DATA_SUCCESS -> {
                chatsRecyclerView.visibility = View.VISIBLE
                chatLoadingProgressCl.visibility = View.GONE
                chatLoadErrorCard.visibility = View.GONE
            }

            Constants.UI_DATA_LOADING -> {
                chatsRecyclerView.visibility = View.GONE
                chatLoadingProgressCl.visibility = View.VISIBLE
                chatLoadErrorCard.visibility = View.GONE
            }

            Constants.UI_DATA_FAILED -> {
                chatsRecyclerView.visibility = View.GONE
                chatLoadingProgressCl.visibility = View.GONE
                chatLoadErrorCard.visibility = View.VISIBLE
            }
        }
    }
}
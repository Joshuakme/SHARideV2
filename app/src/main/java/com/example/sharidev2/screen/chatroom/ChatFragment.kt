package com.example.sharidev2.screen.chatroom

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.adapter.ChatAdapter
import com.example.sharidev2.adapter.MessageAdapter
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.databinding.FragmentChatBinding
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.data.model.MessageType
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.viewmodel.ChatViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch


class ChatFragment : Fragment() {
    // Global Variables Init
    private lateinit var binding: FragmentChatBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var chat: Chat

    private lateinit var context: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)


        if(isAdded) {
            context = requireContext()
        } else {
            if (activity != null) {
                context = requireActivity().applicationContext
            }
        }

        // DATA
        try {
            chat = arguments?.get("chat")!! as Chat
        } catch (e: Exception) {
            Log.e("Single Chat Fragment", e.message.toString())
        }


        // ELEMENTS
        val navBackButton = binding.imgBtnChatBack
        val chatTitle = binding.textChatTitle
        val chatMessagesRecyclerView = binding.recyclerViewChatMessages
        val chatTextInput = binding.editTextMessagesChatInput
        val chatSendButton = binding.cardChatSendBtn
        val chatTextInputContainer = binding.llChatBottomNav


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)


        if(chat != null) {
            chatViewModel.setActiveChat(chat)

            chatViewModel.startListeningForChatUpdates(chat.chatId!!, object: (Chat?) -> Unit {
                override fun invoke(latestChat: Chat?) {
                    if(latestChat != null) {
                        chatTitle.text = latestChat.chatTitle

                        // Set Up RecyclerView
                        val chatAdapter = MessageAdapter(requireContext(), latestChat.messages!!.toList())
                        chatMessagesRecyclerView.layoutManager = LinearLayoutManager(context)
                        chatMessagesRecyclerView.adapter = chatAdapter
                    } else {
                        findNavController().popBackStack()
                    }
                }
            })
        }


        // EVENT LISTENERS
        // Navigate back to Messages Fragment
        navBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Chat Text Input Height Adjust
        chatTextInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                if(editable.isNullOrBlank()) {
                    enableSendButton(false)
                } else {
                    enableSendButton(true)
                }


                // Get the number of lines in the EditText
                val lineCount = chatTextInput.lineCount

                // Set a maximum of 3 lines
                val maxLines = 3
                val minHeight = resources.getDimension(R.dimen.ss_messages_chat_bottom_nav_height).toInt()

                // Calculate the height based on line count
                val lineHeight = chatTextInput.lineHeight   // 53px

                val newHeight = if(lineCount in 1..maxLines) {
                    minHeight + (lineCount - 1) * lineHeight
                } else if(lineCount > maxLines) {
                    minHeight + (maxLines - 1) * lineHeight
                } else {
                    minHeight
                }

                // Set the new height to the container
                chatTextInputContainer.layoutParams.height = newHeight
                chatTextInputContainer.requestLayout()
            }
        })


        chatSendButton.setOnClickListener {

            val message = chatTextInput.text.toString()

            val newMessage = Message(text = message)

            loadingSendMessage(Constants.UI_DATA_LOADING)
            lifecycleScope.launch {
                val respond = chatViewModel.addMessageToChat(chat.chatId!!, newMessage)

                loadingSendMessage(respond)
            }
        }


        return binding.root
    }


    private fun loadingSendMessage(status: Int) {
        val chatTextInput = binding.editTextMessagesChatInput
        val chatSendButtonIcon = binding.imgBtnMessagesChatSend
        val chatSendButtonLoading = binding.progressBarChatSendLoading

        when(status) {
            Constants.UI_DATA_LOADING -> {
                enableSendButton(false)
                chatSendButtonIcon.visibility = View.GONE
                chatSendButtonLoading.visibility = View.VISIBLE
            }

            else -> {
                enableSendButton(true)
                chatSendButtonIcon.visibility = View.VISIBLE
                chatSendButtonLoading.visibility = View.GONE

                chatTextInput.text.clear()
            }
        }
    }

    private fun enableSendButton(enable: Boolean) {
        val chatSendButton = binding.cardChatSendBtn
        val chatSendButtonIcon = binding.imgBtnMessagesChatSend

        if(enable) {
            chatSendButton.isEnabled = true
            chatSendButton.isClickable = true

            chatSendButton.visibility = View.VISIBLE
        } else {
            chatSendButton.isEnabled = false
            chatSendButton.isClickable = false

            chatSendButton.visibility = View.GONE
        }
    }
}
package com.example.sharidev2.screen.chatroom

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
<<<<<<< HEAD
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentChatBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class ChatFragment : Fragment() {
    // Global Variables Init
    private lateinit var binding: FragmentChatBinding
=======
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.adapter.ChatAdapter
import com.example.sharidev2.databinding.FragmentChatBinding
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.data.model.MessageType
import com.google.android.material.bottomnavigation.BottomNavigationView



class ChatFragment : Fragment() {
    // Global Variables Init
    private lateinit var binding: FragmentChatBinding
    private val chatMessageList: MutableList<Message> = getMessageChat()

>>>>>>> main
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)


        // ELEMENTS
<<<<<<< HEAD
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navBackButton = binding.imgBtnChatBack
        val chatMessagesRecyclerView = binding.recyclerViewChatMessages
        val chatTextInput = binding.editTextMessagesChatInput
=======
        val navBackButton = binding.imgBtnChatBack
        val chatMessagesRecyclerView = binding.recyclerViewChatMessages
        val chatTextInput = binding.editTextMessagesChatInput
        val chatSendButton = binding.imgBtnMessagesChatSend
>>>>>>> main
        val chatTextInputContainer = binding.llChatBottomNav


        // LAYOUT SETTINGS
<<<<<<< HEAD
        bottomNav?.visibility = View.GONE

        // Set Up RecyclerView
        chatMessagesRecyclerView
=======
        (activity as MainActivity).setBottomNavVisible(false)

        // Set Up RecyclerView
        val chatAdapter = ChatAdapter(chatMessageList)
        chatMessagesRecyclerView.layoutManager = LinearLayoutManager(context)
        chatMessagesRecyclerView.adapter = chatAdapter
>>>>>>> main


        // EVENT LISTENERS
        // Navigate back to Messages Fragment
        navBackButton.setOnClickListener {
            findNavController().navigate(R.id.action_chatFragment_to_messagesFragment)
        }

        // Chat Text Input Height Adjust
        chatTextInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
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


<<<<<<< HEAD

        return binding.root
    }
=======
        chatSendButton.setOnClickListener {
            val message = chatTextInput.text.toString()

            val newMessage = Message("m3", "s3", message, readBy = emptyList(), messageType = MessageType.TEXT)

            chatMessageList.add(newMessage)

            chatTextInput.text.clear()
        }


        return binding.root
    }


    private fun getMessageChat(): MutableList<Message> {
        return mutableListOf(
            Message(
                    "m1",
                    "s1",
                    "Hi, I'm ALi. Nice to meet you.",
                    readBy = listOf<String>(),
                    messageType = MessageType.TEXT
                ),
            Message(
                "m2",
                "s2",
                "Yooo",
                readBy = listOf<String>(),
                messageType = MessageType.TEXT
            ),
        )
    }
>>>>>>> main
}
package com.example.sharidev2.screen.chatroom

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentChatBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)


        // ELEMENTS
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val chatMessagesRecyclerView = binding.recyclerViewChatMessages
        val chatTextInput = binding.editTextMessagesChatInput
        val chatTextInputContainer = binding.llChatBottomNav


        // LAYOUT SETTINGS
        bottomNav?.visibility = View.GONE



        // EVENT LISTENERS
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
                val lineHeight = chatTextInput.lineHeight   // 53dp

                val newHeight = if(lineCount in 1..3) {
                    minHeight + (lineCount - 1) * lineHeight
                } else if(lineCount > maxLines) {
                    minHeight + maxLines * lineHeight
                } else {
                    minHeight
                }

                // Set the new height to the container
                chatTextInputContainer.layoutParams.height = newHeight
                chatTextInputContainer.requestLayout()
            }
        })



        return binding.root
    }
}
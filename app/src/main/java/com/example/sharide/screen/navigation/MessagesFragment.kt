package com.example.sharide.screen.navigation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharide.MainActivity
import com.example.sharide.R
import com.example.sharide.adapter.ChatAdapter
import com.example.sharide.data.model.Chat
import com.example.sharide.data.model.Message
import com.example.sharide.data.model.User
import com.example.sharide.databinding.FragmentMessagesBinding
import com.example.sharide.utility.CommonUtils
import com.example.sharide.utility.Constants
import com.example.sharide.viewmodel.ChatViewModel
import com.example.sharide.viewmodel.SharedCurrentUserViewModel

class MessagesFragment : Fragment() {
    // Variables Init
    private lateinit var binding: FragmentMessagesBinding

    private val chatViewModel: ChatViewModel by viewModels()
    private val currentUserViewModel: SharedCurrentUserViewModel by activityViewModels()
    private var currentUser: User? = null

    private lateinit var context: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_messages, container, false)


        if(isAdded) {
            context = requireContext()
        } else {
            if (activity != null) {
                context = requireActivity().applicationContext
            }
        }

        currentUser = currentUserViewModel.user.value

        // ELEMENT VARIABLES
        val testChatCard = binding.cardChat
        val chatsRecyclerView = binding.recyclerChats


        // LAYOUT SETTINGS
        val activity = activity as MainActivity
        activity.setStatusBarColor(CommonUtils().getThemeColor(context, android.R.attr.colorBackground))
        activity.setBottomNavVisible(true)
        activity.resetBottomNavPosition()
        loadingMessages(Constants.UI_DATA_LOADING)


        if(currentUser?.uid != null) {
            chatViewModel.startListeningForUserChatsUpdates(currentUser!!.uid!!, object: (List<Chat>) -> Unit {
                override fun invoke(newChatList: List<Chat>) {
                    if(newChatList.isNotEmpty()) {
                        val newSortedChatList = newChatList.sortedByDescending { it.timestamp }

                        chatViewModel.setChatList(newSortedChatList)

                        val adapter = ChatAdapter(chatViewModel.oldChatList.value!!, newSortedChatList, chatViewModel,
                            object: ChatAdapter.OnChatClickListener {
                                override fun onChatClick(chat: Chat) {
                                    chatViewModel.setOldChatList(newSortedChatList)

                                    val action = MessagesFragmentDirections.actionMessagesFragmentToChatFragment(chat)
                                    findNavController().navigate(action)
                                }
                            },
                            object: ChatAdapter.OnChatUpdateListener {
                                override fun onChatUpdate(newMessage: Message) {
                                    CommonUtils().sendMessageNotification(context, newMessage)
                                }
                            }
                        )

                        chatsRecyclerView.adapter = adapter
                        chatsRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

                        loadingMessages(Constants.UI_DATA_SUCCESS)
                    } else {
                        loadingMessages(Constants.UI_DATA_FAILED)
                    }
                }
            })
        } else {
            loadingMessages(Constants.UI_DATA_FAILED)
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

            else -> {
                chatsRecyclerView.visibility = View.GONE
                chatLoadingProgressCl.visibility = View.GONE
                chatLoadErrorCard.visibility = View.GONE
            }
        }
    }
}
package com.example.sharidev2.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.data.repository.ChatRepository
import com.example.sharidev2.utility.FirebaseClient
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class ChatViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val chatRepository = ChatRepository()
    private val currentUser = FirebaseClient.firebaseAuth.currentUser

    // DATA KEY CONSTANT
    private var userChatListListener: ListenerRegistration? = null
    private var userChatListener: ListenerRegistration? = null
    private val CHAT_LIST_KEY = "chat_list"
    private val OLD_CHAT_LIST_KEY = "old_chat_list"
    private val ACTIVE_CHAT_KEY = "active_chat"
    private val OLD_CHAT_KEY = "old_chat"



    // INTERNAL DATA MEMBERS
    private val chatList: LiveData<List<Chat>> = savedStateHandle.getLiveData(CHAT_LIST_KEY, mutableListOf())
    private val oldChatList: LiveData<List<Chat>> = savedStateHandle.getLiveData(OLD_CHAT_LIST_KEY, mutableListOf())
    private val activeChat: LiveData<Chat> = savedStateHandle.getLiveData(ACTIVE_CHAT_KEY, Chat())
    private val oldChat: LiveData<Chat> = savedStateHandle.getLiveData(OLD_CHAT_KEY, Chat())


    init {
        viewModelScope.launch {
            val chatList = chatRepository.getChatList()

            if(chatList != null) {
                setChatList(chatList)
                setOldChatList(chatList)
            }
        }
    }


    // SETTER in SavedStateHandle
    fun setChatList(newChatList: List<Chat>) {
        savedStateHandle[CHAT_LIST_KEY] = newChatList
    }

    fun setOldChatList(newOldChatList: List<Chat>) {
        savedStateHandle[OLD_CHAT_LIST_KEY] = newOldChatList
    }

    fun setActiveChat(newActiveChat: Chat) {
        savedStateHandle[ACTIVE_CHAT_KEY] = newActiveChat
    }

    fun setOldChat(newOldChat: Chat) {
        savedStateHandle[OLD_CHAT_KEY] = newOldChat
    }


    fun startListeningForUserChatsUpdates(userUid: String, onChatsUpdate: (List<Chat>) -> Unit) {
        userChatListListener = chatRepository.listenForUserChatListUpdates(userUid, onChatsUpdate)
    }

    fun startListeningForChatUpdates(chatId: String, onChatUpdate: (Chat?) -> Unit) {
        userChatListener = chatRepository.listenForChatUpdates(chatId, onChatUpdate)
    }

    suspend fun addMessageToChat(chatId: String, message: Message): Int {
        setOldChat(activeChat.value!!)
        return chatRepository.addMessageToChat(chatId, message, activeChat.value!!)
    }

    fun getNewMessage(): Message? {
        activeChat.value!!.messages!!.let { activeMessages ->
            Log.e("ChatViewModel", "hasNewMessage-activeMessage size: " + activeMessages.size)
            oldChat.value!!.messages!!.let { oldMessages ->
                Log.e("ChatViewModel", "hasNewMessage-oldMessage size: " + activeMessages.size)
                for (message in activeMessages) {
                    if (!oldMessages.contains(message)) {
                        // Found a new message
                        if(currentUser?.uid != null) {
                            if(message.senderId != currentUser!!.uid) {
                                return message
                            }
                        }
                    }
                }
            }
        }
        // No new messages found
        return null
    }

    override fun onCleared() {
        userChatListListener?.remove()
    }
}
package com.example.sharidev2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.data.repository.ChatRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class ChatViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val chatRepository = ChatRepository()

    // DATA KEY CONSTANT
    private var userChatListListener: ListenerRegistration? = null
    private var userChatListener: ListenerRegistration? = null
    private val CHAT_LIST_KEY = "chat_list"
    private val ACTIVE_CHAT_KEY = "active_chat"



    // INTERNAL DATA MEMBERS
    private val chatList: LiveData<List<Chat>> = savedStateHandle.getLiveData(CHAT_LIST_KEY)
    private val activeChat: LiveData<Chat> = savedStateHandle.getLiveData(ACTIVE_CHAT_KEY)


    init {
        if(!chatList.isInitialized) {
            viewModelScope.launch {
                val chatList = chatRepository.getChatList()

                if(chatList != null) {
                    setChatList(chatList)
                }
            }
        }
    }


    // SETTER in SavedStateHandle
    fun setChatList(newChatList: List<Chat>) {
        savedStateHandle[CHAT_LIST_KEY] = newChatList
    }

    fun setActiveChat(newActiveChat: Chat) {
        savedStateHandle[ACTIVE_CHAT_KEY] = newActiveChat
    }


    fun startListeningForUserChatsUpdates(userUid: String, onChatsUpdate: (List<Chat>) -> Unit) {

        userChatListListener = chatRepository.listenForUserChatListUpdates(userUid, onChatsUpdate)
    }

    fun startListeningForChatUpdates(chatId: String, onChatUpdate: (Chat?) -> Unit) {
        userChatListener = chatRepository.listenForChatUpdates(chatId, onChatUpdate)
    }

    suspend fun addMessageToChat(chatId: String, message: Message): Int {
         return chatRepository.addMessageToChat(chatId, message, activeChat.value!!)
    }

    override fun onCleared() {
        userChatListListener?.remove()
    }
}
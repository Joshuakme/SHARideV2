package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.ChatStatus
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseClient
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatRepository {
    // Firebase Instances
    private val firestore = FirebaseClient.firestore

    private val converters = Converters()

    private val currentUser = FirebaseClient.firebaseAuth.currentUser
    private val chatCollectionRef = firestore.collection("chat")


    // CREATE
    suspend fun addMessageToChat(chatId: String, message: Message, oldChat: Chat): Int {
        return withContext(Dispatchers.IO) {
            try {
                if(currentUser != null) {
                    message.senderId = currentUser.uid
                    message.senderName = currentUser.displayName
                    message.photoUrl = currentUser.photoUrl

                    if(oldChat.messages != null) {
                        oldChat.messages!!.add(message)
                    }

                    val chatHashMap = converters.toChatHashMap(oldChat, message.text)


                    chatCollectionRef.document(chatId)
                        .update(chatHashMap)
                        .await()

                    Constants.FIREBASE_REQUEST_SUCCESS
                } else {
                    Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
                }
            } catch (e: Exception) {
                Log.e("Add Message To Chat", e.message.toString())
                Constants.FIREBASE_REQUEST_EXCEPTION
            }
        }
    }


    // RETRIEVE
    suspend fun getChatList(): List<Chat>? {
        val chatList = mutableListOf<Chat>()

        return withContext(Dispatchers.IO) {
            try {
                if(currentUser != null) {
                    val chatQuerySnapshot = chatCollectionRef
                        .whereArrayContains("members", currentUser.uid)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get()
                        .await()

                    for(document in chatQuerySnapshot.documents) {

                        val chat = converters.toChatFull(document)

                        chatList.add(chat)
                    }

                    chatList
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("Get Chat List", e.message.toString())
                null
            }
        }
    }

    fun listenForUserChatListUpdates(userUid: String, listener: (List<Chat>) -> Unit): ListenerRegistration {
        val userChatsRef = chatCollectionRef.whereArrayContains("members", userUid)

        return userChatsRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                // Handle error
                return@addSnapshotListener
            }

            val userChats: List<Chat> = snapshots?.documents?.mapNotNull { snapshot ->
                val chatData = snapshot.data

                if (chatData != null) {
                    converters.toChatFull(chatData)
                } else {
                    null
                }
            } ?: emptyList()

            listener(userChats)
        }
    }

    fun listenForChatUpdates(chatId: String, listener: (Chat?) -> Unit): ListenerRegistration {
        val userChatsRef = chatCollectionRef.document(chatId)

        return userChatsRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                // Handle error
                return@addSnapshotListener
            }


            if(snapshots != null) {
                val chatData = snapshots.data

                val chat = if (chatData != null) {
                    converters.toChatFull(chatData)
                } else {
                    null
                }

                listener(chat)
            }
        }
    }
}
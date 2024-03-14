package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.ChatStatus
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.UserClient
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
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

                    if(oldChat.messages != null) {
                        oldChat.messages!!.add(message)
                    }


                    val chatHashMap = hashMapOf(
                        "chatId" to oldChat.chatId,
                        "members" to oldChat.members,
                        "lastMessage" to message.text,
                        "messages" to oldChat.messages,
                        "rideId" to oldChat.rideId,
                        "chatStatus" to oldChat.chatStatus
                    )


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
                        .get()
                        .await()

                    for(document in chatQuerySnapshot.documents) {

                        val chat = Chat(
                            chatId = document.getString("chatId"),
                            members = document.get("members") as List<String>,
                            lastMessage = document.getString("lastMessage"),
                            timestamp = document.getTimestamp("timestamp"),
                            messages = converters.toMessageList(document.get("messages") as List<Map<String, Any>>).toMutableList(),
                            rideId = document.getString("rideId"),
                            chatStatus = ChatStatus.valueOf(document.getString("chatStatus")!!)
                        )

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

    fun listenForUserChatsUpdates(userUid: String, listener: (List<Chat>) -> Unit): ListenerRegistration {
        val userChatsRef = chatCollectionRef.whereArrayContains("members", userUid)

        return userChatsRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                // Handle error
                return@addSnapshotListener
            }

            val userChats: List<Chat> = snapshots?.documents?.mapNotNull { snapshot ->
                val chatData = snapshot.data

                if (chatData != null) {
                    Chat(
                        chatId = snapshot.id,
                        members = chatData["members"] as List<String>,
                        lastMessage = chatData["lastMessage"] as String,
                        timestamp = chatData["timestamp"] as Timestamp,
                        messages = converters.toMessageList(chatData["messages"] as List<Map<String, Any>>).toMutableList(),
                        rideId = chatData["rideId"] as String,
                        chatStatus = ChatStatus.valueOf(chatData["chatStatus"] as String)
                    )
                } else {
                    null
                }
            } ?: emptyList()

            listener(userChats)
        }
    }

}
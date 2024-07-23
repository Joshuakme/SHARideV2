package com.example.sharide.data.repository

import android.util.Log
import com.example.sharide.data.dto.NotificationBody
import com.example.sharide.data.dto.SendMessageDto
import com.example.sharide.data.model.Chat
import com.example.sharide.data.model.FirebaseMessagingApi
import com.example.sharide.data.model.Message
import com.example.sharide.utility.Constants
import com.example.sharide.utility.Converters
import com.example.sharide.utility.FirebaseClient
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.IOException


class ChatRepository {
    // Firebase Instances
    private val firestore = FirebaseClient.firestore

    private val converters = Converters()

    private val currentUser = FirebaseClient.firebaseAuth.currentUser
    private val chatCollectionRef = firestore.collection("chat")

    private val api: FirebaseMessagingApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create()


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

                    sendNotification(chatId, message, oldChat)

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


    private suspend fun sendNotification(chatId: String, message: Message, oldChat: Chat) {
        if(currentUser != null) {
           // val currentUserFcmToken = FirebaseClient.getUserFcmToken(currentUser.uid)
            val testDeviceFcmToken = "ctdivLU0Szi4V83mIOT2Qq:APA91bGV_Pe76swGxh2g3LN373lmtl5g9uRYr0hBIbRl6MV7mndVR1BqjaHsuUbF9QIMYNDiB_h1n_AwlD1XGtUpBnExLcS52kfkPYXXT7_1-17VIVs1TAQka78D00TvBaXnt586jgF1"

            val messageDto = SendMessageDto(
                to = testDeviceFcmToken,
                notification = NotificationBody(
                    title = "New message from ${currentUser.displayName}",
                    body = message.text
                )
            )

            try {
                api.sendMessage(messageDto)
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e("Chat Repository", e.message.toString())
            }


            //val chatMembersFcmTokens = oldChat.memberFcmTokens!!.filter { it != currentUserFcmToken }


//            try {
//                val jsonObject = JSONObject()
//
//                val notificationObj = JSONObject()
//                notificationObj.put("title", oldChat.chatTitle)
//                notificationObj.put("body", message)
//
//                val dataObj = JSONObject()
//                dataObj.put("chatId", chatId)   // For activity to intent to chat
//
//                jsonObject.put("notification", notificationObj)
//                jsonObject.put("data", dataObj)
////                jsonObject.put("to", chatMembersFcmTokens)
//                jsonObject.put("to", testDeviceFcmToken)
//                callApi(jsonObject)
//            } catch (e: Exception) {
//                Log.e("Chat Repository: sendNotification()", e.message.toString())
//            }
        }
    }

    private fun callApi(jsonObject: JSONObject) {
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()

        val fcmUrl = "https://fcm.googleapis.com/v1/projects/sharide-777b0/messages:send"

        val body = jsonObject.toString().toRequestBody(JSON)

        val apiKey = "AAAAKXhRkQM:APA91bEoSgCfObxmlnjVYuwbpUuhJwj1htZTk_oSSjhZDglLaoQiHaEbaPUSWqNqaXF_D4RLyTNMyLBJQDeeG5ZZ82BM18GyLDA3SrDQh16OMPrn9vxvP5zMT2qn8G_MrySscSeoEO-v"

        val request = Request.Builder()
            .url(fcmUrl)
            .post(body)
            .header("Authorization", "Bearer $apiKey")
            .build()

        client.newCall(request)
    }


}
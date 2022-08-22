package com.android.example.messenger.services

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.example.messenger.ui.chat.ChatActivity
import com.android.example.messenger.ui.signup.SignUpPresenter
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var presenter: SignUpPresenter
    private var TAG = "MyFirebaseMessagingService"
    private var broadcaster: LocalBroadcastManager? = null

    override fun onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //super.onMessageReceived(remoteMessage)

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            handleMessage(remoteMessage)
            //showNotification(remoteMessage.data["ID"]?.toInt() ?: 0)
        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(
                TAG, "Message Notification Body: " + remoteMessage.notification!!
                    .body
            )
            //showNotification(remoteMessage.data["ID"]?.toInt() ?: 0)
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

    }


    private fun handleMessage(remoteMessage: RemoteMessage) {
        val intent = Intent("MyData")
        intent.putExtra("id", remoteMessage.data["ID"]?.toLong());
        intent.putExtra("senderId", remoteMessage.data["SENDER_ID"]?.toLong());
        intent.putExtra("senderUsername", remoteMessage.data["SENDER_USERNAME"]);
        intent.putExtra("senderUrl", remoteMessage.data["SENDER_URL"]);
        intent.putExtra("senderLastOnline", remoteMessage.data["LAST_ONLINE"]);
        intent.putExtra("recipientId", remoteMessage.data["RECIPIENT_ID"]?.toLong());
        intent.putExtra("bodyMessage", remoteMessage.data["SENDER_MESSAGE"]);
        intent.putExtra("messageCreatedAt", remoteMessage.data["MESSAGE_TIME"]);
        intent.putExtra("conversationId", remoteMessage.data["CONVERSATION_ID"]?.toLong());
        intent.putExtra("unread", remoteMessage.data["UNREAD"]?.toLong());
        intent.putExtra("type", remoteMessage.data["TYPE"])
        intent.putExtra("url", remoteMessage.data["URL"]).toString()
        intent.putExtra("latitude", remoteMessage.data["LATITUDE"]).toString()
        intent.putExtra("longitude", remoteMessage.data["LONGITUDE"]).toString()

        broadcaster?.sendBroadcast(intent);

        navigateToChat(remoteMessage.data["SENDER_USERNAME"], remoteMessage.data["SENDER_ID"]?.toLong(), remoteMessage.data["SENDER_URL"], remoteMessage.data["LAST_ONLINE"], remoteMessage.data["CONVERSATION_ID"]?.toLong() )



    }


    private fun navigateToChat(
        recipientName: String?,
        recipientId: Long?,
        imgUrl: String?,
        lastOnline: String?,
        conversationId: Long?
    ) {
        val intent = Intent(baseContext, ChatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("RECIPIENT_ID", recipientId)
        intent.putExtra("RECIPIENT_NAME", recipientName)
        intent.putExtra("RECIPIENT_URl", imgUrl)
        intent.putExtra("RECIPIENT_LAST_ONLINE", lastOnline)
        intent.putExtra("CONVERSATION_ID", conversationId)
        handleIntent(intent)
    }


}
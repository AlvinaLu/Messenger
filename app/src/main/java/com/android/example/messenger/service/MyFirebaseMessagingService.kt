package com.android.example.messenger.service

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.example.messenger.ui.chat.ChatActivity
import com.android.example.messenger.ui.signup.SignUpPresenter
import com.android.example.messenger.ui.signup.SignUpPresenterImpl
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

        Log.d(TAG, "From: " + remoteMessage.from)


        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            handleMessage1(remoteMessage)
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            if ( /* Check if data needs to be processed by long running job */true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //  scheduleJob()
            } else {
                // Handle message within 10 seconds
                //handleNow()
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(
                TAG, "Message Notification Body: " + remoteMessage.notification!!
                    .body
            )
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun handleMessage1(remoteMessage: RemoteMessage) {
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
        broadcaster?.sendBroadcast(intent);
    }

    private fun handleMessage(remoteMessage: RemoteMessage) {
        val handler = Handler(Looper.getMainLooper())
        remoteMessage.notification?.let {
            val intent = Intent("MyData")
            intent.putExtra("message", it.body);
            broadcaster?.sendBroadcast(intent);
        }
        val id: Long = remoteMessage.data["SENDER_ID"]?.toLong() ?: -1L
        val username: String = remoteMessage.data["SENDER_USERNAME"].toString()
        val message: String = remoteMessage.data["SENDER_MESSAGE"].toString()
        val url: String = remoteMessage.data["SENDER_URL"].toString()
        val lastOnline: String = remoteMessage.data["LAST_ONLINE"].toString()
        handler.post(Runnable { navigateToChat(username, id, url, lastOnline, null) })
    }

    private fun navigateToChat(
        recipientName: String,
        recipientId: Long,
        imgUrl: String,
        lastOnline: String,
        conversationId: Long?
    ) {
        val intent = Intent(baseContext, ChatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("RECIPIENT_ID", recipientId)
        intent.putExtra("RECIPIENT_NAME", recipientName)
        intent.putExtra("RECIPIENT_URl", imgUrl)
        intent.putExtra("RECIPIENT_LAST_ONLINE", lastOnline)

        baseContext.startActivity(intent)
    }


}
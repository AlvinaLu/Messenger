package com.android.example.messenger.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class CheckOnlineService() : Service() {

    var onlineInteraction: OnlineInteraction? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        onlineInteraction?.stopOnline()
        onlineInteraction = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val token = intent?.getStringExtra("token")
        if (token!=null){
            onlineInteraction = OnlineInteraction(this, token )
            onlineInteraction?.startOnline()
        }

        return START_STICKY
    }
}
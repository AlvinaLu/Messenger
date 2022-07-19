package com.android.example.messenger

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.android.example.messenger.service.OnlineInteraction

class CheckOnlineService(context: Context) : Service() {

    val onlineInteraction = OnlineInteraction(context)

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        onlineInteraction.stopOnline()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        onlineInteraction.startOnline()
        return START_STICKY
    }
}
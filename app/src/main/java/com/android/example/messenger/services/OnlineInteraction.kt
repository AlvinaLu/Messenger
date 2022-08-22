package com.android.example.messenger.services

import android.content.Context
import android.util.Log
import com.android.example.messenger.data.AppWebApi
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.scheduleAtFixedRate

class OnlineInteraction(val context: Context, val token: String) {

    private val service: AppWebApi = AppWebApi.getApiService()
    private val timer = Timer()


    fun startOnline() {
        timer.scheduleAtFixedRate(0, TimeUnit.MINUTES.toMillis(1)) {
            Log.w("Online", "Tick-tack")
            service.updateUserOnline(token).get()
        }
    }

    fun stopOnline() {
        timer.cancel()
    }

}
package com.android.example.messenger.service

import android.content.Context
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.service.MessengerApiService
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class OnlineInteraction(context: Context) {

    private val preferences: AppPreferences = AppPreferences.create(context)
    private val service: MessengerApiService = MessengerApiService.getInstance()
    val timer = Timer()

    fun startOnline() {
        timer.schedule(TimeUnit.MINUTES.toMillis(1)) { service.updateUserOnline(preferences.accessToken as String) }
    }

    fun stopOnline() {
        timer.cancel()
    }

}
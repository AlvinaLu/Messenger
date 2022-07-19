package com.android.example.messenger.ui.signup

import com.android.example.messenger.data.local.AppPreferences

interface SignUpPresenter {
    var preferences: AppPreferences

    fun executeSignUp(username: String, phoneNumber: String, password: String)


    fun putNotificationToken(token: String?)

}
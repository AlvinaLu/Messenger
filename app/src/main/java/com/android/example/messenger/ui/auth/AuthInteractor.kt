package com.android.example.messenger.ui.auth

import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.vo.UserVO

interface AuthInteractor {

    var userDetails: UserVO
    var accessToken: String
    var submittedUsername: String
    var submittedPassword: String

    interface onAuthFinishedListener {
        fun onAuthSuccess()

        fun putNotificationTokenSuccess()

        fun putNotificationTokenError()

        fun onAuthError()

        fun onUsernameError()

        fun onPasswordError()
    }

    fun persistAccessToken(preferences: AppPreferences)

    fun persistUserDetails(preferences: AppPreferences)


    fun putNotificationToken(token: String, listener: onAuthFinishedListener)
}
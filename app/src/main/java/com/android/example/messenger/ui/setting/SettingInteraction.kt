package com.android.example.messenger.ui.setting

import com.android.example.messenger.data.response.UserVO

interface SettingInteraction {

    interface OnUrlSendFinishedListener {
        fun onSendSuccess(message: UserVO)

        fun onSendError()
    }

    interface OnUrlGetFinishedListener {
        fun onGetSuccess(userVO: UserVO)

        fun onGetError()
    }

    fun sendUrl(url: String, listener: OnUrlSendFinishedListener)


}
package com.android.example.messenger.ui.setting

import com.android.example.messenger.data.vo.ConversationVO
import com.android.example.messenger.data.vo.MessageVO
import com.android.example.messenger.data.vo.UserVO

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

    fun getUrl(userId: Long)

}
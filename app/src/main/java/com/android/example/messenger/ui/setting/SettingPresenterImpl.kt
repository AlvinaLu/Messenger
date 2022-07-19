package com.android.example.messenger.ui.setting

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.ImageView
import com.android.example.messenger.data.vo.UserVO
import com.android.example.messenger.ui.chat.ChatInteractionImpl
import com.android.example.messenger.ui.chat.ChatInteractor
import com.android.example.messenger.ui.chat.ChatView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.File
import java.io.FileOutputStream

class SettingPresenterImpl(val view: SettingView): SettingPresenter,
    SettingInteraction.OnUrlSendFinishedListener,
    SettingInteraction.OnUrlGetFinishedListener {

    private val interaction: SettingInteraction = SettingInteractionImpl(view.getContext())

    override fun sendUrl(url: String) {
        interaction.sendUrl(url, this)
    }

    override fun getUrl() {
        TODO("Not yet implemented")
    }

    override fun onSendSuccess(message: UserVO) {
        view.saveImgSuccess()
    }

    override fun onSendError() {
        view.showSaveImgError()
    }

    override fun onGetSuccess(userVO: UserVO) {
        TODO("Not yet implemented")
    }

    override fun onGetError() {
        TODO("Not yet implemented")
    }


}
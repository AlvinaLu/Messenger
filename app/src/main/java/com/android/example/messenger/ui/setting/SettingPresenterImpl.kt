package com.android.example.messenger.ui.setting

import com.android.example.messenger.data.response.UserVO

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
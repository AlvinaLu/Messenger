package com.android.example.messenger.ui.setting

import android.content.Context
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.remote.request.UrlUpdateRequestObject
import com.android.example.messenger.service.MessengerApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SettingInteractionImpl(context: Context): SettingInteraction {

    private val service: MessengerApiService = MessengerApiService.getInstance()
    private val preferences: AppPreferences = AppPreferences.create(context)

    override fun sendUrl(url: String, listener: SettingInteraction.OnUrlSendFinishedListener) {
        if(url!=null){
            val requestObject = UrlUpdateRequestObject(url)
            service.updateUserUrl(requestObject,  preferences.accessToken as String)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
                    preferences.storeUserDetails(res)
                    listener.onSendSuccess(res)
                           },
                    { error ->
                        error.printStackTrace()})
        }
        }

    override fun getUrl(userId: Long) {
        TODO("Not yet implemented")
    }


}
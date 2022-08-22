package com.android.example.messenger.ui.login

import com.android.example.messenger.data.AppWebApi
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.remote.request.LoginRequestObject
import com.android.example.messenger.data.remote.request.TokenUpdateRequestObject
import com.android.example.messenger.data.response.UserVO
import com.android.example.messenger.ui.auth.AuthInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class LoginInteractorImpl : LoginInteractor {

    override lateinit var userDetails: UserVO
    override lateinit var accessToken: String
    override lateinit var submittedUsername: String
    override lateinit var submittedPassword: String

    private val appWebApi: AppWebApi = AppWebApi.getApiService()

    override fun login(username: String, password: String, listener: AuthInteractor.onAuthFinishedListener) {
        when {

            username.isBlank() -> listener.onUsernameError()

            password.isBlank() -> listener.onPasswordError()
            else -> {

                val loginRequestObject = LoginRequestObject(username, password)

                appWebApi.login(loginRequestObject)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe( { res ->
                        accessToken = res.headers()["Authorization"] as String
                        listener.onAuthSuccess()

                    }, { error ->
                        listener.onAuthError()
                        error.printStackTrace()
                    })
            }
        }
    }


    override fun retrieveDetails(preferences: AppPreferences,
                                 listener: LoginInteractor.OnDetailsRetrievalFinishedListener) {

        appWebApi.echoDetails(preferences.accessToken as String)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                userDetails = res
                listener.onDetailsRetrievalSuccess()},
                { error ->
                    listener.onDetailsRetrievalError()
                    error.printStackTrace()})
    }

    override fun persistAccessToken(preferences: AppPreferences) {
        preferences.storeAccessToken(accessToken)
    }

    override fun persistUserDetails(preferences: AppPreferences) {
        preferences.storeUserDetails(userDetails)
    }

    override fun putNotificationToken(token: String, listener: AuthInteractor.onAuthFinishedListener) {
        if(token!=null){
            val requestObject = TokenUpdateRequestObject(token)
            appWebApi.updateUserToken(requestObject,  accessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
                    userDetails = res
                    listener.putNotificationTokenSuccess()
                },
                    { error ->
                        listener.putNotificationTokenError()
                        error.printStackTrace()})
        }
    }
}
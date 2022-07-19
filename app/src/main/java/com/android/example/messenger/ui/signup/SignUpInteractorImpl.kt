package com.android.example.messenger.ui.signup

import android.text.TextUtils
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.remote.request.LoginRequestObject
import com.android.example.messenger.data.remote.request.TokenUpdateRequestObject
import com.android.example.messenger.data.remote.request.UserRequestObject
import com.android.example.messenger.data.vo.UserVO
import com.android.example.messenger.service.MessengerApiService
import com.android.example.messenger.ui.auth.AuthInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SignUpInteractorImpl : SignUpInteractor {

    override lateinit var userDetails: UserVO
    override lateinit var accessToken: String
    override lateinit var submittedUsername: String
    override lateinit var submittedPassword: String

    private val service: MessengerApiService = MessengerApiService.getInstance()

    override fun signUp(username: String, phoneNumber: String, password: String,
                        listener: SignUpInteractor.OnSignUpFinishedListener) {
        submittedUsername = username
        submittedPassword = password
        val userRequestObject = UserRequestObject(username, password, phoneNumber)

        when {
            TextUtils.isEmpty(username) -> listener.onUsernameError()
            TextUtils.isEmpty(phoneNumber) -> listener.onPhoneNumberError()
            TextUtils.isEmpty(password) -> listener.onPasswordError()
            else -> {
                /*
                 * Registering a new user to the Messenger platform
                 * with the MessengerApiService
                 */
                service.createUser(userRequestObject)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ res ->
                        userDetails = res
                        listener.onSuccess()
                    }, { error ->
                        listener.onError()
                        error.printStackTrace()
                    })
            }
        }
    }

    override fun getAuthorization(listener: AuthInteractor.onAuthFinishedListener) {
        val userRequestObject = LoginRequestObject(submittedUsername, submittedPassword)

        /*
         * Log the registered user in to the platform
         * with the MessengerApiService
         */
        service.login(userRequestObject)
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


    override fun persistAccessToken(preferences: AppPreferences) {
        preferences.storeAccessToken(accessToken)
    }

    override fun persistUserDetails(preferences: AppPreferences) {
        preferences.storeUserDetails(userDetails)
    }

    override fun putNotificationToken(token: String, listener: AuthInteractor.onAuthFinishedListener) {

        if(token!=null){
            val requestObject = TokenUpdateRequestObject(token)
            service.updateUserToken(requestObject,  accessToken)
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
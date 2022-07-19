package com.android.example.messenger.ui.login

import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.remote.request.LoginRequestObject
import com.android.example.messenger.data.remote.request.TokenUpdateRequestObject
import com.android.example.messenger.data.vo.UserVO
import com.android.example.messenger.service.MessengerApiService
import com.android.example.messenger.ui.auth.AuthInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class LoginInteractorImpl : LoginInteractor {

    override lateinit var userDetails: UserVO
    override lateinit var accessToken: String
    override lateinit var submittedUsername: String
    override lateinit var submittedPassword: String

    private val service: MessengerApiService = MessengerApiService.getInstance()

    override fun login(username: String, password: String, listener: AuthInteractor.onAuthFinishedListener) {
        when {
            /*
             * If an empty username is submitted in the login form, the username is invalid.
             * The listener's onUsernameError() function is called when this happens.
             */
            username.isBlank() -> listener.onUsernameError()

            /*
             * Call the listener's onPasswordError() function when an empty
             * password is submitted.
             */
            password.isBlank() -> listener.onPasswordError()
            else -> {

                /*
                 * Initializing model's submittedUsername and submittedPassword
                 * fields and creating appropriate LoginRequestObject.
                 */
                submittedUsername = username
                submittedPassword = password
                val requestObject = LoginRequestObject(username, password)

                /*
                 * Using MessengerApiService to send a login request to Messenger API.
                 */
                service.login(requestObject)
                    .subscribeOn(Schedulers.io())   // subscribing Observable to Scheduler thread
                    .observeOn(AndroidSchedulers.mainThread())  // setting observation to be done on the main thread
                    .subscribe({ res ->
                        if (res.code() != 403) {
                            accessToken = res.headers()["Authorization"] as String
                            listener.onAuthSuccess()
                        } else {
                            /*
                             * Branched reached when an HTTP 403 (forbidden) status code
                             * is returned by the server. This indicates that the login
                             * failed and the user is not authorized to access the server.
                             */
                            listener.onAuthError()
                        }
                    }, { error ->
                        listener.onAuthError()
                        error.printStackTrace()
                    })
            }
        }
    }

    override fun retrieveDetails(preferences: AppPreferences,
                                 listener: LoginInteractor.OnDetailsRetrievalFinishedListener) {
        /*
         * Retrieves details of user upon initial login
         */
        service.echoDetails(preferences.accessToken as String)
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
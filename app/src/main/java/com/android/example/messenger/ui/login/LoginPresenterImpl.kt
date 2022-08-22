package com.android.example.messenger.ui.login

import android.content.ContentValues
import android.util.Log
import android.widget.Toast
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.ui.auth.AuthInteractor
import com.android.example.messenger.utils.SecureUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class LoginPresenterImpl(private val view: LoginView) : LoginPresenter,
    AuthInteractor.onAuthFinishedListener, LoginInteractor.OnDetailsRetrievalFinishedListener {

    private val interactor: LoginInteractor = LoginInteractorImpl()
    private val preferences: AppPreferences = AppPreferences.create(view.getContext())

    override fun onPasswordError() {
        view.hideProgress()
        view.setPasswordError()
    }

    override fun onUsernameError() {
        view.hideProgress()
        view.setUsernameError()
    }

    override fun onAuthSuccess() {
        interactor.persistAccessToken(preferences)
        interactor.retrieveDetails(preferences, this)
        putNotificationToken(null)
    }



    override fun onDetailsRetrievalSuccess() {
        interactor.persistUserDetails(preferences)
        view.hideProgress()
        view.navigateToHome()
    }

    override fun onDetailsRetrievalError() {
        interactor.retrieveDetails(preferences, this)
    }

    /**
     * Called by a LoginView to request start of login process
     */
    override fun executeLogin(username: String, password: String) {
        view.showProgress()
        val hashPassword = SecureUtils.getSecurePassword(password)
        if (hashPassword != null) {
            interactor.login(username, hashPassword, this)
        }
    }

    override fun putNotificationToken(token: String?) {
        if(token==null){
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if(!task.isSuccessful){
                    Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                val token = task.result
                Toast.makeText(view.getContext(), token, Toast.LENGTH_LONG).show()
                interactor.putNotificationToken(token.toString(), this)
            })
        }
        else{
            interactor.putNotificationToken(token.toString(), this)
        }
    }

    override fun putNotificationTokenSuccess() {
        interactor.persistUserDetails(preferences)
        Toast.makeText(view.getContext(), "Notification's success", Toast.LENGTH_SHORT).show()
    }

    override fun putNotificationTokenError() {
        Toast.makeText(view.getContext(), "Token error", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthError() {
        view.showAuthError()
        view.hideProgress()
    }
}
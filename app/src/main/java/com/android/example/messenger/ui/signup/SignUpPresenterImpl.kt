package com.android.example.messenger.ui.signup

import android.content.ContentValues.TAG
import android.util.JsonToken
import android.util.Log
import android.widget.Toast
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.ui.auth.AuthInteractor
import com.android.example.messenger.utils.SecureUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class SignUpPresenterImpl(private val view: SignUpView): SignUpPresenter,
    SignUpInteractor.OnSignUpFinishedListener, AuthInteractor.onAuthFinishedListener {

    private val interactor: SignUpInteractor = SignUpInteractorImpl()
    override var preferences: AppPreferences = AppPreferences.create(view.getContext())

    override fun onSuccess() {
        interactor.getAuthorization(this)
    }

    override fun onError() {
        view.hideProgress()
        view.showSignUpError()
    }

    override fun onUsernameError() {
        view.hideProgress()
        view.setUsernameError()
    }

    override fun onPasswordError() {
        view.hideProgress()
        view.setPasswordError()
    }

    override fun onPhoneNumberError() {
        view.hideProgress()
        view.setPhoneNumberError()
    }

    override fun executeSignUp(username: String, phoneNumber: String, password: String) {
        view.showProgress()
        if(username.length > 20){
            view.hideProgress()
            view.setUsernameTooLongError()
        }else{
            val hashPassword = SecureUtils.getSecurePassword(password)
            if (hashPassword != null) {
                interactor.signUp(username, phoneNumber, hashPassword, this)
            }

        }
    }



    override fun onAuthSuccess() {
        interactor.persistAccessToken(preferences)
        interactor.persistUserDetails(preferences)
        putNotificationToken(null)
        view.hideProgress()
        view.navigateToHome()
    }

    override fun onAuthError() {
        view.hideProgress()
        view.showAuthError()
    }

    override fun putNotificationToken(token: String?) {
        if(token==null){
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if(!task.isSuccessful){
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                val token = task.result
                Toast.makeText(view.getContext(), token, Toast.LENGTH_LONG).show()
                interactor.putNotificationToken(token.toString(), this)
                Log.w("TOKEN", token)
            })
        }
        else{
            interactor.putNotificationToken(token.toString(), this)
        }

    }

    override fun putNotificationTokenSuccess() {
        interactor.persistUserDetails(preferences)
        Toast.makeText(view.getContext(), "token success", Toast.LENGTH_SHORT).show()
    }

    override fun putNotificationTokenError() {
        Toast.makeText(view.getContext(), "Token error", Toast.LENGTH_SHORT).show()
    }


}
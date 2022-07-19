package com.android.example.messenger.ui.main

import android.content.Context
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.remote.request.repository.ConversationsRepository
import com.android.example.messenger.data.remote.request.repository.ConversationsRepositoryImpl
import com.android.example.messenger.data.remote.request.repository.UserRepository
import com.android.example.messenger.data.remote.request.repository.UserRepositoryImpl
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MainInteractorImpl(val context: Context) : MainInteractor {

    private val userRepository: UserRepository = UserRepositoryImpl(context, )
    private val conversationsRepository: ConversationsRepository = ConversationsRepositoryImpl(context)

    override fun loadContacts(listener: MainInteractor.OnContactsLoadFinishedListener) {

        userRepository.all()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                listener.onContactsLoadSuccess(res) },
                { error ->
                    listener.onContactsLoadError()
                    error.printStackTrace()})
    }

    override fun loadConversations(listener: MainInteractor.OnConversationsLoadFinishedListener) {
        /*
         * Retrieve all conversations of the currently logged in user
         * from the Messenger API.
         */
        conversationsRepository.all()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res -> listener.onConversationsLoadSuccess(res) },
                { error ->
                    listener.onConversationsLoadError()
                    error.printStackTrace()})
    }

    override fun logout(listener: MainInteractor.OnLogoutFinishedListener) {
        /*
         * Clear all locally stores data and call listener's
         * onLogoutSuccess() callback
         */
        val preferences: AppPreferences = AppPreferences.create(context)
        preferences.clear()
        listener.onLogoutSuccess()
    }
}

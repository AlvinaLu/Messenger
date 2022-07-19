package com.android.example.messenger.ui.chat

import android.content.Context
import android.util.Log
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.remote.request.MessageRequestObject
import com.android.example.messenger.data.remote.request.repository.ConversationsRepository
import com.android.example.messenger.data.remote.request.repository.ConversationsRepositoryImpl
import com.android.example.messenger.service.MessengerApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers

class ChatInteractionImpl(context: Context) : ChatInteractor {

    private val preferences: AppPreferences = AppPreferences.create(context)
    private val service: MessengerApiService = MessengerApiService.getInstance()
    private val conversationsRepository: ConversationsRepository = ConversationsRepositoryImpl(context)

    /**
     * Called to load the messages of a conversation thread
     * @param conversationId the unique id of the conversation opened
     * @param listener instance of the type [ChatInteractor.OnMessageLoadFinishedListener]
     */
    override fun loadMessages(conversationId: Long, listener: ChatInteractor.OnMessageLoadFinishedListener) {
        conversationsRepository.findConversationById(conversationId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res -> listener.onLoadSuccess(res)
                Log.d("load", "LoadMessage: " + res)},
                { error ->
                    listener.onLoadError()
                    Log.d("load", "loadUnsuccessful ")
                    error.printStackTrace()})
    }

    override fun loadAllMessages(listener: ChatInteractor.OnMessagesLoadFinishedListener) {
        conversationsRepository.all()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res -> listener.onLoadMessagesSuccess(res) },
                { error ->
                    listener.onLoadMessagesError()
                    error.printStackTrace()
                })
    }


    /**
     * Called to send a message to a user
     * @param recipientId unique id of the message recipient
     * @param listener instance of the type [ChatInteractor.OnMessageSendFinishedListener]
     */
    override fun sendMessage(recipientId: Long, message: String,
                             listener: ChatInteractor.OnMessageSendFinishedListener) {
        service.createMessage(MessageRequestObject(recipientId, message), preferences.accessToken as String)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res -> listener.onSendSuccess(res)},
                { error ->
                    listener.onSendError()
                    error.printStackTrace()})
    }
}
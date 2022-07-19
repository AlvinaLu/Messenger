package com.android.example.messenger.data.remote.request.repository

import android.content.Context
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.vo.ConversationListVO
import com.android.example.messenger.data.vo.ConversationVO
import com.android.example.messenger.service.MessengerApiService
import io.reactivex.Observable

class ConversationsRepositoryImpl(ctx: Context) : ConversationsRepository {

    private val preferences: AppPreferences = AppPreferences.create(ctx)
    private val service: MessengerApiService = MessengerApiService.getInstance()

    override fun findConversationById(id: Long): Observable<ConversationVO> {
        return service.showConversation(id, preferences.accessToken as String)
    }

    override fun all(): Observable<ConversationListVO> {
        return service.listConversations(preferences.accessToken as String)
    }
}
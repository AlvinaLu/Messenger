package com.android.example.messenger.data.remote.request.repository

import com.android.example.messenger.data.vo.ConversationListVO
import com.android.example.messenger.data.vo.ConversationVO
import io.reactivex.Observable

interface ConversationsRepository {
    fun findConversationById(id: Long): Observable<ConversationVO>
    fun all(): Observable<ConversationListVO>
}
package com.android.example.messenger.data.remote.request.repository

import android.content.Context
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.vo.UserListVO
import com.android.example.messenger.data.vo.UserVO
import com.android.example.messenger.service.MessengerApiService
import io.reactivex.Observable

class UserRepositoryImpl(ctx: Context) : UserRepository {

    private val preferences: AppPreferences = AppPreferences.create(ctx)
    private val service: MessengerApiService = MessengerApiService.getInstance()

    override fun findById(id: Long): Observable<UserVO> {
        return service.showUser(id, preferences.accessToken as String)
    }

    override fun all(): Observable<UserListVO> {
        return service.listUsers(preferences.accessToken as String)
    }

    override fun echoDetails(): Observable<UserVO> {
        return service.echoDetails(preferences.accessToken as String)
    }
}
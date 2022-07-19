package com.android.example.messenger.data.remote.request.repository

import com.android.example.messenger.data.vo.UserListVO
import com.android.example.messenger.data.vo.UserVO
import io.reactivex.Observable

interface UserRepository {

    fun findById(id: Long): Observable<UserVO>

    fun all(): Observable<UserListVO>

    fun echoDetails(): Observable<UserVO>
}
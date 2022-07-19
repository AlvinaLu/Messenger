package com.android.example.messenger.ui.experimental

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.vo.UserListVO
import com.android.example.messenger.data.vo.UserVO
import com.android.example.messenger.service.MessengerApiService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.geojson.Point
import java.io.IOException

class ExperimentalRepository(
    private val contactsDao: ContactsDao,
    private val appWebApi: AppWebApi,
    private val context: Context
) {
    val allContacts: Flow<List<ContactsModel>> = contactsDao.getContactsFlow()
    private val preferences: AppPreferences = AppPreferences.create(context)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(vararg contact: ContactsModel) {
        contactsDao.insertContact(*contact)
    }

    @WorkerThread
    suspend fun insertOrUpdate(vararg contacts: ContactsModel) {
        val currentIds = contactsDao.getContacts().map {
            it.id
        }
        contacts.forEach {
            if (currentIds.contains(it.id)) {
                contactsDao.updateContact(it)
            } else {
                contactsDao.insertContact(it)
            }
        }
    }


    suspend fun fetchAll(): Boolean {
        try {
            val response = appWebApi.listUsers(preferences.accessToken as String).get()

            val contacts = response
                ?.users
                ?.map {
                    ContactsModel(
                        id = it.id,
                        username = it.username,
                        phoneNumber = it.phoneNumber,
                        status = it.status,
                        createdAt = it.createdAt,
                        imgUrl = it.imgUrl,
                        lastOnline = it.lastOnline,
                        notificationToken = it.notificationToken
                    )
                }

            contacts?.toTypedArray()?.let { insertOrUpdate(*it) }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

}
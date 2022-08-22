package com.android.example.messenger.data.db

import android.content.Context
import androidx.annotation.WorkerThread
import com.android.example.messenger.data.AppWebApi
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.remote.request.TokenUpdateRequestObject
import com.android.example.messenger.models.ContactsModel
import kotlinx.coroutines.flow.Flow

class ContactsRepository(
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

    @WorkerThread
    suspend fun clearAllData() {
        contactsDao.deleteAll()
    }

    fun logOut() {
        try {
            appWebApi.updateUserToken(TokenUpdateRequestObject("none"), preferences.accessToken as String)
        } catch (e: Exception) {
            e.printStackTrace()
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
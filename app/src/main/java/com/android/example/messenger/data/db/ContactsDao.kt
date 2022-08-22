package com.android.example.messenger.data.db

import androidx.room.*
import com.android.example.messenger.models.ContactsModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {

    @Query("SELECT * FROM contact ORDER BY id ASC")
    fun getContactsFlow(): Flow<List<ContactsModel>>

    @Query("SELECT * FROM contact ORDER BY id ASC")
    suspend fun getContacts(): List<ContactsModel>

    @Query("SELECT * FROM contact  WHERE id = :id")
    fun getContactsId(id: Long): ContactsModel?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContact(vararg contact: ContactsModel)

    @Update
    suspend fun updateContact(vararg contact: ContactsModel)

    @Query("DELETE FROM contact")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(item: ContactsModel)

}

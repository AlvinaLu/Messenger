package com.android.example.messenger.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.example.messenger.data.db.CategoryRepository
import com.android.example.messenger.data.db.ContactsRepository
import com.android.example.messenger.data.db.ConversationRepository
import com.android.example.messenger.data.db.MessagesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val conversationRepository: ConversationRepository,
    private val contactRepository: ContactsRepository,
    private val messageRepository: MessagesRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    fun logOut(){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                contactRepository.clearAllData()
                conversationRepository.clearAllData()
                messageRepository.clearAllData()
                categoryRepository.clearAllData()
            }
            withContext(Dispatchers.IO){
                contactRepository.logOut()
            }
        }

    }

    class MainViewModelFactory(
        private val conversationRepository: ConversationRepository,
        private val contactRepository: ContactsRepository,
        private val messagesRepository: MessagesRepository,
        private val categoryRepository: CategoryRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(conversationRepository, contactRepository, messagesRepository, categoryRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
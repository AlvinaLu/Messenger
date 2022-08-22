package com.android.example.messenger.ui.main

import androidx.lifecycle.*
import com.android.example.messenger.data.db.CategoryRepository
import com.android.example.messenger.data.db.ConversationRepository
import com.android.example.messenger.data.db.MessagesRepository
import com.android.example.messenger.models.CategoryModel
import com.android.example.messenger.models.ConversationModel
import com.android.example.messenger.models.MessageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConversationsViewModel(
    private val repository: ConversationRepository,
    private val messagesRepository: MessagesRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val observer = Observer<List<ConversationModel>> {
        listLoadedMutableLiveData.value = if (it.isEmpty()) {
            Status.EMPTY
        } else {
            Status.NOT_EMPTY
        }
    }

    private val listLoadedMutableLiveData: MutableLiveData<Status> by lazy {
        MutableLiveData<Status>().also { it.value = Status.NOT_LOADED }
    }

    val listLoadedLiveData: LiveData<Status> = listLoadedMutableLiveData

    var liveData: LiveData<List<ConversationModel>> = repository.allConversations.asLiveData()

    fun insert(vararg conversation: ConversationModel) = viewModelScope.launch {
        repository.insert(*conversation)
    }

    var categoriesLiveData: LiveData<List<CategoryModel>> =
        categoryRepository.allCategories.asLiveData()

    private val fetchFromWebStatusMutableLiveData: MutableLiveData<Status> by lazy {
        MutableLiveData<Status>().also { it.value = Status.NOT_LOADED }
    }
    val fetchFromWebStatusLiveData: MutableLiveData<Status> = fetchFromWebStatusMutableLiveData

    fun addCategory(name: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { categoryRepository.insertOrUpdate(name) }
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { categoryRepository.delete(name) }
            val conversations =
                withContext(Dispatchers.IO) { repository.getConversationByCategory(name) }
            conversations.forEach {
                val newConversation = withContext(Dispatchers.IO) {
                   it.conversationId.let { it2->
                        ConversationModel(
                            conversationId = it2,
                            secondPartyUsername = it.secondPartyUsername,
                            imgUrl = it.imgUrl,
                            lastOnline = it.lastOnline,
                            unread = it.unread,
                            lastMessage = it.lastMessage,
                            lastMessageTime = it.lastMessageTime,
                            recipientId = it.recipientId,
                            pinned = it.pinned,
                            category = null
                        )
                    }
                }
                withContext(Dispatchers.IO) { repository.insertOrUpdate(newConversation) }
            }



        }
    }

    fun addMessage(message: MessageModel) {
        viewModelScope.launch {
            messagesRepository.insert(message)
            val conversationModel =
                withContext(Dispatchers.IO) { repository.getConversationById(message.conversationId) }
            if (conversationModel == null) {
                fetchFromWeb()
            }
            val newConversation = withContext(Dispatchers.IO) {
                conversationModel?.conversationId?.let {
                    ConversationModel(
                        conversationId = it,
                        secondPartyUsername = conversationModel.secondPartyUsername,
                        imgUrl = conversationModel.imgUrl,
                        lastOnline = conversationModel.lastOnline,
                        unread = conversationModel.unread + 1,
                        lastMessage = message.body.toString(),
                        lastMessageTime = message.createdAt,
                        recipientId = message.unread,
                        pinned = conversationModel.pinned,
                        category = conversationModel.category
                    )
                }
            }
            if (newConversation != null) {
                withContext(Dispatchers.IO) { repository.insertOrUpdate(newConversation) }
            }
        }

    }

    fun changeItemUnreadCount(conversationModel: ConversationModel) {
        viewModelScope.launch {
            val newConversation = withContext(Dispatchers.IO) {
                ConversationModel(
                    conversationId = conversationModel.conversationId,
                    secondPartyUsername = conversationModel.secondPartyUsername,
                    imgUrl = conversationModel.imgUrl,
                    lastOnline = conversationModel.lastOnline,
                    unread = 0,
                    lastMessage = conversationModel.lastMessage,
                    lastMessageTime = conversationModel.lastMessageTime,
                    recipientId = conversationModel.recipientId,
                    pinned = conversationModel.pinned,
                    category = conversationModel.category
                )
            }
            withContext(Dispatchers.IO) { repository.insertOrUpdate(newConversation) }
        }
    }

    fun pinConversationItem(conversationModel: ConversationModel) {
        viewModelScope.launch {
            val newConversation = withContext(Dispatchers.IO) {
                ConversationModel(
                    conversationId = conversationModel.conversationId,
                    secondPartyUsername = conversationModel.secondPartyUsername,
                    imgUrl = conversationModel.imgUrl,
                    lastOnline = conversationModel.lastOnline,
                    unread = 0,
                    lastMessage = conversationModel.lastMessage,
                    lastMessageTime = conversationModel.lastMessageTime,
                    recipientId = conversationModel.recipientId,
                    pinned = !conversationModel.pinned,
                    category = conversationModel.category
                )
            }
            withContext(Dispatchers.IO) { repository.insertOrUpdate(newConversation) }
        }
    }

    fun fetchFromWeb() {
        viewModelScope.launch {
            fetchFromWebStatusMutableLiveData.value = Status.NOT_LOADED
            val result = withContext(Dispatchers.IO) {
                repository.fetchAll()
            }
            if (result) {
                fetchFromWebStatusLiveData.value = Status.NOT_EMPTY
            }
        }
    }

    init {
        liveData.observeForever(observer)
    }

    override fun onCleared() {
        liveData.removeObserver(observer)
        super.onCleared()
    }
}

class ConversationViewModelFactory(
    private val repository: ConversationRepository,
    private val messagesRepository: MessagesRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConversationsViewModel(repository, messagesRepository, categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

enum class Status {
    NOT_LOADED,
    EMPTY,
    NOT_EMPTY
}
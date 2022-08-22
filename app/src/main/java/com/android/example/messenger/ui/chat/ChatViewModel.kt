package com.android.example.messenger.ui.chat

import android.annotation.SuppressLint
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.android.example.messenger.data.db.CategoryRepository
import com.android.example.messenger.data.db.ConversationRepository
import com.android.example.messenger.data.db.MessagesRepository
import com.android.example.messenger.models.CategoryModel
import com.android.example.messenger.models.ConversationModel
import com.android.example.messenger.models.MessageModel
import com.android.example.messenger.models.TYPE_OF_MESSAGE
import com.android.example.messenger.ui.main.Status
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.notify
import okhttp3.internal.notifyAll
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class ChatViewModel(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessagesRepository,
    categoryRepository: CategoryRepository,
    conversationId: Long,
    recipientId: Long
) : ViewModel() {


    private val observer = Observer<List<MessageModel>> {
        Log.d("obs", "in chat view model ${it.hashCode()}")
        listLoadedMutableLiveData.value = if (it.isEmpty()) {
            MessageStatus.EMPTY
        } else {
            MessageStatus.NOT_EMPTY
        }
    }

    private val listLoadedMutableLiveData: MutableLiveData<MessageStatus> by lazy {
        MutableLiveData<MessageStatus>().also { it.value = MessageStatus.NOT_LOADED }
    }

    val listLoadedLiveData: LiveData<MessageStatus> = listLoadedMutableLiveData

    val liveData: LiveData<List<MessageModel>> =
        messageRepository.getByConversationOrRecipientId(conversationId, recipientId).asLiveData()

    var categoriesLiveData: LiveData<List<CategoryModel>> = categoryRepository.allCategories.asLiveData()


    private val fetchFromWebStatusMutableLiveData: MutableLiveData<MessageStatus> by lazy {
        MutableLiveData<MessageStatus>().also { it.value = MessageStatus.NOT_LOADED }
    }
    val fetchFromWebStatusLiveData: MutableLiveData<MessageStatus> =
        fetchFromWebStatusMutableLiveData

    fun chooseCategory(category: String, conversationId: Long): String {
        viewModelScope.launch {
            val conversationModel =
                withContext(Dispatchers.IO) { conversationRepository.getConversationById(conversationId) }
            val newConversation = withContext(Dispatchers.IO) {
                conversationModel?.let {
                    ConversationModel(
                        conversationId = conversationModel.conversationId,
                        secondPartyUsername = conversationModel.secondPartyUsername,
                        imgUrl = conversationModel.imgUrl,
                        lastOnline = conversationModel.lastOnline,
                        unread = conversationModel.unread,
                        lastMessage = conversationModel.lastMessage,
                        lastMessageTime = conversationModel.lastMessageTime,
                        recipientId = conversationModel.recipientId,
                        pinned = conversationModel.pinned,
                        category = category
                    )
                }
            }
            if (newConversation != null) {
                withContext(Dispatchers.IO) { conversationRepository.insertOrUpdate(newConversation) }
            }

        }
        return category
    }

    fun updateUri(message: MessageModel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                messageRepository.insertOrUpdate(message)
            }
        }
    }


    fun addMessage(message: MessageModel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { messageRepository.insert(message) }
            withContext(Dispatchers.IO) { messageRepository.readMessage(message) }
            val conversationModel =
                withContext(Dispatchers.IO) { conversationRepository.getConversationById(message.conversationId) }
            val newConversation = withContext(Dispatchers.IO) {
                conversationModel?.conversationId?.let {
                    ConversationModel(
                        conversationId = it,
                        secondPartyUsername = conversationModel.secondPartyUsername,
                        imgUrl = conversationModel.imgUrl,
                        lastOnline = conversationModel.lastOnline,
                        unread = conversationModel.unread,
                        lastMessage = message.body.toString(),
                        lastMessageTime = message.createdAt,
                        recipientId = message.unread,
                        pinned = conversationModel.pinned,
                        category = conversationModel.category
                    )
                }
            }
            if (newConversation != null) {
                withContext(Dispatchers.IO) { conversationRepository.insertOrUpdate(newConversation) }
            }
        }
    }

    fun sendMessage(
        conversationId: Long,
        senderId: Long,
        recipientId: Long,
        body: String,
        type: TYPE_OF_MESSAGE,
        uri: Uri?,
        latitude: String, longitude: String
    ) {

        val message = MessageModel(
            conversationId = conversationId,
            senderId = senderId,
            recipientId = recipientId,
            id = 5000L + Random().nextLong(),
            body = body,
            createdAt = LocalDateTime.now().atZone(ZoneId.systemDefault())
                .withZoneSameInstant((ZoneId.of("UTC"))).toLocalDateTime().toString(),
            unread = recipientId,
            type = type,
            url = "",
            uri = null,
            localId = UUID.randomUUID().toString(),
            latitude = latitude,
            longitude = longitude
        )



        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                messageRepository.insertOrUpdate(message)
            }
        }

        if (type == TYPE_OF_MESSAGE.TEXT || type == TYPE_OF_MESSAGE.LOCATION) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    messageRepository.sendMessage(
                        message
                    )
                }
            }
        } else if (uri != null) {
            val storageReference = FirebaseStorage.getInstance().reference
            val path = if (type == TYPE_OF_MESSAGE.DOCUMENT) {
                storageReference.child("/files").child(UUID.randomUUID().toString())
            } else {
                storageReference.child("/images").child(UUID.randomUUID().toString())
            }
            path.putFile(uri)
                .addOnSuccessListener {
                    path.downloadUrl.addOnCompleteListener {
                        if (it.isSuccessful) {
                            val documentUrl = it.result.toString()
                            if (documentUrl.isNotEmpty() && documentUrl != null) {
                                message.url = documentUrl
                                viewModelScope.launch {
                                    withContext(Dispatchers.IO) {
                                        messageRepository.sendMessage(
                                            message
                                        )
                                    }
                                }
                            }
                        }
                    }
                }.addOnFailureListener {
                    val it = message.copy()
                    it.body = "Unsuccessful upload"
                    viewModelScope.launch {
                        withContext(Dispatchers.IO) {
                            messageRepository.insertOrUpdate(message)
                        }
                    }
                }

        } else {
            val it = message.copy()
            it.body = "File doesn't exist"
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    messageRepository.insertOrUpdate(message)
                }
            }
        }


    }


    fun fetchFromWebById(id: Long) {
        if (id > 0) {
            viewModelScope.launch {
                fetchFromWebStatusLiveData.value = MessageStatus.NOT_LOADED
                val result = withContext(Dispatchers.IO) {
                    messageRepository.fetchConversationFromWebById(id)
                }
                if (result) {
                    fetchFromWebStatusLiveData.value = MessageStatus.NOT_EMPTY
                }

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

class ChatViewModelFactory(
    private val conversationRepository: ConversationRepository,
    private val messagesRepository: MessagesRepository,
    private val categoryRepository: CategoryRepository,
    private val conversationId: Long,
    private val recipientId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(
                conversationRepository,
                messagesRepository,
                categoryRepository,
                conversationId,
                recipientId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}

enum class MessageStatus {
    NOT_LOADED,
    EMPTY,
    NOT_EMPTY
}




package com.android.example.messenger.ui.chat

import android.util.Log
import android.widget.Toast
import com.android.example.messenger.data.vo.ConversationListVO
import com.android.example.messenger.data.vo.ConversationVO
import com.android.example.messenger.data.vo.MessageVO
import com.example.messenger.utils.message.Message
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.util.*
import kotlin.collections.ArrayList


class ChatPresenterImpl(val view: ChatView) : ChatPresenter, ChatInteractor.OnMessageSendFinishedListener,
    ChatInteractor.OnMessageLoadFinishedListener, ChatInteractor.OnMessagesLoadFinishedListener{

    private val interactor: ChatInteractor = ChatInteractionImpl(view.getContext())
    private lateinit var messages: ArrayList<MessageVO>
    private var newMessages: ArrayList<MessageVO> = ArrayList()
    private var adapter: AdapterMessages = AdapterMessages(view.getContext(), newMessages, this)


    override fun addMessageReceive(message: MessageVO?){
        if (message != null) {
            message.createdAt = getTime(message.createdAt)
            newMessages.add(message)
            adapter = AdapterMessages(view.getContext(), newMessages, this)
            view.onLoadSuccessNewMessage(adapter)
        }
    }

    override fun deleteMessage(message: MessageVO?) {
        Toast.makeText(view.getContext(), "delete success", Toast.LENGTH_LONG).show()
    }


    override fun onLoadSuccess(conversationVO: ConversationVO) {
        val conversationId = conversationVO.conversationId
        val date = "Online last at ${getTime(conversationVO.lastOnline)}"
        val url = conversationVO.imgUrl
        val user = conversationVO.secondPartyUsername
        messages = conversationVO.messages
        newMessages = ArrayList<MessageVO>()
        messages.forEach {
            newMessages.add(MessageVO(it.id, it.senderId, it.recipientId, it.conversationId, it.body,  getTime(it.createdAt), it.unread))
        }
        adapter = AdapterMessages(view.getContext(), newMessages, this)
        view.onLoadSuccess(user, date, url, adapter, conversationId)
    }

    override fun getTime(createdAt: String) : String{
        val dateNow = LocalDateTime.now()
        val date = LocalDateTime.parse(createdAt)
        if ((dateNow.minusDays(1)) > date) {
            val day = LocalDateTime.parse(createdAt)
            val dayOfWeek: DayOfWeek = day.dayOfWeek

            return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
        }
        return DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.parse(createdAt))
    }

    override fun onLoadError() {
        view.showConversationLoadError()
    }

    override fun onSendSuccess(messageVO: MessageVO) {
        if (messageVO != null) {
            messageVO.createdAt = getTime(messageVO.createdAt)
            newMessages.add(messageVO)
            adapter = AdapterMessages(view.getContext(), newMessages, this)
            view.onLoadSuccessNewMessage(adapter)
        }
        view.onSendSuccess(adapter, messageVO)
    }

    override fun onSendError() {
        view.showMessageSendError()
    }


    /**
     * Called by [ChatView] to send a message to a user
     * @param recipientId unique id of message recipient
     * @param message message to be sent to recipient
     */
    override fun sendMessage(senderId : Long, recipientId: Long, message: String) {
        interactor.sendMessage(recipientId, message,this)
    }

    /**
     * Called by [ChatView] to load the messages in an opened thread
     * @param conversationId unique id of conversation to be loaded
     */
    override fun loadMessages(conversationId: Long) {
        if(conversationId == -1L){
            interactor.loadAllMessages(this)
        }else {
            interactor.loadMessages(conversationId, this)
        }
    }

    override fun onLoadMessagesSuccess(conversationVO: ConversationListVO) {
        val recipientName = view.getRecipientName()
        if(recipientName != null){
            var conversations = conversationVO.conversations
            conversations.forEach{
                if(it.secondPartyUsername == recipientName){
                    loadMessages(it.conversationId)
                }
            }
        }
    }

    override fun onLoadMessagesError() {
        view.showConversationLoadError()
    }


}
package com.android.example.messenger.ui.chat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.vo.MessageVO
import com.example.messenger.utils.message.Message

class AdapterMessages : RecyclerView.Adapter<RecyclerView.ViewHolder>, Filterable {

    private val context: Context
    var messagesList: ArrayList<MessageVO>
    val filterMessageList: ArrayList<MessageVO>
    private var preferences: AppPreferences
    private lateinit var presenter: ChatPresenter

    private var filter: FilterMessage? = null

    val ITEM_SENT = 1
    val ITEM_RECIEVE = 2

    constructor(context: Context, messagesList: ArrayList<MessageVO>, presenter: ChatPresenterImpl) {
        this.context = context
        this.messagesList = messagesList
        preferences = AppPreferences.create(context)
        this.filterMessageList = messagesList
        this.presenter = presenter
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == ITEM_SENT) {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.row_sent_messages, parent, false)
            SentViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.row_receive_messages, parent, false)
            ReceiveViewHolder(view)
        }

    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView = itemView.findViewById<TextView>(R.id.txt_sent_message)
        val sentTime: TextView = itemView.findViewById<TextView>(R.id.txt_sent_message_time)
        val sentCard = itemView.findViewById<ConstraintLayout>(R.id.cart_sent)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById<TextView>(R.id.txt_receive_message)
        val receiveTime: TextView = itemView.findViewById<TextView>(R.id.txt_receive_message_time)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messagesList[position]
        return if (preferences.userDetails.id == message.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECIEVE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = messagesList[position]
        var message = model.body
        if (holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder
            holder.sentMessage.text = model.body.toString()
            holder.sentTime.text = model.createdAt.toString()
            holder.sentCard.setOnClickListener {
                presenter.deleteMessage(model)
            }
        } else {
            val viewHolder = holder as ReceiveViewHolder
            holder.receiveMessage.text = model.body.toString()
            holder.receiveTime.text = model.createdAt.toString()
        }
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterMessage(filterMessageList, this)
        }
        return filter as FilterMessage
    }

}
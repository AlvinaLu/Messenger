package com.android.example.messenger.ui.chat

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.databinding.MessageItemTextBinding
import com.android.example.messenger.models.MessageModel
import com.android.example.messenger.utils.message.toTime
import com.android.example.messenger.utils.message.toTimeMessage

class TextViewHolder(
    private val binding: MessageItemTextBinding,
    private val interaction: MessageAdapter.Interaction?,
) :
    RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    val preferences: AppPreferences = AppPreferences.create(binding.root.context)

    private lateinit var storedItem: MessageModel

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        interaction?.imageClicked(storedItem)
    }

    fun bind(item: MessageModel) {
        storedItem = item
        if (item.senderId == preferences.userDetails.id) {
            binding.chatUserMessageSent.text = item.body
            binding.chatUserMessageTimeSent.text = item.createdAt.toTimeMessage()
            binding.blocUserMessageSent.visibility = View.VISIBLE
            binding.chatUserMessageSent.visibility = View.VISIBLE
            binding.chatUserMessageTimeSent.visibility = View.VISIBLE

            binding.blocUserMessageReceive.visibility = View.GONE
            binding.chatUserMessageReceive.visibility = View.GONE
            binding.chatUserMessageTimeReceive.visibility = View.GONE


        }  else {
            binding.chatUserMessageReceive.text = item.body
            binding.chatUserMessageTimeReceive.text = item.createdAt.toTimeMessage()

            binding.blocUserMessageSent.visibility = View.GONE
            binding.chatUserMessageSent.visibility = View.GONE
            binding.chatUserMessageTimeSent.visibility = View.GONE

            binding.blocUserMessageReceive.visibility = View.VISIBLE
            binding.chatUserMessageReceive.visibility = View.VISIBLE
            binding.chatUserMessageTimeReceive.visibility = View.VISIBLE

        }

    }

}
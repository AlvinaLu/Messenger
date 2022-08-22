package com.android.example.messenger.ui.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.DiffUtil
import com.android.example.messenger.models.ConversationModel
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.databinding.VhConversationsBinding
import com.android.example.messenger.models.MessageModel
import com.android.example.messenger.utils.message.avatar.RoundedCornersTransformation
import com.android.example.messenger.utils.message.cut
import com.android.example.messenger.utils.message.toTime
import com.avatarfirst.avatargenlib.AvatarGenerator
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class ConversationAdapter(private val interaction: Interaction? = null) :
    ListAdapter<ConversationModel, ConversationAdapter.ConversationViewHolder>(
        PoiModelDiffCallback()
    ), Filterable {

    var mListRef: List<ConversationModel>? = null
    var mFilteredList: List<ConversationModel>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        return ConversationViewHolder.from(parent, interaction)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun swapData(data: List<ConversationModel>) {
        mListRef = data
        submitList(data.toMutableList())
    }


    class ConversationViewHolder(
        private val binding: VhConversationsBinding,
        private val interaction: Interaction?,
    ) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        val preferences: AppPreferences = AppPreferences.create(binding.root.context)


        lateinit var storedItem: ConversationModel

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            interaction?.itemClicked(storedItem)
        }

        override fun onLongClick(v: View?): Boolean {
            interaction?.itemPinned(storedItem)
            Toast.makeText(binding.root.context, "User Pinned", Toast.LENGTH_LONG).show()
            return true
        }

        fun bind(item: ConversationModel) {
            binding.tvNotification.visibility = View.INVISIBLE
            storedItem = item
            binding.tvUsername.text = item.secondPartyUsername

            binding.tvPreview.text = item.lastMessage.cut(34)
            binding.tvTime.text = item.lastOnline.toTime()

            if (item.unread > 0) {
                binding.tvNotification.text = item.unread.toString();
                binding.tvNotification.visibility = View.VISIBLE
                val animShake = AnimationUtils.loadAnimation(binding.root.context, R.anim.shake);
                binding.tvNotificationBox.startAnimation(animShake);
            }

            val avatarView = binding.avatar
            val imgUrl = item.imgUrl
            val generatedAvatar = AvatarGenerator.AvatarBuilder(binding.root.context)
                .setLabel(item.secondPartyUsername.toUpperCase())
                .setAvatarSize(56)
                .setTextSize(15)
                .toSquare()
                .setBackgroundColor(Color.rgb(173, 214, 237))
                .build()

            if (imgUrl.isNotEmpty()) {
                Picasso.get()
                    .load(imgUrl).fit()
                    .placeholder(generatedAvatar)
                    .error(generatedAvatar)
                    .transform(RoundedCornersTransformation(20, 0))
                    .into(avatarView);
            } else {
                val transformAvatar =
                    RoundedCornersTransformation(7, 0).transform(generatedAvatar.toBitmap())
                avatarView.setImageBitmap(transformAvatar)
            }
        }


        companion object {
            fun from(parent: ViewGroup, interaction: Interaction?): ConversationViewHolder {
                val binding: VhConversationsBinding = VhConversationsBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ),
                    parent,
                    false
                )
                return ConversationViewHolder(binding, interaction)
            }
        }


    }

    interface Interaction {
        fun itemClicked(item: ConversationModel)
        fun itemPinned(item: ConversationModel)
    }

    override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {

                val charString = charSequence.toString()

                if (charString.isEmpty() || charString == "All contacts") {
                    mFilteredList = mListRef
                } else {
                    mListRef?.let {
                        val filteredList = arrayListOf<ConversationModel>()
                        for (item in mListRef!!) {
                            if (item is ConversationModel) {
                                if (charString == item.category) {
                                    filteredList.add(item)
                                }
                            }
                        }
                        mFilteredList = filteredList
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = mFilteredList
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                mFilteredList = filterResults.values as ArrayList<ConversationModel>
                submitList(mFilteredList)
            }
        }
    }


    class PoiModelDiffCallback() : DiffUtil.ItemCallback<ConversationModel>() {
        override fun areItemsTheSame(oldItem: ConversationModel, newItem: ConversationModel): Boolean {
            return oldItem.conversationId == newItem.conversationId
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: ConversationModel, newItem: ConversationModel): Boolean {
            return (oldItem.conversationId == newItem.conversationId
                    && oldItem.pinned == newItem.pinned
                    && oldItem.unread == newItem.unread
                    && oldItem.lastMessage == newItem.lastMessage
                    && oldItem.imgUrl == newItem.imgUrl
                    && oldItem.lastMessageTime == newItem.lastMessageTime
                    && oldItem.lastOnline == newItem.lastOnline
                    && oldItem.category == newItem.category)
        }
    }
}


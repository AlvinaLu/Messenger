package com.android.example.messenger.ui.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.DiffUtil
import com.android.example.messenger.models.ConversationModel
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.databinding.ConversationPinnedBinding
import com.android.example.messenger.utils.message.avatar.RoundedCornersTransformation
import com.avatarfirst.avatargenlib.AvatarGenerator
import com.squareup.picasso.Picasso

class ConversationPinnedAdapter(private val interaction: InteractionPinned? = null) :
    ListAdapter<ConversationModel, ConversationPinnedAdapter.ConversationPinnedViewHolder>(
        PoiModelDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationPinnedViewHolder {
        return ConversationPinnedViewHolder.from(parent, interaction)
    }

    override fun onBindViewHolder(holder: ConversationPinnedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun swapData(data: List<ConversationModel>) {
        submitList( data.filter { it.pinned }.toMutableList())
    }


    class ConversationPinnedViewHolder(
        private val binding: ConversationPinnedBinding,
        private val interaction: InteractionPinned?,
    ) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        val preferences: AppPreferences = AppPreferences.create(binding.root.context)



        lateinit var storedItem: ConversationModel

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            interaction?.itemClicked(storedItem)
        }
        

        fun bind(item: ConversationModel) {
            storedItem = item
            var name  = item.secondPartyUsername
            if (name.length > 20) {
                name = name.substring(0, 20)
                name += "..."
            }
            binding.tvUsername.text = name
            var message = item.lastMessage
            if (message.length > 20) {
                message = message.substring(0, 20)
                message += "..."
            }
            binding.tvPreview.text = message
            

            val avatarView = binding.avatar
            val imgUrl = item.imgUrl
            val generatedAvatar = AvatarGenerator.AvatarBuilder(binding.root.context)
                .setLabel(item.secondPartyUsername.uppercase())
                .setAvatarSize(46)
                .setTextSize(12)
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
            binding.imbPinned.setOnClickListener{
               interaction?.itemUnPinned(item)
            }
        }



        companion object {
            fun from(parent: ViewGroup, interaction: InteractionPinned?): ConversationPinnedViewHolder {
                val binding: ConversationPinnedBinding = ConversationPinnedBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ),
                    parent,
                    false
                )
                return ConversationPinnedViewHolder(binding, interaction)
            }
        }


    }

    interface InteractionPinned {
        fun itemClicked(item: ConversationModel)
        fun itemUnPinned(item: ConversationModel)
    }


    class PoiModelDiffCallback() : DiffUtil.ItemCallback<ConversationModel>() {
        override fun areItemsTheSame(oldItem: ConversationModel, newItem: ConversationModel): Boolean {
            return oldItem.conversationId == newItem.conversationId
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: ConversationModel, newItem: ConversationModel): Boolean {
            return oldItem == newItem
        }
    }
}


package com.android.example.messenger.ui.experimental

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.databinding.VhContactsBinding
import com.android.example.messenger.utils.message.avatar.RoundedCornersTransformation
import com.avatarfirst.avatargenlib.AvatarGenerator
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class ExperimentalAdapter(private val interaction: Interaction? = null) :
    ListAdapter<ContactsModel, ExperimentalAdapter.ExperimentalViewHolder>(PoiModelDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperimentalViewHolder {
        return ExperimentalViewHolder.from(parent, interaction)
    }

    override fun onBindViewHolder(holder: ExperimentalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun swapData(data: List<ContactsModel>) {
        submitList(data.toMutableList())
    }

    class ExperimentalViewHolder(
        private val binding: VhContactsBinding,
        private val interaction: Interaction?,
    ) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private val radius = 20
        private val margin = 0
        private val transformation: Transformation = RoundedCornersTransformation(radius, margin)

        lateinit var storedItem: ContactsModel

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            interaction?.itemClicked(storedItem)
        }

        fun bind(item: ContactsModel) {
            storedItem = item
            binding.tvUsernameContact.text = item.username
            binding.tvPhoneContact.text = item.phoneNumber
            binding.tvTimeContact.text = "Last seen\n at ${getTime(item.createdAt)}"

            val avatarView = binding.avatarContact
            val imgUrl = item.imgUrl

            val generatedAvatar = AvatarGenerator.AvatarBuilder(binding.root.context)
                .setAvatarSize(56)
                .setTextSize(15)
                .toSquare()
                .setBackgroundColor(Color.rgb(173, 214, 237))
                .build()


            if(imgUrl!=null && imgUrl.isNotEmpty()) {
                Picasso.get()
                    .load(imgUrl).fit()
                    .placeholder(generatedAvatar)
                    .error(generatedAvatar)
                    .transform(transformation)
                    .into(avatarView);
            }else{
                val transformAvatar = RoundedCornersTransformation(7, 0).transform(generatedAvatar.toBitmap())
                avatarView.setImageBitmap(transformAvatar)
            }

        }

        private fun getTime(createdAt: String): String {
            val dateNow = LocalDateTime.now()

            val date = LocalDateTime.parse(createdAt)
            if ((dateNow.minusDays(1)) > date) {
                val day = LocalDateTime.parse(createdAt)
                val dayOfWeek: DayOfWeek = day.dayOfWeek

                return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
            }
            return DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.parse(createdAt))
        }

        companion object {
            fun from(parent: ViewGroup, interaction: Interaction?): ExperimentalViewHolder {
                val binding: VhContactsBinding = VhContactsBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ),
                    parent,
                    false
                )
                return ExperimentalViewHolder(binding, interaction)
            }
        }
    }

    interface Interaction {
        fun itemClicked(item: ContactsModel)
    }

    class PoiModelDiffCallback() : DiffUtil.ItemCallback<ContactsModel>() {
        override fun areItemsTheSame(oldItem: ContactsModel, newItem: ContactsModel): Boolean {
            //Confirm that your id variable matches this one or change this one to match the one in your model
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: ContactsModel, newItem: ContactsModel): Boolean {
            return oldItem == newItem
        }
    }
}
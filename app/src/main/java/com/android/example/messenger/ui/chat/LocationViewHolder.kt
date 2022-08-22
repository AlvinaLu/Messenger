package com.android.example.messenger.ui.chat

import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.BuildConfig.GOOGLE_MAPS_API_KEY
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.databinding.MessageItemImageBinding
import com.android.example.messenger.databinding.MessageLocactionBinding
import com.android.example.messenger.models.MessageModel
import com.android.example.messenger.utils.message.toTime
import com.android.example.messenger.utils.message.toTimeMessage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.File

class LocationViewHolder(
    private val binding: MessageLocactionBinding,
    private val interaction: MessageAdapter.Interaction?,
) :
    RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    var block: ConstraintLayout? = null
    var time: TextView? = null
    var imageView: ImageView? = null


    val preferences: AppPreferences = AppPreferences.create(binding.root.context)

    private lateinit var storedItem: MessageModel

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        interaction?.imageClicked(storedItem)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun bind(item: MessageModel) {
        storedItem = item
        if (item.senderId == preferences.userDetails.id) {
            block = binding.blocUserMessageSent
            time = binding.chatUserMessageTimeSent
            imageView = binding.userMapSent


            binding.blocUserMessageReceive.visibility = View.GONE
            binding.userMapReceive.visibility = View.GONE
            binding.chatUserMessageTimeReceive.visibility = View.GONE

        } else if (item.recipientId == preferences.userDetails.id) {
            block = binding.blocUserMessageReceive
            time = binding.chatUserMessageTimeReceive
            imageView = binding.userMapReceive

            binding.blocUserMessageSent.visibility = View.GONE
            binding.userMapSent.visibility = View.GONE
            binding.chatUserMessageTimeSent.visibility = View.GONE

        }
        if (item.latitude.isNotEmpty() && item.longitude.isNotEmpty()){
            Glide.with(binding.root.context)
                .load(Uri.parse("https://maps.google.com/maps/api/staticmap?center=" +
                        item.latitude +
                        "," +
                        item.longitude +
                        "&zoom=14&size=200x150&scale=2" +
                        "&markers=color:0xADD6ED%7Clabel:%7C" +
                        item.latitude +
                        "," +
                        item.longitude + "&key=${GOOGLE_MAPS_API_KEY}"))
                .centerCrop()
                .into(imageView!!);

            block!!.visibility = View.VISIBLE
            time!!.text = item.createdAt.toTimeMessage()
            time!!.visibility = View.VISIBLE
            imageView!!.visibility = View.VISIBLE

            block!!.setOnClickListener {
                interaction?.locationClicked(item)
                Toast.makeText(
                    binding.root.context,
                    "Location clicked",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }
}

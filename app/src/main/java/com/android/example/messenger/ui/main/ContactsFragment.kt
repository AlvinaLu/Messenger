package com.android.example.messenger.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.ChatterApp
import com.android.example.messenger.R
import com.android.example.messenger.data.vo.UserVO
import com.android.example.messenger.ui.chat.ChatActivity
import com.android.example.messenger.ui.chat.ChatView
import com.android.example.messenger.ui.experimental.ExperimentalActivity
import com.android.example.messenger.ui.experimental.ExperimentalViewModel
import com.android.example.messenger.utils.message.avatar.RoundedCornersTransformation
import com.avatarfirst.avatargenlib.AvatarConstants
import com.avatarfirst.avatargenlib.AvatarGenerator
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList


class ContactsFragment : Fragment() {

    private lateinit var activity: MainActivity
    private lateinit var rvContacts: RecyclerView
    var contacts: ArrayList<UserVO> = ArrayList()
    lateinit var contactsAdapter: ContactsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val baseLayout = inflater.inflate(R.layout.fragment_contacts, container, false)
        rvContacts = baseLayout.findViewById(R.id.rv_contacts)
        contactsAdapter = ContactsAdapter(activity, contacts)

        rvContacts.adapter = contactsAdapter
        rvContacts.layoutManager = LinearLayoutManager(activity.baseContext)

        return baseLayout
    }

    fun setActivity(activity: MainActivity) {
        this.activity = activity
    }

    class ContactsAdapter(private val context: Context, private val dataSet: List<UserVO>) :
        RecyclerView.Adapter<ContactsAdapter.ViewHolder>(), ChatView.ChatAdapter {

        private val radius = 20
        private val margin = 0
        private val transformation: Transformation = RoundedCornersTransformation(radius, margin)


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.vh_contacts, parent, false)
            val llContainer = itemLayout.findViewById<LinearLayout>(R.id.ll_container)

            return ViewHolder(llContainer)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataSet[position]
            val itemLayout = holder.itemLayout

            itemLayout.findViewById<TextView>(R.id.tv_username_contact).text = item.username
            itemLayout.findViewById<TextView>(R.id.tv_phone_contact).text = item.phoneNumber
            itemLayout.findViewById<TextView>(R.id.tv_time_contact).text = "Last seen\n at ${getTime(item.createdAt)}"

            val avatarView = itemLayout.findViewById<ImageView>(R.id.avatar_contact)
            val imgUrl = item.imgUrl

            val generatedAvatar = AvatarGenerator.AvatarBuilder(context)
                .setLabel(item.username)
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

            itemLayout.setOnClickListener {
                navigateToChat(item.username, item.id, item.imgUrl, item.lastOnline)
            }
        }

        fun getTime(createdAt: String) : String{
            val dateNow = LocalDateTime.now()

            val date = LocalDateTime.parse(createdAt)
            if ((dateNow.minusDays(1)) > date) {
                val day = LocalDateTime.parse(createdAt)
                val dayOfWeek: DayOfWeek = day.dayOfWeek

                return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
            }
            return DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.parse(createdAt))
        }

        override fun getItemCount(): Int {
            return dataSet.size
        }

        override fun navigateToChat(recipientName: String, recipientId: Long, imgUrl: String, lastOnline: String, conversationId: Long? ) {
            val intent = Intent(context, ExperimentalActivity::class.java)
            intent.putExtra("RECIPIENT_ID", recipientId)
            intent.putExtra("RECIPIENT_NAME", recipientName)
            intent.putExtra("RECIPIENT_URl", imgUrl)
            intent.putExtra("RECIPIENT_LAST_ONLINE", lastOnline)

            context.startActivity(intent)
        }

        class ViewHolder(val itemLayout: LinearLayout) : RecyclerView.ViewHolder(itemLayout)
    }
}
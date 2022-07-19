package com.android.example.messenger.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.vo.Conversation
import com.android.example.messenger.data.vo.ConversationVO
import com.android.example.messenger.data.vo.MessageVO
import com.android.example.messenger.ui.chat.ChatActivity
import com.android.example.messenger.ui.chat.ChatView
import com.android.example.messenger.utils.message.avatar.RoundedCornersTransformation
import com.avatarfirst.avatargenlib.AvatarGenerator
import com.example.messenger.utils.message.Message
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.util.*
import kotlin.collections.ArrayList

class ConversationsFragment : Fragment(), View.OnClickListener {


    private lateinit var activity: MainActivity
    private lateinit var rvConversations: RecyclerView
    private lateinit var fabContacts: FloatingActionButton
    var conversations: ArrayList<Conversation> = ArrayList()
    lateinit var conversationsAdapter: ConversationsAdapter
    var receivedConversationId: Long? = null

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if(intent.extras?.getLong("unread") == activity.preferences.userDetails.id){
                val receivedConversationId = intent.extras?.getLong("conversationId") ?: -1
                val  newMessage =
                    MessageVO(
                        intent.extras?.getLong("id")!!,
                        intent.extras?.getLong("senderId")!!,
                        intent.extras?.getLong("recipientId")!!,
                        intent.extras?.getLong("conversationId")!!,
                        intent.extras?.getString("bodyMessage").toString(),
                        intent.extras?.getString("messageCreatedAt").toString(),
                        intent.extras?.getLong("unread")!!,
                        )
                activity.presenter.addMessageReceive(receivedConversationId, newMessage)
            }

        }
    }

    /**
     * Function called when user interface of ConversationsFragment
     * is being drawn for the first time
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment layout inflation
        val baseLayout = inflater.inflate(R.layout.fragment_conversations, container, false)

        // Layout view bindings
        rvConversations = baseLayout.findViewById(R.id.rv_conversations)
        fabContacts = baseLayout.findViewById(R.id.fab_contacts)


        conversationsAdapter = ConversationsAdapter(activity, conversations)

        // Setting the adapter of conversations recycler view to created conversations adapter
        rvConversations.adapter = conversationsAdapter

        // Setting the layout manager of conversations recycler view a linear layout manager
        rvConversations.layoutManager = LinearLayoutManager(activity.baseContext)

        fabContacts.setOnClickListener(this)

        return baseLayout
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(activity).registerReceiver(messageReceiver, IntentFilter("MyData"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(messageReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.fab_contacts) {
            this.activity.showContactsScreen()
        }
    }

    fun setActivity(activity: MainActivity) {
        this.activity = activity
    }

    /**
     * Custom adapter for conversations recycler view
     * @property context
     * @property dataSet List containing data set of conversations recycler view
     */
    class ConversationsAdapter(
        private val context: Context,
        private val dataSet: List<Conversation>,
    ) :
        RecyclerView.Adapter<ConversationsAdapter.ViewHolder>(), ChatView.ChatAdapter {

        val preferences: AppPreferences = AppPreferences.create(context)

        val radius = 20
        val margin = 0
        val transformation: Transformation = RoundedCornersTransformation(radius, margin)

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataSet[position] // get item at current position
            val itemLayout = holder.itemLayout // bind view holder layout to local variable
            val notification = itemLayout.findViewById<TextView>(R.id.tv_notification)
            notification.visibility = View.INVISIBLE
            var quantity = 0
            item.messages.forEach {
                if(it.unread == preferences.userDetails.id){
                    quantity++
                }
            }
            if(quantity>0){
                notification.text = quantity.toString();
                notification.visibility = View.VISIBLE
            }
            // Setting data of layout's TextView widgets
            itemLayout.findViewById<TextView>(R.id.tv_username).text = item.secondPartyUsername
            var message: String = item.messages[item.messages.size - 1].body
            if (message.length > 35) {
                message = message.substring(0, 34)
                message += "..."
            }
            itemLayout.findViewById<TextView>(R.id.tv_preview).text = message
            itemLayout.findViewById<TextView>(R.id.tv_time).text =
                getTime(item.messages[item.messages.size - 1].createdAt)

            val avatarView = itemLayout.findViewById<ImageView>(R.id.avatar)
            val imgUrl = item.imgUrl
            val generatedAvatar = AvatarGenerator.AvatarBuilder(context)
                .setLabel(item.secondPartyUsername)
                .setAvatarSize(56)
                .setTextSize(15)
                .toSquare()
                .setBackgroundColor(Color.rgb(173, 214, 237))
                .build()

            if (imgUrl != null && imgUrl.isNotEmpty()) {
                Picasso.get()
                    .load(imgUrl).fit()
                    .placeholder(generatedAvatar)
                    .error(generatedAvatar)
                    .transform(transformation)
                    .into(avatarView);
            } else {
                val transformAvatar =
                    RoundedCornersTransformation(7, 0).transform(generatedAvatar.toBitmap())
                avatarView.setImageBitmap(transformAvatar)
            }

            itemLayout.setOnClickListener {
                val message = item.messages[0]
                val recipientId: Long
                notification.visibility = View.INVISIBLE

                item.messages.forEach {
                    if(it.unread == preferences.userDetails.id){
                        it.unread = -1
                    }
                }

                recipientId = if (message.senderId == preferences.userDetails.id) {
                    message.recipientId
                } else {
                    message.senderId
                }
                navigateToChat(
                    item.secondPartyUsername,
                    recipientId,
                    item.imgUrl,
                    item.lastOnline,
                    item.conversationId
                )
            }
        }


        fun getTime(createdAt: String): String {
            val dateNow = LocalDateTime.now()

            val date = LocalDateTime.parse(createdAt)
            if ((dateNow.minusDays(1)) > date) {
                val day = LocalDateTime.parse(createdAt)
                val dayOfWeek: DayOfWeek = day.dayOfWeek

                return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
            }
            return DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.parse(createdAt))
        }

        /**
         * Invoked when the RecyclerView needs a new RecyclerView.ViewHolder instance
         * to represent an item in the data set
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.vh_conversations, null, false)
                .findViewById<LinearLayout>(R.id.ll_container)

            return ViewHolder(itemLayout)
        }

        /**
         * Called to get the number of items in data set
         */
        override fun getItemCount(): Int {
            return dataSet.size
        }

        /**
         * Navigates the user to conversation thread
         * @param recipientName name of chat recipient
         * @param recipientId unique id of recipient
         * @param conversationId unique id of active conversation
         */
        override fun navigateToChat(
            recipientName: String,
            recipientId: Long,
            mgUrl: String,
            lastOnline: String,
            conversationId: Long?
        ) {
            val intent = Intent(context, ChatActivity::class.java)

            // Putting extra data into intent
            intent.putExtra("CONVERSATION_ID", conversationId)
            intent.putExtra("RECIPIENT_ID", recipientId)
            intent.putExtra("RECIPIENT_NAME", recipientName)

            context.startActivity(intent)
        }

        /**
         * @property itemLayout layout view of [ViewHolder]
         */
        class ViewHolder(val itemLayout: LinearLayout) : RecyclerView.ViewHolder(itemLayout)

    }
}
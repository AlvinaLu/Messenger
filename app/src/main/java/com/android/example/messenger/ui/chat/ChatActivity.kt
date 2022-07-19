package com.android.example.messenger.ui.chat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.data.vo.Conversation
import com.android.example.messenger.data.vo.MessageVO
import com.android.example.messenger.databinding.ActivityChatBinding
import com.android.example.messenger.ui.main.ConversationsFragment
import com.android.example.messenger.utils.message.avatar.RoundedCornersTransformation
import com.avatarfirst.avatargenlib.AvatarGenerator
import com.example.messenger.utils.message.Message
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation


class ChatActivity : AppCompatActivity(), ChatView {

    private var recipientId: Long = -1
    var conversationId: Long = -1
    private var recipientName: String? = null
    private var urlImg: String? = null
    private var lastOnline: String? = null
    private lateinit var preferences: AppPreferences
    private lateinit var presenter: ChatPresenter
    private lateinit var binding: ActivityChatBinding
    private var adMessages: AdapterMessages? = null
    private lateinit var recipientAvatar: View
    private var keyboardListenersAttached = false
    private var rootLayout: ViewGroup? = null

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val receivedConversationId = intent.extras?.getLong("conversationId")
            if (receivedConversationId == conversationId) {
                var newMessage =
                    MessageVO(
                        intent.extras?.getLong("id")!!,
                        intent.extras?.getLong("senderId")!!,
                        intent.extras?.getLong("recipientId")!!,
                        intent.extras?.getLong("conversationId")!!,
                        intent.extras?.getString("bodyMessage").toString(),
                        intent.extras?.getString("messageCreatedAt").toString(),
                        intent.extras?.getLong("unread")!!,
                    )
                presenter.addMessageReceive(newMessage)

            }
        }
    }


    val radius = 20
    val margin = 0
    val transformation: Transformation = RoundedCornersTransformation(radius, margin)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = AppPreferences.create(this)
        presenter = ChatPresenterImpl(this)

        conversationId = intent.getLongExtra("CONVERSATION_ID", -1)
        recipientId = intent.getLongExtra("RECIPIENT_ID", -1)
        recipientName = intent.getStringExtra("RECIPIENT_NAME")
        urlImg = intent.getStringExtra("RECIPIENT_URl")
        lastOnline = intent.getStringExtra("RECIPIENT_LAST_ONLINE")

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_chevronleft)
        supportActionBar?.elevation = 0F


        presenter.loadMessages(conversationId)

        binding.rvMessages.layoutManager = LinearLayoutManager(this.baseContext)

        (binding.rvMessages.getLayoutManager() as LinearLayoutManager).stackFromEnd = true


        binding.imgBtnSend.setOnClickListener {
            onSubmit(binding.etMessage.text.toString())
            binding.etMessage.text.clear()
            closeKeyBoard()

        }
        binding.etMessage.setOnClickListener {
            if (adMessages != null) {
                binding.rvMessages.smoothScrollToPosition(adMessages!!.messagesList.size - 1)
            }


        }


    }


    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    override fun onStart() {
        super.onStart()
        super.onResume()
        supportActionBar?.setDisplayShowCustomEnabled(true)

        val inflater: LayoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        recipientAvatar = inflater.inflate(R.layout.avatar_tollbar_chat, null)
        supportActionBar?.customView = recipientAvatar

        if (recipientName != null && urlImg != null && lastOnline != null) {
            addRecipientData(recipientName!!, urlImg!!, presenter.getTime(lastOnline!!))
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(messageReceiver, IntentFilter("MyData"))
    }


    override fun getRecipientName(): String? {
        return recipientName
    }


    override fun onLoadSuccess(
        secondPartyUsername: String,
        date: String,
        imgUrl: String,
        adapterMessages: AdapterMessages,
        conversationId: Long?
    ) {
        addRecipientData(secondPartyUsername, imgUrl, date)

        if (conversationId != null) {
            this.conversationId = conversationId
        }

        this.adMessages = adapterMessages
        binding.rvMessages.adapter = adMessages
        binding.rvMessages.smoothScrollToPosition(adMessages!!.messagesList.size - 1)
    }

    override fun onLoadSuccessNewMessage(
        adapterMessages: AdapterMessages
    ) {
        this.adMessages = adapterMessages
        binding.rvMessages.adapter = adMessages
        binding.rvMessages.smoothScrollToPosition(adMessages!!.messagesList.size - 1)
    }


    private fun addRecipientData(secondPartyUsername: String, imgUrl: String, date: String) {

        val avatarView = findViewById<ImageView>(R.id.avatar_toolbar)
        val textNameView = findViewById<TextView>(R.id.tv_username_toolbar)
        val textOnlineView = findViewById<TextView>(R.id.tv_online_toolbar)

        val generatedAvatar = AvatarGenerator.AvatarBuilder(this)
            .setLabel(secondPartyUsername)
            .setAvatarSize(44)
            .setTextSize(10)
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

        supportActionBar?.subtitle = null
        supportActionBar?.title = null

        textNameView.text = secondPartyUsername
        textOnlineView.text = date
    }


    override fun onSendSuccess(adapterMessages: AdapterMessages, messageVO: MessageVO) {
        this.adMessages = adapterMessages
        binding.rvMessages.adapter = adMessages
        binding.rvMessages.smoothScrollToPosition(adMessages!!.messagesList.size - 1)
        Toast.makeText(this, "Message sent", Toast.LENGTH_LONG).show()
        conversationId = messageVO.conversationId
    }

    /**
     * Function override from MessageInput.InputListener
     * Called when a user submits a message with the MessageInput widget
     * @param input message input submitted by user
     */
    private fun onSubmit(input: String): Boolean {
        presenter.sendMessage(preferences.userDetails.id, recipientId, input)
        return true
    }

    override fun showConversationLoadError() {
        Toast.makeText(
            this, "Unable to load thread. Please try again later.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun showMessageSendError() {
        Toast.makeText(
            this, "Unable to send message. Please try again later.",
            Toast.LENGTH_LONG
        ).show()
    }


    override fun bindViews() {
        TODO("Not yet implemented")
    }

    override fun getContext(): Context {
        return this
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.chat_menu, menu)

        val searchItem = menu?.findItem(R.id.search_button)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                searchView.setQuery("", false)
                searchItem.collapseActionView()
                Toast.makeText(this@ChatActivity, "looking for $query", Toast.LENGTH_LONG).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    adMessages?.filter?.filter(newText)
                } catch (e: Exception) {

                }
                return true
            }

        })
        return true
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }


}


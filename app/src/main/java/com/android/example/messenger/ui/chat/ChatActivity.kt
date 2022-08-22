package com.android.example.messenger.ui.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.example.messenger.ChatterApp
import com.android.example.messenger.LoginActivity
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.databinding.ActivityChatBinding
import com.android.example.messenger.databinding.NotificationBinding
import com.android.example.messenger.models.CategoryModel
import com.android.example.messenger.models.MessageModel
import com.android.example.messenger.models.TYPE_OF_MESSAGE
import com.android.example.messenger.utils.message.avatar.RoundedCornersTransformation
import com.android.example.messenger.utils.message.cut
import com.android.example.messenger.utils.message.toTime
import com.avatarfirst.avatargenlib.AvatarGenerator
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.animation.AnimationUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*


class ChatActivity : AppCompatActivity(), MessageAdapter.Interaction {

    private var recipientId: Long = -1
    var conversationId: Long = -1
    private var recipientName: String? = null
    private var urlImg: String? = null
    private var lastOnline: String? = null
    private lateinit var preferences: AppPreferences
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var chatViewModel: ChatViewModel
    private var url: String? = null
    private var uri: Uri? = null
    private var typeOfMessage = TYPE_OF_MESSAGE.TEXT
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var popupmenu: PopupMenu
    private val paths = arrayOf("item 1", "item 2", "item 3")
    private var categories: List<CategoryModel>? = null
    private var category = "All contacts"


    private var launcherDocument =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                typeOfMessage = TYPE_OF_MESSAGE.DOCUMENT
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                this.uri = uri
                getFileName(uri)?.let {
                    onSubmit(
                        it,
                        TYPE_OF_MESSAGE.DOCUMENT,
                        uri,
                        "",
                        ""
                    )
                }
                typeOfMessage = TYPE_OF_MESSAGE.TEXT

            }
        }

    private var launcherImage =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (result.data?.data != null) {
                        typeOfMessage = TYPE_OF_MESSAGE.IMAGE
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        this.uri = result?.data?.data!!

                        getFileName(result?.data?.data!!)?.let {
                            onSubmit(
                                it,
                                TYPE_OF_MESSAGE.IMAGE,
                                uri,
                                "",
                                ""
                            )
                        }
                        typeOfMessage = TYPE_OF_MESSAGE.TEXT

                    }
                }

            }
        )

    private var launcherOpenDocument =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {

        }

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val receivedConversationId = intent.extras?.getLong("conversationId")
            if (receivedConversationId == conversationId) {
                var newMessage =
                    MessageModel(
                        intent.extras?.getLong("id")!!,
                        intent.extras?.getLong("senderId")!!,
                        intent.extras?.getLong("recipientId")!!,
                        intent.extras?.getLong("conversationId")!!,
                        intent.extras?.getString("bodyMessage").toString(),
                        intent.extras?.getString("messageCreatedAt").toString(),
                        intent.extras?.getLong("unread")!!,
                        TYPE_OF_MESSAGE.valueOf(intent.extras?.getString("type").toString()),
                        intent.extras?.getString("url").toString(),
                        null,
                        UUID.randomUUID().toString(),
                        intent.extras?.getString("latitude").toString(),
                        intent.extras?.getString("longitude").toString(),
                    )
                chatViewModel.addMessage(newMessage)

            }

            if (intent.extras?.getLong("senderId") != recipientId) {
                showToast(
                    intent.extras?.getLong("conversationId"),
                    intent.extras?.getString("senderUsername").toString(),
                    intent.extras?.getLong("senderId"),
                    intent.extras?.getString("url").toString(),
                    intent.extras?.getString("senderLastOnline").toString(),
                    "All contacts",
                    intent.extras?.getString("bodyMessage").toString()
                )
            }

        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        preferences = AppPreferences.create(this)

        messageAdapter = MessageAdapter(this)
        with(binding.rvMessages) {
            adapter = messageAdapter
            val lM = LinearLayoutManager(this@ChatActivity, LinearLayoutManager.VERTICAL, false)
            lM.stackFromEnd = true
            lM.isSmoothScrollbarEnabled = true
            layoutManager = lM
        }

        bottomSheetBehavior =
            BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_choice))
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        conversationId = intent.getLongExtra("CONVERSATION_ID", -1)
        recipientId = intent.getLongExtra("RECIPIENT_ID", -1)
        recipientName = intent.getStringExtra("RECIPIENT_NAME")
        urlImg = intent.getStringExtra("RECIPIENT_URl")
        lastOnline = intent.getStringExtra("RECIPIENT_LAST_ONLINE")
        if (intent.getStringExtra("CATEGORY") != null) {
            category = intent.getStringExtra("CATEGORY")!!
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_chevronleft)
        supportActionBar?.elevation = 0F
        supportActionBar?.isHideOnContentScrollEnabled = false


        chatViewModel = ViewModelProvider(
            this, ChatViewModelFactory(
                (this.applicationContext as ChatterApp).conversationsRepository,
                (this.applicationContext as ChatterApp).messagesRepository,
                (this.applicationContext as ChatterApp).categoryRepository,
                conversationId,
                recipientId
            )
        )[ChatViewModel::class.java]


        chatViewModel.listLoadedLiveData.observe(this) { dataStatus ->
            when (dataStatus) {
                MessageStatus.NOT_LOADED -> {
                    binding.rvMessages.visibility = View.INVISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.note.visibility = View.GONE

                }

                MessageStatus.EMPTY -> {
                    binding.rvMessages.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.note.text = "You have not messages yet"
                    binding.note.visibility = View.VISIBLE

                }

                MessageStatus.NOT_EMPTY -> {
                    binding.rvMessages.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.note.visibility = View.GONE

                }

                else -> {

                }
            }
        }

        binding.imgBtnSend.setOnClickListener {

            if (binding.etMessage.text.toString().isNotEmpty()) {
                onSubmit(binding.etMessage.text.toString(), TYPE_OF_MESSAGE.TEXT, null, "", "")
            } else {
                Toast.makeText(this, "Message is empty", Toast.LENGTH_LONG).show()
            }
            binding.etMessage.text.clear()
            typeOfMessage = TYPE_OF_MESSAGE.TEXT
            closeKeyBoard()

        }

        chatViewModel.fetchFromWebById(conversationId)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        chatViewModel.liveData.observe(this) {
            Log.d("obs", "in chat activity ${it.hashCode()}")
            messageAdapter.swapData(it)

            binding.rvMessages.scrollToPosition(messageAdapter.itemCount)
            binding.rvMessages.smoothScrollToPosition(messageAdapter.itemCount)
            binding.rvMessages.clearAnimation()
        }


        binding.imgBtnAttach.setOnClickListener { attach() }

        binding.rvMessages.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                binding.rvMessages.smoothScrollToPosition(messageAdapter.itemCount)
            }
        }

        chatViewModel.categoriesLiveData.observe(this) {
            categories = it
        }


    }

    private fun showToast(
        mConversationId: Long?,
        mRecipientName: String?,
        mRecipientId: Long?,
        mRecipientUrl: String?,
        mRecepientLastOnline: String?,
        mCategory: String?,
        mMessage: String?
    ) {

        if (mConversationId != null && mRecipientName != null &&
            mRecipientId != null &&
            mRecipientUrl != null &&
            mRecepientLastOnline != null &&
            mCategory != null &&
            mMessage != null
        ) {

            val dialog: AlertDialog
            val bin: NotificationBinding = NotificationBinding.inflate(layoutInflater)



            val image = bin.bigIcon

            val generatedAvatar = mRecipientName.let {
                AvatarGenerator.AvatarBuilder(this)
                    .setLabel(it)
                    .setAvatarSize(44)
                    .setTextSize(10)
                    .toSquare()
                    .setBackgroundColor(Color.rgb(173, 214, 237))
                    .build()
            }


            if (mRecipientUrl.isNotEmpty()) {
                Picasso.get()
                    .load(mRecipientUrl).fit()
                    .placeholder(generatedAvatar)
                    .error(generatedAvatar)
                    .transform(RoundedCornersTransformation(20, 0))
                    .into(image)

            } else {
                val transformAvatar =
                    RoundedCornersTransformation(7, 0).transform(generatedAvatar.toBitmap())
                image.setImageBitmap(transformAvatar)
            }
            val title = bin.contentTitle
            title.text = mRecipientName

            val text = bin.contentText
            text.text = mMessage.cut(25)


            dialog = AlertDialog.Builder(this, R.style.MessageAlertDialogTheme)
                .setView(bin.root)
                .create()

            dialog.window?.attributes?.gravity = Gravity.TOP or Gravity.FILL_HORIZONTAL
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window!!.attributes)
            layoutParams.width = getWidth(this)
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.windowAnimations = R.anim.slide_down
            dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            //bin.root.animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_down)
            dialog.show()

            dialog.window!!.attributes = layoutParams


            val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(this, defaultSoundUri)
            r.play()

            bin.notificationButton.setOnClickListener {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("CONVERSATION_ID", mConversationId)
                intent.putExtra("RECIPIENT_NAME", mRecipientName)
                intent.putExtra("RECIPIENT_ID", mRecipientId)
                intent.putExtra("RECIPIENT_URl", mRecipientUrl)
                intent.putExtra("RECIPIENT_LAST_ONLINE", mRecepientLastOnline)
                intent.putExtra("CATEGORY", mCategory)
                this.startActivity(intent)
                finish()
                dialog.dismiss()

            }
        }
    }

    fun getWidth(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }



    private fun attach() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        findViewById<ImageView>(R.id.btn_attach_image).setOnClickListener {
            this.typeOfMessage = TYPE_OF_MESSAGE.IMAGE
            attachImage()
        }
        findViewById<ImageView>(R.id.btn_attach_file).setOnClickListener {
            this.typeOfMessage = TYPE_OF_MESSAGE.DOCUMENT
            Toast.makeText(this, "attach Document", Toast.LENGTH_SHORT).show()
            attachFile()
        }
        findViewById<ImageView>(R.id.btn_attach_location).setOnClickListener {
            this.typeOfMessage = TYPE_OF_MESSAGE.LOCATION
            Toast.makeText(this, "attach Location", Toast.LENGTH_SHORT).show()
            attachLocation()
        }

    }


    private fun attachImage() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        gallery.type = "image/*"
        launcherImage.launch(gallery)

    }

    private fun attachFile() {
        launcherDocument.launch("*/*")
    }

    @SuppressLint("MissingPermission")
    private fun attachLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }

        val tack = fusedLocationClient.lastLocation
        tack.addOnSuccessListener {
            if (it != null) {
                if (typeOfMessage == TYPE_OF_MESSAGE.LOCATION) {
                    onSubmit(
                        "Location",
                        TYPE_OF_MESSAGE.LOCATION,
                        null,
                        it.latitude.toString(),
                        it.longitude.toString()
                    )
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    typeOfMessage = TYPE_OF_MESSAGE.TEXT
                }
            }
        }
    }

    override fun imageClicked(item: MessageModel) {

        if (item.uri != null) {
            val uri = Uri.parse(item.uri?.replace("file://", "content://"))
            val fileException = MimeTypeMap.getFileExtensionFromUrl(item.uri)
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileException)
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(uri, mime)
            var builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivity(intent);

        }


    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun documentClicked(item: MessageModel) {
        if (item.uri != null) {
            val uri = Uri.parse(item.uri?.replace("file://", "content://"))
            val fileException = MimeTypeMap.getFileExtensionFromUrl(item.uri)
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileException)
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(uri, mime)
            var builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivity(intent);
        }
    }

    override fun locationClicked(item: MessageModel) {
            val gmmIntentUri =
                Uri.parse("http://maps.google.com/maps?q=${item.latitude},${item.longitude}(${item.body})&iwloc=A&hl=es")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
    }

    override fun putUri(item: MessageModel) {
        chatViewModel.updateUri(item)
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
        val recipientAvatar = inflater.inflate(R.layout.avatar_tollbar_chat, null)
        supportActionBar?.customView = recipientAvatar

        if (recipientName != null && urlImg != null && lastOnline != null) {
            addRecipientData(recipientName!!, urlImg!!, lastOnline!!)
        }

        super.onStart()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(messageReceiver, IntentFilter("MyData"))

    }


    private fun addRecipientData(
        secondPartyUsername: String,
        imgUrl: String,
        date: String
    ) {


        val avatarView = findViewById<ImageView>(R.id.avatar_toolbar)
        val textNameView = findViewById<TextView>(R.id.tv_username_toolbar)
        val textOnlineView = findViewById<TextView>(R.id.tv_online_toolbar)

        val generatedAvatar = AvatarGenerator.AvatarBuilder(this)
            .setLabel(secondPartyUsername.uppercase())
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
                .transform(RoundedCornersTransformation(20, 0))
                .into(avatarView);
        } else {
            val transformAvatar =
                RoundedCornersTransformation(7, 0).transform(generatedAvatar.toBitmap())
            avatarView.setImageBitmap(transformAvatar)
        }

        supportActionBar?.subtitle = null
        supportActionBar?.title = null

        textNameView.text = secondPartyUsername.cut(15)
        textOnlineView.text = "Last seen" + date.toTime()
    }


    private fun onSubmit(
        input: String,
        type: TYPE_OF_MESSAGE,
        uri: Uri?,
        latitude: String,
        longitude: String
    ): Boolean {
        chatViewModel.sendMessage(
            conversationId,
            preferences.userDetails.id,
            recipientId,
            input,
            type,
            uri,
            latitude, longitude
        )
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }

            R.id.menu_vertical -> {
                if (categories?.isNotEmpty() == true) {
                    popupmenu = PopupMenu(
                        this@ChatActivity,
                        binding.shadowView,
                        Gravity.RIGHT,
                        0,
                        R.style.AppStyle_PopupMenu
                    )
                    categories!!.forEachIndexed { index, element ->
                        if (element.categoryName == category) {
                            if (category == this.getString(R.string.all_contacts)) {
                                popupmenu.menu.add(1, index, 0, "None").isChecked = true
                            } else {
                                popupmenu.menu.add(1, index, index, category).isChecked = true
                            }
                        } else {
                            if (element.categoryName == this.getString(R.string.all_contacts)) {
                                popupmenu.menu.add(1, index, index, "None")
                            } else {
                                popupmenu.menu.add(1, index, index, element.categoryName)
                            }
                        }
                    }
                    popupmenu.menu.setGroupCheckable(1, true, true)

                    popupmenu.setOnMenuItemClickListener { item ->
                        Toast.makeText(
                            this@ChatActivity,
                            "You Clicked : " + item.title,
                            Toast.LENGTH_SHORT
                        ).show()
                            item.isChecked = true
                            category = if (item.title.toString() == "None") {
                                chatViewModel.chooseCategory(this.getString(R.string.all_contacts), conversationId)
                            } else {
                                chatViewModel.chooseCategory(item.title.toString(), conversationId)
                            }

                        true
                    }
                    popupmenu.show()
                }
            }
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
                Toast.makeText(this@ChatActivity, "looking for $query", Toast.LENGTH_LONG)
                    .show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    messageAdapter.filter.filter(newText)
                    binding.rvMessages.scrollToPosition(messageAdapter.itemCount)
                    binding.rvMessages.smoothScrollToPosition(messageAdapter.itemCount)
                    binding.rvMessages.clearAnimation()
                } catch (e: Exception) {

                }
                return true
            }

        })
        return true
    }

    @SuppressLint("Range")
    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result =
                        cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }


}




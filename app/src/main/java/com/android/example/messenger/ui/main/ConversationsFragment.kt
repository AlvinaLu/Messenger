package com.android.example.messenger.ui.main

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.example.messenger.ChatterApp
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.databinding.FragmentConversationsBinding
import com.android.example.messenger.models.CategoryModel
import com.android.example.messenger.models.ConversationModel
import com.android.example.messenger.models.MessageModel
import com.android.example.messenger.models.TYPE_OF_MESSAGE
import com.android.example.messenger.ui.chat.ChatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.*

class ConversationsFragment() : Fragment(), ConversationAdapter.Interaction, ConversationPinnedAdapter.InteractionPinned, CategoriesAdapter.InteractionCategories {

    private var _binding: FragmentConversationsBinding? = null
    private val binding get() = _binding!!
    lateinit var conversationAdapter: ConversationAdapter
    lateinit var conversationPinnedAdapter: ConversationPinnedAdapter
    lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var activity: MainActivity
    private lateinit var preferences: AppPreferences
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var customAlertDialogView : View



    private val conversationViewModel: ConversationsViewModel by activityViewModels {
        ConversationViewModelFactory(
            (requireActivity().applicationContext as ChatterApp).conversationsRepository,
            (requireActivity().applicationContext as ChatterApp).messagesRepository,
            (requireActivity().applicationContext as ChatterApp).categoryRepository
        )
    }

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.extras?.getLong("unread") == activity.preferences.userDetails.id) {
                val newMessage =
                    MessageModel(
                        intent.extras?.getLong("id")!!,
                        intent.extras?.getLong("senderId")!!,
                        intent.extras?.getLong("recipientId")!!,
                        intent.extras?.getLong("conversationId")!!,
                        intent.extras?.getString("bodyMessage").toString(),
                        intent.extras?.getString("messageCreatedAt").toString(),
                        intent.extras?.getLong("unread")!!,
                        TYPE_OF_MESSAGE.valueOf(intent.extras?.getString("type").toString()),
                        intent.extras?.getString("url").toString(), null,   UUID.randomUUID().toString(),
                        intent.extras?.getString("latitude").toString(),
                        intent.extras?.getString("longitude").toString(),
                    )
                conversationViewModel.addMessage(newMessage)
                val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(activity, defaultSoundUri)
                r.play()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConversationsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = getActivity() as MainActivity
        conversationAdapter = ConversationAdapter(this)
        conversationPinnedAdapter = ConversationPinnedAdapter(this)
        categoriesAdapter = CategoriesAdapter(this)

        conversationViewModel.addCategory("All contacts")

        with(binding.rvConversations) {
            adapter = conversationAdapter
            layoutManager = LinearLayoutManager(activity)
        }
        with(binding.rvPinned){
            adapter = conversationPinnedAdapter
            layoutManager = GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)
        }
        with(binding.rvGroup){
            adapter = categoriesAdapter
            layoutManager = LinearLayoutManager(activity,  LinearLayoutManager.HORIZONTAL, false)
        }

        preferences = AppPreferences.create(activity)
        conversationViewModel.listLoadedLiveData.observe(viewLifecycleOwner) { dataStatus ->
            when (dataStatus) {
                Status.NOT_LOADED -> {
                    binding.rvConversations.visibility = View.INVISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.llGroup.visibility = View.GONE
                }
                Status.EMPTY -> {
                    binding.rvConversations.visibility = View.INVISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.note.text = "You have no active conversations."
                    binding.note.visibility = View.VISIBLE
                    binding.llGroup.visibility = View.GONE
                }
                Status.NOT_EMPTY -> {
                    binding.rvConversations.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.note.visibility = View.GONE
                    binding.llGroup.visibility = View.VISIBLE

                }
                else -> {

                }
            }

        }
        conversationViewModel.liveData.observe(viewLifecycleOwner) {
            conversationAdapter.swapData(it)
            conversationPinnedAdapter.swapData(it)
        }

        if(preferences.accessToken != null){
            conversationViewModel.fetchFromWeb()
        }

        binding.fabContacts.setOnClickListener {
            activity.showContactsScreen()
        }

        materialAlertDialogBuilder = MaterialAlertDialogBuilder(activity, R.style.AlertDialogTheme)

        conversationViewModel.categoriesLiveData.observe(viewLifecycleOwner){
            categoriesAdapter.swapData(it)
        }

        binding.imbtnAdd.setOnClickListener {
            customAlertDialogView = LayoutInflater.from(activity)
                .inflate(R.layout.alert_dialog, null, false)
            launchCustomAlertDialog()
        }

    }

    override fun onResume() {
        super.onResume()
        if(preferences.accessToken != null){
            conversationViewModel.fetchFromWeb()
        }
    }


    private fun launchCustomAlertDialog() {
        val editText = customAlertDialogView.findViewById<EditText>(R.id.et_add_category)

        materialAlertDialogBuilder.setView(customAlertDialogView)
            .setTitle("Add new category of message")
            .setNeutralButton("CANCEL"){ dialog, which ->

            }
            .setPositiveButton("SAVE"){ dialog, which ->
                val text = editText.text.trim().toString()
                if(text.length <1){
                    Snackbar.make(binding.root, "TextField is empty", Snackbar.LENGTH_SHORT).show()
                }else{
                    conversationViewModel.addCategory(text)
                }
                Snackbar.make(binding.root, "save", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    fun setActivity(activity: MainActivity) {
        this.activity = activity
    }

    private fun showNetworkError() {
        Snackbar.make(binding.root, "Data not loaded. Network error.", Snackbar.LENGTH_SHORT).show()
    }

    override fun itemClicked(item: ConversationModel) {
        conversationViewModel.changeItemUnreadCount(item)
        navigateToChat(
            recipientName = item.secondPartyUsername,
            conversationId = item.conversationId,
            recipientId = item.recipientId,
            url = item.imgUrl,
            lastOnline = item.lastOnline,
            category = item.category
        )
    }

    override fun itemUnPinned(item: ConversationModel) {
        conversationViewModel.pinConversationItem(item)
    }

    override fun itemPinned(item: ConversationModel) {
        conversationViewModel.pinConversationItem(item)
    }

    private fun navigateToChat(recipientName: String, conversationId: Long, recipientId: Long, url: String, lastOnline: String, category: String?) {
        val intent = Intent(activity, ChatActivity::class.java)
        intent.putExtra("CONVERSATION_ID", conversationId)
        intent.putExtra("RECIPIENT_NAME", recipientName)
        intent.putExtra("RECIPIENT_ID", recipientId)
        intent.putExtra("RECIPIENT_URl", url)
        intent.putExtra("RECIPIENT_LAST_ONLINE", lastOnline)
        intent.putExtra("CATEGORY", category)
        activity.startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        conversationViewModel.fetchFromWeb()
        LocalBroadcastManager.getInstance(activity)
            .registerReceiver(messageReceiver, IntentFilter("MyData"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(messageReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun categoryLongClicked(item: CategoryModel) {
        MaterialAlertDialogBuilder(activity, R.style.AlertDialogTheme )
            .setTitle("Do you really want to delete a category?")
            .setMessage("This category will be removed from all contacts.")
            .setNeutralButton("CANCEL"){ dialog, which ->

            }
            .setPositiveButton("OK"){ dialog, which ->
                conversationViewModel.deleteCategory(item.categoryName)
            }
            .show()

    }

    override fun categoryClicked(item: CategoryModel) {
        try {
            conversationAdapter.filter.filter(item.categoryName)
        } catch (_: Exception) {

        }
    }


}
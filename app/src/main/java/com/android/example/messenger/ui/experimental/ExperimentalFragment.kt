package com.android.example.messenger.ui.experimental

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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.ChatterApp
import com.android.example.messenger.R
import com.android.example.messenger.data.vo.UserVO
import com.android.example.messenger.ui.chat.ChatView
import com.android.example.messenger.ui.main.MainActivity
import com.android.example.messenger.utils.message.avatar.RoundedCornersTransformation
import com.avatarfirst.avatargenlib.AvatarGenerator
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList
import androidx.fragment.app.activityViewModels
import com.android.example.messenger.databinding.FragmentExperimentalBinding

class ExperimentalFragment: Fragment(), ExperimentalAdapter.Interaction {

    private var _binding: FragmentExperimentalBinding? = null
    private val binding get() = _binding!!

    lateinit var contactsAdapter: ExperimentalAdapter

    private val contactsViewModel: ExperimentalViewModel by activityViewModels {
        ExperimentalViewModelFactory((requireActivity().applicationContext as ChatterApp).repositoryDao)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding = FragmentExperimentalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contactsAdapter = ExperimentalAdapter(this)
        with(binding.rvContacts){
            adapter = contactsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
        contactsViewModel.listLoadedLiveData.observe(viewLifecycleOwner){ dataStatus ->
            when(dataStatus) {
                Status.NOT_LOADED -> {
                    binding.rvContacts.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }
                Status.EMPTY->{
                    binding.rvContacts.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.note.text = "Lis is Empty"
                    binding.note.visibility = View.VISIBLE
                }
                Status.NOT_EMPTY -> {
                    binding.rvContacts.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.note.visibility = View.GONE

                }else -> {

                }
            }

        }
        contactsViewModel.liveData.observe(viewLifecycleOwner){
            contactsAdapter.swapData(it)
        }

        contactsViewModel.fetchFromWeb()



    }

    override fun itemClicked(item: ContactsModel) {
        navigateToChat(recipientName = item.username, recipientId = item.id, imgUrl = item.imgUrl, lastOnline = item.lastOnline, null)
    }

     private fun navigateToChat(recipientName: String, recipientId: Long, imgUrl: String, lastOnline: String, conversationId: Long? ) {
            val intent = Intent(activity, ExperimentalActivity::class.java)
            intent.putExtra("RECIPIENT_ID", recipientId)
            intent.putExtra("RECIPIENT_NAME", recipientName)
            intent.putExtra("RECIPIENT_URl", imgUrl)
            intent.putExtra("RECIPIENT_LAST_ONLINE", lastOnline)

            activity?.startActivity(intent)
    }


}
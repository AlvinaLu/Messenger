package com.android.example.messenger.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.example.messenger.ChatterApp
import androidx.fragment.app.activityViewModels
import com.android.example.messenger.databinding.FragmentContactsBinding
import com.android.example.messenger.models.ContactsModel
import com.android.example.messenger.ui.chat.ChatActivity

class ContactsFragment: Fragment(), ContactAdapter.Interaction {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    lateinit var contactsAdapter: ContactAdapter
    private lateinit var activity: MainActivity

    private val contactsViewModel: ContactsViewModel by activityViewModels {
        ExperimentalViewModelFactory((requireActivity().applicationContext as ChatterApp).contactsRepository)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contactsAdapter = ContactAdapter(this)
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
                    binding.note.text = "You have no active contacts."
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

    fun setActivity(activity: MainActivity){
        this.activity = activity
    }

    override fun itemClicked(item: ContactsModel) {
        navigateToChat(recipientName = item.username, recipientId = item.id, imgUrl = item.imgUrl, lastOnline = item.lastOnline, null)
    }

     private fun navigateToChat(recipientName: String, recipientId: Long, imgUrl: String, lastOnline: String, conversationId: Long? ) {
            val intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra("RECIPIENT_ID", recipientId)
            intent.putExtra("RECIPIENT_NAME", recipientName)
            intent.putExtra("RECIPIENT_URl", imgUrl)
            intent.putExtra("RECIPIENT_LAST_ONLINE", lastOnline)
            activity?.startActivity(intent)
    }



}
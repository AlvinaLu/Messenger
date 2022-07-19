package com.android.example.messenger.ui.main

import com.android.example.messenger.data.vo.Conversation
import com.android.example.messenger.data.vo.ConversationListVO
import com.android.example.messenger.data.vo.MessageVO
import com.android.example.messenger.data.vo.UserListVO
import com.android.example.messenger.ui.chat.AdapterMessages
import com.example.messenger.utils.message.Message

class MainPresenterImpl(val view: MainView) : MainPresenter, MainInteractor.OnConversationsLoadFinishedListener,
    MainInteractor.OnContactsLoadFinishedListener, MainInteractor.OnLogoutFinishedListener {

    private val interactor: MainInteractor = MainInteractorImpl(view.getContext())
    private val map = HashMap<Long, Int>()

    override fun addMessageReceive(conversationID: Long, message: MessageVO) {
        val conversationsFragment = view.getConversationsFragment()
        val conversations = conversationsFragment.conversations
        val adapter = conversationsFragment.conversationsAdapter

        adapter.notifyDataSetChanged()

        conversations.forEachIndexed { index, it ->
            if( it.conversationId == conversationID){
                it.messages.add(message)
                adapter.notifyItemInserted(index)
                adapter.notifyDataSetChanged()
            }

        }
    }



    override fun onConversationsLoadSuccess(conversationsListVo: ConversationListVO) {
        /*
         * Checking if currently logged in user has active conversations.
         */
        if (conversationsListVo.conversations.isNotEmpty()) {
            val conversationsFragment = view.getConversationsFragment()
            val conversations = conversationsFragment.conversations
            val adapter = conversationsFragment.conversationsAdapter

            conversations.clear()
            adapter.notifyDataSetChanged()


            conversationsListVo.conversations.forEach { it ->
                conversations.add(Conversation(it.conversationId,
                    it.secondPartyUsername,
                    it.imgUrl,
                    it.lastOnline,
                    it.messages,
                ))
                adapter.notifyItemInserted(conversations.size - 1)
            }
        } else {
            view.showNoConversations()
        }
    }

    override fun onConversationsLoadError() {
        view.showConversationsLoadError()
    }

    override fun onContactsLoadSuccess(userListVO: UserListVO) {
        val contactsFragment = view.getContactsFragment()
        val contacts = contactsFragment.contacts
        val adapter = contactsFragment.contactsAdapter

        /*
         * Clear previously loaded contacts in contacts list
         * and notify adapter pf data set change.
         */
        contacts.clear()
        adapter.notifyDataSetChanged()

        /*
         * Add each contact retrieved from API to ContactsFragment's
         * contacts list.
         * Contacts adapter is notified after every item addition.
         */
        userListVO.users.forEach { contact ->
            contacts.add(contact)
            contactsFragment.contactsAdapter.notifyItemInserted(contacts.size - 1)
        }
    }

    override fun onContactsLoadError() {
        view.showContactsLoadError()
    }

    override fun onLogoutSuccess() {
        view.navigateToLogin()
    }

    override fun loadConversations() {
        interactor.loadConversations(this)
    }

    override fun loadContacts() {
        interactor.loadContacts(this)
    }

    override fun executeLogout() {
        interactor.logout(this)
    }
}
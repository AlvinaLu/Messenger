package com.android.example.messenger.ui.main

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import com.android.example.messenger.R
import android.view.*
import androidx.appcompat.widget.Toolbar
import com.android.example.messenger.LoginActivity
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.ui.experimental.ExperimentalActivity
import com.android.example.messenger.ui.setting.SettingsActivity

class MainActivity : AppCompatActivity(), MainView {

    private lateinit var llContainer: LinearLayout
    lateinit var presenter: MainPresenter
    private var toolbar : Toolbar? = null

    private val contactsFragment = ContactsFragment()
    private val conversationsFragment = ConversationsFragment()
    lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter = MainPresenterImpl(this)

        conversationsFragment.setActivity(this@MainActivity)
        contactsFragment.setActivity(this@MainActivity)


        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayUseLogoEnabled(true);
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_chevronleft)
        supportActionBar?.elevation = 0F

        preferences = AppPreferences.create(this)

        bindViews()
        showConversationsScreen()
    }

    override fun bindViews() {
        llContainer = findViewById(R.id.ll_container)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun showConversationsLoadError() {
        Toast.makeText(
            this, "Unable to load conversations. Try again later.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun showContactsLoadError() {
        Toast.makeText(
            this, "Unable to load contacts. Try again later.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onStart() {
        super.onStart()
        presenter.loadContacts()
    }

    override fun showConversationsScreen() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.ll_container, conversationsFragment)
        fragmentTransaction.commit()

        presenter.loadConversations()

        supportActionBar?.title = "Chatter"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

    }

    override fun showContactsScreen() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.ll_container, contactsFragment)
        fragmentTransaction.commit()
        presenter.loadContacts()

        supportActionBar?.title = "Contacts"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0F
    }

    override fun showNoConversations() {
        Toast.makeText(this, "You have no active conversations.", Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> showConversationsScreen()
            R.id.action_settings -> navigateToSettings()
            R.id.action_experimental-> navigateToExperimental()
            R.id.action_logout -> presenter.executeLogout()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getContext(): Context {
        return this
    }


    override fun getContactsFragment(): ContactsFragment {
        return contactsFragment
    }


    override fun getConversationsFragment(): ConversationsFragment {
        return conversationsFragment
    }

    override fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun navigateToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun navigateToExperimental() {
        startActivity(Intent(this, ExperimentalActivity::class.java))
    }



}
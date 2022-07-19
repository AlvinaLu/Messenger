package com.android.example.messenger.ui.experimental

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.android.example.messenger.LoginActivity
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.ui.main.ContactsFragment
import com.android.example.messenger.ui.main.ConversationsFragment
import com.android.example.messenger.ui.main.MainPresenter
import com.android.example.messenger.ui.setting.SettingsActivity
import java.util.*


class ExperimentalActivity : AppCompatActivity() {

    private lateinit var llContainer: LinearLayout
    private var toolbar : Toolbar? = null

    private val contactsFragment = ExperimentalFragment()

    lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayUseLogoEnabled(true);
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_chevronleft)
        supportActionBar?.elevation = 0F

        preferences = AppPreferences.create(this)

        bindViews()
        showContactsScreen()
    }

     fun bindViews() {
        llContainer = findViewById(R.id.ll_container)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }






     fun showContactsScreen() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.ll_container, contactsFragment)
        fragmentTransaction.commit()

        supportActionBar?.title = "Contacts"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0F
    }

     fun showNoConversations() {
        Toast.makeText(this, "You have no active conversations.", Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.action_settings -> navigateToSettings()
        }
        return super.onOptionsItemSelected(item)
    }

   fun getContext(): Context {
        return this
    }



     fun navigateToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }


}
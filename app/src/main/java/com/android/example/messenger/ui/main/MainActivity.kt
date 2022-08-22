package com.android.example.messenger.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.android.example.messenger.ChatterApp
import com.android.example.messenger.LoginActivity
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.databinding.ActivityMainBinding
import com.android.example.messenger.services.CheckOnlineService
import com.android.example.messenger.ui.setting.SettingsActivity


class MainActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var conversationsFragment = ConversationsFragment()
    private var contactsFragment = ContactsFragment()
    lateinit var preferences: AppPreferences
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.create(this)


        conversationsFragment.setActivity(this@MainActivity)
        contactsFragment.setActivity(this@MainActivity)


        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayUseLogoEnabled(true);
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_chevronleft)
        supportActionBar?.elevation = 0F

        mainViewModel = ViewModelProvider(
            this, MainViewModel.MainViewModelFactory(
                (this.applicationContext as ChatterApp).conversationsRepository,
                (this.applicationContext as ChatterApp).contactsRepository,
                (this.applicationContext as ChatterApp).messagesRepository,
                (this.applicationContext as  ChatterApp).categoryRepository
            )
        )[MainViewModel::class.java]


        showConversationsScreen()
    }

    private fun goToLogin() {
        val login = Intent(this, LoginActivity::class.java)
        login.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(login)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }


    fun showConversationsScreen() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        conversationsFragment?.let { fragmentTransaction.replace(R.id.ll_container, it) }
        fragmentTransaction.commit()
        supportActionBar?.title = "Chatter"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
    fun showContactsScreen() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        contactsFragment?.let { fragmentTransaction.replace(R.id.ll_container, it) }
        fragmentTransaction.commit()
        supportActionBar?.title = "Contacts"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0F
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> showConversationsScreen()
            R.id.action_settings -> navigateToSettings()
            R.id.action_logout -> {
                mainViewModel.logOut()
                preferences.clear()
                goToLogin()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onStart() {
        super.onStart()
        showConversationsScreen()
        if (preferences.accessToken == null){goToLogin()}
        val intent = Intent(this, CheckOnlineService::class.java)
        intent.putExtra("token", preferences.accessToken as String)
        this.startService(intent)
    }

    override fun onStop() {
        super.onStop()
        this.stopService(Intent(this, CheckOnlineService::class.java))
    }
    private fun navigateToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }
}
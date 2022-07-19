package com.android.example.messenger.ui.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.ui.main.MainActivity


class SignUpActivity : AppCompatActivity(), SignUpView, View.OnClickListener {

    private lateinit var etUsername: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var presenter: SignUpPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        presenter = SignUpPresenterImpl(this)
        presenter.preferences = AppPreferences.create(this)
        supportActionBar?.hide()
        bindViews()
    }

    override fun bindViews() {
        etUsername = findViewById(R.id.et_username)
        etPhoneNumber = findViewById(R.id.et_phone)
        etPassword = findViewById(R.id.et_password)
        btnSignUp = findViewById(R.id.btn_sign_up)
        progressBar = findViewById(R.id.progress_bar)
        btnSignUp.setOnClickListener(this)
    }

    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    override fun navigateToHome() {
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_sign_up) {
            presenter.executeSignUp(etUsername.text.toString(),
                etPhoneNumber.text.toString(), etPassword.text.toString())
        }
    }

    override fun setUsernameError() {
        etUsername.error = "Username field cannot be empty"
    }

    override fun setUsernameTooLongError() {
        etUsername.error = "Username must be less then 20 letters"
    }

    override fun setPhoneNumberError() {
        etPhoneNumber.error = "Phone number field cannot be empty"
    }

    override fun setPasswordError() {
        etPassword.error = "Password field cannot be empty"
    }

    override fun showAuthError() {
        Toast.makeText(this, "An authorization error occurred. Please try again later.",
            Toast.LENGTH_LONG).show()
    }

    override fun showSignUpError() {
        Toast.makeText(this, "An unexpected error occurred. Please try again later.",
            Toast.LENGTH_LONG).show()
    }

    override fun getContext(): Context {
        return this
    }
}
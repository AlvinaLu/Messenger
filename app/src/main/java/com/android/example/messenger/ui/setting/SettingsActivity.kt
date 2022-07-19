package com.android.example.messenger.ui.setting

import android.content.ClipData
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuView
import androidx.core.net.toUri
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.databinding.ActivitySettingBinding
import com.android.example.messenger.utils.message.avatar.CompressFile
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.util.*


class SettingsActivity : AppCompatActivity(), SettingView {

    lateinit var binding: ActivitySettingBinding
    lateinit var preferences: AppPreferences
    private lateinit var presenter: SettingPresenter
    private lateinit var toolbar: MaterialToolbar
    var imageUri: Uri? = null
    var photoUrl: String = ""
    var compressor = CompressFile()

    private val imageView: ImageView by lazy {
        findViewById<ImageView>(R.id.firebaseImage)
    }
    private val textView: TextView by lazy {
        findViewById<TextView>(R.id.text_setting)
    }

    private val selectPictureLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uriToFile(this, uri, "select_image_from_gallery")?.let { file ->
                compressor.compressImage(file.absolutePath, 0.5)


                setImage(imageView, file.absolutePath)

                imageUri = file.toUri()

                uploadImage()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = SettingPresenterImpl(this)



        setSupportActionBar(binding.topAppBar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayUseLogoEnabled(true);
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_chevronleft)
        supportActionBar?.elevation = 0F
        binding.topAppBar.elevation = 0F
        binding.appBar.elevation = 0F
        binding.toolbarLayout.elevation =0F

        binding.topAppBar.setNavigationOnClickListener {

            onBackPressed()

        }


        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {
                    Toast.makeText(this, "Delete", Toast.LENGTH_LONG).show()
                    true
                }
                R.id.action_logout -> {
                    Toast.makeText(this, "Log out", Toast.LENGTH_LONG).show()
                    true
                }
                else -> false
            }
        }



        preferences = AppPreferences.create(this)
        checkImageExist()
        binding.textSetting.text = preferences.userDetails.notificationToken


        binding.toolbarLayout.isTitleEnabled = true
        binding.toolbarLayout.title = preferences.userDetails.username

        binding.fabAddImage.setOnClickListener {

            selectPictureLauncher.launch("image/*")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_setting, menu)
        binding.appBar.addOnOffsetChangedListener(OnOffsetChangedListener{appBar, verticalOffset ->
            if (verticalOffset >= 0) {
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_chevronleft_white)
                menu?.getItem(0)?.setIcon(R.drawable.ic_menuvertical_white)
            } else {
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_chevronleft)
                menu?.getItem(0)?.setIcon(R.drawable.ic_menuvertical)
            }
        })
        return true
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun checkImageExist() {
        val url = preferences.userDetails.imgUrl
        if (url != null && url.isNotEmpty()) {
            Picasso.get().load(url)
                .error(R.drawable.ic_chevronleft)
                .into(binding.firebaseImage)
        }
    }


    private fun uploadImage() {
        showProgress()
        val storageReference = FirebaseStorage.getInstance().reference
        val path = storageReference.child("/images").child(UUID.randomUUID().toString())
        imageUri?.let {
            path.putFile(it)
                .addOnSuccessListener {
                    path.downloadUrl.addOnCompleteListener {
                        if (it.isSuccessful) {
                            photoUrl = it.result.toString()
                            if (photoUrl.isNotEmpty() && photoUrl != null) {
                                saveImgUrl(photoUrl)
                            }
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Unsuccessful upload", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun showProgress() {
        binding.progressBarSetting.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBarSetting.visibility = View.GONE
    }

    override fun showSaveImgError() {
        hideProgress()
        Toast.makeText(this, "Unsuccessful upload to server", Toast.LENGTH_LONG).show()
    }

    override fun saveImgSuccess() {
        hideProgress()
        Toast.makeText(this, "Successful upload to server", Toast.LENGTH_LONG).show()
    }

    override fun saveImgUrl(url: String) {
        presenter.sendUrl(url)
    }

    override fun bindViews() {
        TODO("Not yet implemented")
    }

    override fun getContext(): Context {
        return this
    }

    private fun setImage(imageView: ImageView, filePath: String) {
        Glide.with(imageView.context).asBitmap().load(filePath).skipMemoryCache(true)
            .diskCacheStrategy(
                DiskCacheStrategy.NONE
            ).into(imageView)
    }


    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("temp_image", ".jpg", storageDir)
    }

    private fun uriToFile(context: Context, uri: Uri, fileName: String): File? {
        context.contentResolver.openInputStream(uri)?.let { inputStream ->
            val tempFile: File = createImageFile()
            val fileOutputStream = FileOutputStream(tempFile)
            inputStream.copyTo(fileOutputStream)
            inputStream.close()
            fileOutputStream.close()
            return tempFile
        }
        return null
    }

}

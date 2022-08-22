package com.android.example.messenger.ui.chat

import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.databinding.MessageItemImageBinding
import com.android.example.messenger.models.MessageModel
import com.android.example.messenger.utils.message.toTime
import com.android.example.messenger.utils.message.toTimeMessage
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Cache
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.util.concurrent.Executors

class ImageViewHolder(
    private val binding: MessageItemImageBinding,
    private val interaction: MessageAdapter.Interaction?,
) :
    RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    var block: ConstraintLayout? = null
    var time: TextView? = null
    var imageView: ImageView? = null
    var imageViewTmp: ImageView? = null
    var progress: ProgressBar? = null

    val preferences: AppPreferences = AppPreferences.create(binding.root.context)

    private lateinit var storedItem: MessageModel

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        interaction?.imageClicked(storedItem)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun bind(item: MessageModel) {
        storedItem = item
        if (item.senderId == preferences.userDetails.id) {
            block = binding.blocUserMessageSent
            time = binding.chatUserMessageTimeSent
            imageView = binding.userImageSent
            imageViewTmp = binding.userImageTmpSent
            progress = binding.progressBarSent

            binding.blocUserMessageReceive.visibility = View.GONE
            binding.userImageReceive.visibility = View.GONE
            binding.userImageTmpReceive.visibility = View.GONE
            binding.chatUserMessageTimeReceive.visibility = View.GONE
            binding.progressBarReceive.visibility = View.GONE

        } else if (item.recipientId == preferences.userDetails.id) {
            block = binding.blocUserMessageReceive
            time = binding.chatUserMessageTimeReceive
            imageView = binding.userImageReceive
            imageViewTmp = binding.userImageTmpReceive
            progress = binding.progressBarReceive


            binding.blocUserMessageSent.visibility = View.GONE
            binding.userImageSent.visibility = View.GONE
            binding.chatUserMessageTimeSent.visibility = View.GONE
            binding.userImageTmpSent.visibility = View.GONE
            binding.progressBarSent.visibility = View.GONE
        }

        if (item.uri == null && item.url.isEmpty()) {
            imageView!!.visibility = View.GONE
            imageViewTmp!!.visibility = View.VISIBLE
            progress!!.visibility = View.VISIBLE
        } else if (item.uri == null && item.url.isNotEmpty()) {
            imageViewTmp!!.visibility = View.VISIBLE
            progress!!.visibility = View.VISIBLE
            Picasso.get()
                .load(item.url)
                .resize(700, 0)
                .noPlaceholder()
                .error(R.drawable.image_sent)
                .into(imageView, object: Callback {
                    override fun onSuccess() {
                        imageView!!.visibility = View.VISIBLE
                        imageViewTmp!!.visibility = View.GONE
                        progress!!.visibility = View.GONE
                        block!!.setOnClickListener {
                            loadImage(item)
                        }
                    }
                    override fun onError(e: java.lang.Exception?) {
                        Toast.makeText(
                            binding.root.context,
                            "Unsuccessful loading",
                            Toast.LENGTH_LONG
                        ).show()
                        item.body = "Unsuccessful upload"
                        progress!!.visibility = View.GONE
                    }
                })
        } else if (item.uri != null) {
            progress!!.visibility = View.GONE
            imageViewTmp!!.visibility = View.VISIBLE
            progress!!.visibility = View.VISIBLE
            Picasso.get()
                .load(Uri.parse(item.uri?.replace("file://", "content://")))
                .resize(700, 0)
                .noPlaceholder()
                .error(R.drawable.image_sent)
                .into(imageView, object: Callback {
                    override fun onSuccess() {
                        imageView!!.visibility = View.VISIBLE
                        imageViewTmp!!.visibility = View.GONE
                        progress!!.visibility = View.GONE
                    }

                    override fun onError(e: java.lang.Exception?) {
                        Toast.makeText(
                            binding.root.context,
                            "Unsuccessful loading",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            block!!.setOnClickListener {
                interaction?.imageClicked(item)
                Toast.makeText(
                    binding.root.context,
                    "Image clicked",
                    Toast.LENGTH_LONG
                ).show()
            }

        }


        block!!.visibility = View.VISIBLE
        time!!.visibility = View.VISIBLE
        time!!.text = item.createdAt.toTimeMessage()

    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun loadImage(item: MessageModel) {
        val filePath = File(binding.root.context.filesDir,
            "images"
        )
        filePath.mkdir()
        var file = File(filePath.path, "${item.id}"+item.body)
        try {
            file.createNewFile()
            getFileFromStorage(file, item.url) {
                val newItem = item.copy()
                progress!!.visibility = View.GONE
                val uri = FileProvider.getUriForFile(binding.root.context, "com.android.example.messenger", file)
                newItem.uri = uri.toString()
                interaction?.putUri(newItem)
                Toast.makeText(
                    binding.root.context,
                    "Successful download",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e: Exception) {
            progress!!.visibility = View.GONE
                  Toast.makeText(
                binding.root.context,
                "UnSuccessful save",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun getFileFromStorage(mFile: File, fileUrl: String, function: () -> Unit) {
        val path = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl)
        path.getFile(mFile)
            .addOnSuccessListener { function() }
            .addOnFailureListener {
                progress!!.visibility = View.GONE
                Toast.makeText(
                    binding.root.context,
                    "UnSuccessful save",
                    Toast.LENGTH_LONG
                ).show()
            }.addOnProgressListener {
                progress!!.visibility = View.VISIBLE
                val prog: Double =
                    100.0 * it.bytesTransferred / it.totalByteCount
                progress!!.incrementProgressBy(prog.toInt())
            }
    }


}

object ImageHandler {
    private var instance: Picasso? = null
    fun getSharedInstance(context: Context): Picasso? {
        if (instance == null) {
            instance = Picasso.Builder(context).executor(Executors.newSingleThreadExecutor())
                .memoryCache(Cache.NONE).indicatorsEnabled(true).build()
        }
        return instance
    }
}


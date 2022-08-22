package com.android.example.messenger.ui.chat

import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.databinding.MessageItemDocumentBinding
import com.android.example.messenger.models.MessageModel
import com.android.example.messenger.utils.message.toTime
import com.android.example.messenger.utils.message.toTimeMessage
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class DocumentViewHolder(
    private val binding: MessageItemDocumentBinding,
    private val interaction: MessageAdapter.Interaction?,
) :
    RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    var block: ConstraintLayout? = null
    var text: TextView? = null
    var time: TextView? = null
    var button: ImageView? = null
    var progress: ProgressBar? = null

    private lateinit var storedItem: MessageModel
    val preferences: AppPreferences = AppPreferences.create(binding.root.context)

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
            text = binding.chatUserMessageSent
            time = binding.chatUserMessageTimeSent
            button = binding.btnDownloadSent
            progress = binding.progressBarSent
            binding.blocUserMessageReceive.visibility = View.GONE
            binding.chatUserMessageReceive.visibility = View.GONE
            binding.chatUserMessageTimeReceive.visibility = View.GONE
            binding.btnDownloadReceive.visibility = View.GONE
            binding.progressBarReceive.visibility = View.GONE
        } else if(item.recipientId == preferences.userDetails.id) {
            block = binding.blocUserMessageReceive
            text = binding.chatUserMessageReceive
            time = binding.chatUserMessageTimeReceive
            button = binding.btnDownloadReceive
            progress = binding.progressBarReceive
            binding.blocUserMessageSent.visibility = View.GONE
            binding.chatUserMessageSent.visibility = View.GONE
            binding.chatUserMessageTimeSent.visibility = View.GONE
            binding.btnDownloadSent.visibility = View.GONE
            binding.progressBarSent.visibility = View.GONE
        }

        if(item.uri == null && item.url.isEmpty()){
            progress!!.visibility = View.VISIBLE
        }
        else if (item.uri != null) {
            button!!.setImageDrawable(
                ContextCompat.getDrawable(
                    binding.root.context,
                    R.drawable.ic_documentfilled
                )
            )
            progress!!.visibility = View.GONE
            button!!.visibility = View.VISIBLE
            button!!.setOnClickListener {
                interaction?.documentClicked(item)
                Toast.makeText(
                    binding.root.context,
                    "Click",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        else if (item.url.isNotEmpty() && item.uri == null) {
            button!!.visibility = View.VISIBLE
            progress!!.visibility = View.GONE
            button!!.setOnClickListener {
                buttonClicked(item)
            }
        }
        block!!.visibility = View.VISIBLE
        text!!.text = item.body
        time!!.text = item.createdAt.toTimeMessage()
        text!!.visibility = View.VISIBLE
        time!!.visibility = View.VISIBLE


    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun buttonClicked(item: MessageModel) {
        val filePath = File(binding.root.context.filesDir,
            "files"
        )
        filePath.mkdir()
        val file = File(filePath.path, item.body)
        try {
            file.createNewFile()
            getFileFromStorage(file, item.url) {
                val newItem = item.copy()
                button!!.visibility = View.VISIBLE
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
                button!!.visibility = View.VISIBLE
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
                    button!!.visibility = View.VISIBLE
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




package com.android.example.messenger.ui.chat

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.databinding.*
import com.android.example.messenger.models.MessageModel
import com.android.example.messenger.models.TYPE_OF_MESSAGE
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList


class MessageAdapter(
    private val interaction: Interaction? = null
) :
    ListAdapter<MessageModel, RecyclerView.ViewHolder>(PoiModelDiffCallback()), Filterable {
    var mListRef: List<MessageModel>? = null
    var mFilteredList: List<MessageModel>? = null

    interface Interaction {
        fun imageClicked(item: MessageModel)
        fun documentClicked(item: MessageModel)
        fun locationClicked(item: MessageModel)
        fun putUri(item: MessageModel)
    }

    fun swapData(data: List<MessageModel>) {
            if (mListRef == null) {
                mListRef = data
            }
        submitList(data.toMutableList())
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MessageViewHolderFactory.getHolder(parent, viewType, interaction)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is DocumentViewHolder){
            holder.bind(getItem(position))
        }else if(holder is TextViewHolder){
            holder.bind(getItem(position))
        }else if(holder is ImageViewHolder) {
            holder.bind(getItem(position))
        } else if(holder is LocationViewHolder) {
            holder.bind(getItem(position))
        }
        else{
            throw IllegalStateException("Unexpected View Holder Class")
        }
    }


    class PoiModelDiffCallback() : DiffUtil.ItemCallback<MessageModel>() {
        override fun areItemsTheSame(oldItem: MessageModel, newItem: MessageModel): Boolean {
            //Confirm that your id variable matches this one or change this one to match the one in your model
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: MessageModel, newItem: MessageModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {

                val charString = charSequence.toString()

                if (charString.isEmpty()) {

                    mFilteredList = mListRef
                } else {
                    mListRef?.let {
                        val filteredList = arrayListOf<MessageModel>()
                        for (item in mListRef!!) {
                            if (item is MessageModel) {
                                if (charString.lowercase(Locale.ENGLISH) in item.body.lowercase(
                                        Locale.ENGLISH
                                    )
                                ) {
                                    filteredList.add(item)
                                }
                            }
                        }
                        mFilteredList = filteredList
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = mFilteredList
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                mFilteredList = filterResults.values as ArrayList<MessageModel>
                submitList(mFilteredList)
            }
        }
    }



}

class MessageViewHolderFactory {
    companion object {
        fun getHolder(
            parent: ViewGroup,
            viewType: Int,
            interaction: MessageAdapter.Interaction?,
        ): RecyclerView.ViewHolder {
             when (viewType) {
                TYPE_OF_MESSAGE.IMAGE.ordinal -> {
                    val binding: MessageItemImageBinding = MessageItemImageBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ),
                        parent,
                        false
                    )
                    return ImageViewHolder(binding, interaction)
                }
                TYPE_OF_MESSAGE.DOCUMENT.ordinal -> {
                    val binding: MessageItemDocumentBinding = MessageItemDocumentBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ),
                        parent,
                        false
                    )
                    return DocumentViewHolder(binding, interaction)
                }
                TYPE_OF_MESSAGE.LOCATION.ordinal -> {
                    val binding: MessageLocactionBinding= MessageLocactionBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ),
                        parent,
                        false
                    )
                    return LocationViewHolder(binding, interaction)
                }
                else -> {
                    val binding: MessageItemTextBinding = MessageItemTextBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ),
                        parent, false
                    )
                    return TextViewHolder(binding, interaction)
                }
            }

        }
    }

}




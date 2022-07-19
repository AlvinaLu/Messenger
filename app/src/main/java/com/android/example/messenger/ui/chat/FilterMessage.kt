package com.android.example.messenger.ui.chat

import android.widget.Filter
import com.android.example.messenger.data.vo.MessageVO
import com.example.messenger.utils.message.Message

class FilterMessage : Filter {

    private val filerList: ArrayList<MessageVO>
    private val adapterMessage: AdapterMessages

    constructor(filerList: ArrayList<MessageVO>, adapterMessage: AdapterMessages) : super() {
        this.filerList = filerList
        this.adapterMessage = adapterMessage
    }

    override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
        var constraint = constraint
        val results = Filter.FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filterModel: ArrayList<MessageVO> = ArrayList()
            for (i in 0 until filerList.size) {
                if (filerList[i].body.uppercase().contains(constraint)) {
                    filterModel.add(filerList[i])
                }
            }
            results.count = filterModel.size
            results.values = filterModel
        }else{
            results.count = filerList.size
            results.values = filerList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults) {
        adapterMessage.messagesList = results.values as ArrayList<MessageVO>

        adapterMessage.notifyDataSetChanged()


    }
}

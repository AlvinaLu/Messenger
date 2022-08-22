package com.android.example.messenger.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.example.messenger.R
import com.android.example.messenger.data.local.AppPreferences
import com.android.example.messenger.databinding.RowCategoriesBinding
import com.android.example.messenger.models.CategoryModel

class CategoriesAdapter(private val interaction: InteractionCategories? = null) :
    ListAdapter<CategoryModel, CategoriesAdapter.ConversationCategoryViewHolder>(
        PoiModelDiffCallback()
    ) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConversationCategoryViewHolder {
        return ConversationCategoryViewHolder.from(parent, interaction)
    }

    override fun onBindViewHolder(holder: ConversationCategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun swapData(data: List<CategoryModel>) {
        submitList(data.toMutableList())
    }


    class ConversationCategoryViewHolder(
        private val binding: RowCategoriesBinding,
        private val interaction: InteractionCategories?,
    ) :
        RecyclerView.ViewHolder(binding.root), View.OnLongClickListener, View.OnClickListener {

        val preferences: AppPreferences = AppPreferences.create(binding.root.context)

        lateinit var storedItem: CategoryModel

        init {
            itemView.setOnLongClickListener(this)
            itemView.setOnClickListener(this)
        }

        override fun onLongClick(v: View?): Boolean {
            if (storedItem.categoryName == binding.root.context.getString(R.string.all_contacts)) {
                Toast.makeText(
                    binding.root.context,
                    "Can't delete category All contacts",
                    Toast.LENGTH_LONG
                ).show()

            } else {
                interaction?.categoryLongClicked(storedItem)
                Toast.makeText(binding.root.context, "Delete category", Toast.LENGTH_LONG).show()
            }
            return true
        }

        override fun onClick(v: View?) {
            interaction?.categoryClicked(storedItem)
        }


        fun bind(item: CategoryModel) {
            storedItem = item
            val block = binding.category
            val text = binding.tvCategory
            if (item.categoryName == binding.root.context.getString(R.string.all_contacts)) {
                block.setCardBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.blue
                    )
                )
                text.text = binding.root.context.getString(R.string.all_contacts)
            } else {
                block.setCardBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.light_tone
                    )
                )
                text.text = item.categoryName
            }


        }


        companion object {
            fun from(
                parent: ViewGroup,
                interaction: InteractionCategories?
            ): ConversationCategoryViewHolder {
                val binding: RowCategoriesBinding = RowCategoriesBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ),
                    parent,
                    false
                )
                return ConversationCategoryViewHolder(binding, interaction)
            }
        }

    }

    interface InteractionCategories {
        fun categoryLongClicked(item: CategoryModel)
        fun categoryClicked(item: CategoryModel)
    }


    class PoiModelDiffCallback() : DiffUtil.ItemCallback<CategoryModel>() {
        override fun areItemsTheSame(oldItem: CategoryModel, newItem: CategoryModel): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: CategoryModel, newItem: CategoryModel): Boolean {
            return oldItem.categoryName == newItem.categoryName
        }

    }

}
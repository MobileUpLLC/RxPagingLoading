package ru.mobileup.rxplce.sample.paging

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item.view.*
import ru.mobileup.rxplce.sample.R

class ItemsAdapter(
    val footerView: View
) : RecyclerView.Adapter<ItemsAdapter.BaseViewHolder>() {

    companion object {
        private const val ITEM_TYPE = 1
        private const val FOOTER_TYPE = 2
    }

    private val asyncDiffer =
        AsyncListDiffer<Any>(
            this,
            object : DiffUtil.ItemCallback<Any>() {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return oldItem == newItem
                }

                @SuppressLint("DiffUtilEquals")
                override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return oldItem == newItem
                }
            })

    private val items get() = asyncDiffer.currentList

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return if (items.isNotEmpty() && position == (items.size - 1)) FOOTER_TYPE
        else ITEM_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            ITEM_TYPE -> ItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item, parent, false)
            )
            FOOTER_TYPE -> FooterViewHolder(footerView)
            else -> throw IllegalArgumentException("ViewType $viewType not implemented")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            holder.bind(items[position] as Item)
        }
    }

    fun submitList(list: List<Item>) {
        asyncDiffer.submitList(
            if (list.isEmpty()) {
                list
            } else {
                list.plus(Unit)
            }
        )
    }

    abstract class BaseViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView),
        LayoutContainer

    class ItemViewHolder(itemView: View) : BaseViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(item: Item) {
            containerView.text.text = "Item ${item.number}"
        }
    }

    class FooterViewHolder(itemView: View) : BaseViewHolder(itemView)

}
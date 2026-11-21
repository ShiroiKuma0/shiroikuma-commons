package org.fossify.commons.adapters

import org.fossify.commons.extensions.rippleDrawable
import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.fossify.commons.R
import org.fossify.commons.databinding.ItemSimpleListBinding
import org.fossify.commons.extensions.*
import org.fossify.commons.models.SimpleListItem

open class SimpleListItemAdapter(val activity: Activity, val onItemClicked: (SimpleListItem) -> Unit) :
    ListAdapter<SimpleListItem, SimpleListItemAdapter.SimpleItemViewHolder>(SimpleListItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleItemViewHolder {
        val view = activity.layoutInflater.inflate(R.layout.item_simple_list, parent, false)
        return SimpleItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleItemViewHolder, position: Int) {
        val route = getItem(position)
        holder.bindView(route)
    }

    open inner class SimpleItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemSimpleListBinding.bind(itemView)
        fun bindView(item: SimpleListItem) {
            setupSimpleListItem(binding, item, onItemClicked)
        }
    }

    private class SimpleListItemDiffCallback : DiffUtil.ItemCallback<SimpleListItem>() {
        override fun areItemsTheSame(oldItem: SimpleListItem, newItem: SimpleListItem): Boolean {
            return SimpleListItem.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: SimpleListItem, newItem: SimpleListItem): Boolean {
            return SimpleListItem.areContentsTheSame(oldItem, newItem)
        }
    }
}

fun setupSimpleListItem(view: ItemSimpleListBinding, item: SimpleListItem, onItemClicked: (SimpleListItem) -> Unit) {
    view.apply {
        // sk18: a sheet row reads the dialog's text role, the chosen one the accent; its ripple is built here
        val color = root.context.uiColor(if (item.selected) UiRole.ACCENT else UiRole.DIALOG_TEXT)
        if (root.background == null) root.background = root.context.rippleDrawable()

        bottomSheetItemTitle.setText(item.textRes)
        bottomSheetItemTitle.setTextColor(color)
        bottomSheetItemIcon.setImageResourceOrBeGone(item.imageRes)
        bottomSheetItemIcon.applyColorFilter(color)
        bottomSheetSelectedIcon.beVisibleIf(item.selected)
        bottomSheetSelectedIcon.applyColorFilter(color)

        root.setOnClickListener {
            onItemClicked(item)
        }
    }
}

/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui.bottomsheets

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hse.core.R
import com.hse.core.common.onClick
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_DATE_FROM_TO
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_HORIZONTAL_CHIPS
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_RANGE_PICKER
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_SIMPLE_CHECKBOX
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_SIMPLE_ITEM
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_SIMPLE_TITLED_ITEM
import com.innovattic.rangeseekbar.RangeSeekBar

open class BottomSheetAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val data = ArrayList<Item>()
    private val originalData = ArrayList<Item>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SIMPLE_TITLED_ITEM -> SimpleTitledItemHolder(parent)
            TYPE_HORIZONTAL_CHIPS -> HorizontalChipsHolder(parent)
            TYPE_RANGE_PICKER -> RangePickerHolder(parent)
            TYPE_DATE_FROM_TO -> DateFromToHolder(parent)
            TYPE_SIMPLE_CHECKBOX -> SimpleCheckboxHolder(parent)
            TYPE_SIMPLE_ITEM -> SimpleItemHolder(parent)
            else -> throw Exception("No view found")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SimpleTitledItemHolder -> {
                val item = data[position] as? Item.SimpleTitledItem ?: return
                holder.title.text = item.title
                holder.text.text = item.text
                holder.itemView.onClick { item.onClick.invoke(item, position) }
            }
            is HorizontalChipsHolder -> {
                val item = data[position] as? Item.HorizontalChips ?: return
                holder.recycler.adapter = item.adapter
                holder.title.text = item.title
            }
            is RangePickerHolder -> {
                val item = data[position] as? Item.RangePicker ?: return
                holder.title.text = item.title
                holder.picker.apply {
                    max = item.max - item.min
                    setMinThumbValue(item.currentMin - item.min)
                    setMaxThumbValue(item.currentMax - item.min)
                    holder.range.text = String.format(
                        item.rangeText,
                        item.currentMin,
                        item.currentMax
                    )

                    seekBarChangeListener = object : RangeSeekBar.SeekBarChangeListener {
                        override fun onStartedSeeking() {

                        }

                        override fun onStoppedSeeking() {

                        }

                        override fun onValueChanged(minThumbValue: Int, maxThumbValue: Int) {
                            item.listener.invoke(
                                item,
                                position,
                                minThumbValue + item.min,
                                maxThumbValue + item.min
                            )
                            holder.range.text = String.format(
                                item.rangeText,
                                minThumbValue + item.min,
                                maxThumbValue + item.min
                            )
                        }
                    }
                }
            }
            is DateFromToHolder -> {
                val item = data[position] as? Item.DateFromTo ?: return
                holder.dateFromText.title = item.titleFrom
                holder.dateToText.title = item.titleTo
                holder.listener = item.listener
                holder.setDateFrom(item.currentTimeFrom)
                holder.setDateTo(item.currentTimeTo)
            }
            is SimpleCheckboxHolder -> {
                val item = data[position] as? Item.SimpleCheckbox ?: return
                holder.text.text = item.text
                holder.checkbox.setImageResource(if (item.selected) R.drawable.ic_done_circle_blue_24dp else R.drawable.ic_checkbox_unchecked_24dp)
                holder.itemView.onClick {
                    item.selected = !item.selected
                    item.listener(item, position, item.selected)
                    notifyItemChanged(position)
                }
            }
            is SimpleItemHolder -> {
                val item = data[position] as? Item.SimpleItem ?: return
                holder.text.text = item.text
            }
        }
    }


    fun addItem(item: Item) {
        data.add(item)
        originalData.add(item)
    }

    fun filter(s: String?, predicate: (Item) -> Boolean) {
        data.clear()
        if (s.isNullOrEmpty()) {
            data.addAll(originalData)
        } else {
            originalData.forEach {
                if (predicate(it)) data.add(it)
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = data[position].type
    override fun getItemCount() = data.size
}
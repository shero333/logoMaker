package com.esport.logo.maker.unlimited.main.edit_create_logo.features.text.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.FontItem

class FontsListAdapter(private var fontItem: FontItemClickEvent)
    : RecyclerView.Adapter<FontsListAdapter.FontsListViewHolder>() {

    private var listFonts: ArrayList<FontItem> = ArrayList()

    fun setFontsList(listFonts: ArrayList<FontItem>) {
        this.listFonts = listFonts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FontsListViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.font_list_itemview, parent, false)
        return FontsListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (listFonts.isNotEmpty())
            listFonts.size
        else
            0
    }

    override fun onBindViewHolder(holder: FontsListViewHolder, position: Int) {

        holder.text1.typeface = listFonts[position].typeface

        //item click listener
        holder.text1.setOnClickListener {
            fontItem.fontItemClicked(listFonts[position], position)
        }
    }

    class FontsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val text1: AppCompatTextView = itemView.findViewById(R.id.text_1)
    }

    interface FontItemClickEvent {
        fun fontItemClicked(item: FontItem, position: Int)
    }
}
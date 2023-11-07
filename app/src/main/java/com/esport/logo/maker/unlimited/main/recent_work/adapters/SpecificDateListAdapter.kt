package com.esport.logo.maker.unlimited.main.recent_work.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.SavedLogo

class SpecificDateListAdapter(private var itemClick: BeforeYesterdayList): RecyclerView.Adapter<SpecificDateListAdapter.BeforeYesterdayListItemViewHolder>() {

    private var beforeYesterdayList: ArrayList<SavedLogo> = ArrayList()

    fun setBeforeYesterdayListToRecentAdapter(beforeYesterdayList: ArrayList<SavedLogo>){
        this.beforeYesterdayList = beforeYesterdayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeforeYesterdayListItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_images_itemview,parent,false)
        return BeforeYesterdayListItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: BeforeYesterdayListItemViewHolder, position: Int) {

        holder.itemLogo.setImageBitmap(BitmapFactory.decodeFile(beforeYesterdayList[position].filePath))

        holder.itemLogo.setOnClickListener {
            itemClick.beforeYesterdayItemClick(beforeYesterdayList[position])
        }
    }

    override fun getItemCount(): Int {
        return if (beforeYesterdayList.isNotEmpty())
            beforeYesterdayList.size
        else
            0
    }

    class BeforeYesterdayListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var itemLogo: AppCompatImageView = itemView.findViewById(R.id.item_recent_list)
    }

    interface BeforeYesterdayList {
        fun beforeYesterdayItemClick(beforeYesterdayLogo: SavedLogo)
    }
}
package com.esport.logo.maker.unlimited.main.recent_work.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.SavedLogo

class YesterdayListAdapter(private var itemClick: YesterdayListItemClick): RecyclerView.Adapter<YesterdayListAdapter.YesterdayListItemViewHolder>() {

    private var yesterdayList: ArrayList<SavedLogo> = ArrayList()

    fun setYesterdayListToRecentAdapter(yesterdayList: ArrayList<SavedLogo>){
        this.yesterdayList = yesterdayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YesterdayListItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_images_itemview,parent,false)
        return YesterdayListItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: YesterdayListItemViewHolder, position: Int) {

        holder.itemLogo.setImageBitmap(BitmapFactory.decodeFile(yesterdayList[position].filePath))

        holder.itemLogo.setOnClickListener {
            itemClick.yesterdayListItemClick(yesterdayList[position])
        }
    }

    override fun getItemCount(): Int {
        return if (yesterdayList.isNotEmpty())
            yesterdayList.size
        else
            0
    }

    class YesterdayListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var itemLogo: AppCompatImageView = itemView.findViewById(R.id.item_recent_list)
    }

    interface YesterdayListItemClick {
        fun yesterdayListItemClick(yesterdayLogo: SavedLogo)
    }
}
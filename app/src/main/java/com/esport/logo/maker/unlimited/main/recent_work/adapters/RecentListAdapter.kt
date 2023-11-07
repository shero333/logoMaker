package com.esport.logo.maker.unlimited.main.recent_work.adapters

import android.graphics.BitmapFactory
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.SavedLogo

class RecentListAdapter(private var itemClick:RecentListItemClick): RecyclerView.Adapter<RecentListAdapter.RecentListItemViewHolder>() {

    private lateinit var recentList: ArrayList<SavedLogo>

    fun setRecentListToRecentAdapter(recentList: ArrayList<SavedLogo>){
        this.recentList = recentList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentListItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_images_itemview,parent,false)
        return RecentListItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentListItemViewHolder, position: Int) {

        holder.itemLogo.setImageBitmap(BitmapFactory.decodeFile(recentList[position].filePath))

        holder.itemLogo.setOnClickListener {
            itemClick.recentListItemClick(recentList[position])
        }
    }

    override fun getItemCount(): Int {
        return if (recentList.isNotEmpty())
            recentList.size
        else
            0
    }

    class RecentListItemViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){

        var itemLogo: AppCompatImageView = itemView.findViewById(R.id.item_recent_list)
    }

    interface RecentListItemClick {
        fun recentListItemClick(recentLogo: SavedLogo)
    }
}
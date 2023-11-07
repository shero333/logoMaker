package com.esport.logo.maker.unlimited.main.adapterRecentMainList

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.SavedLogo

class RecentMainListAdapter(private var clickEvent: RecentItemClicked): RecyclerView.Adapter<RecentMainListAdapter.RecentMainListViewHolder>() {

    private lateinit var listRecent: ArrayList<SavedLogo>

    fun setRecentLogosList(listRecent: ArrayList<SavedLogo>){
        this.listRecent = listRecent
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentMainListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_images_itemview,parent,false)
        return RecentMainListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (listRecent.isNotEmpty())
            listRecent.size
        else
            0
    }

    override fun onBindViewHolder(holder: RecentMainListViewHolder, position: Int) {

        val image = BitmapFactory.decodeFile(listRecent[position].filePath)
        holder.itemRecent.setImageBitmap(image)

        //item click
        holder.itemRecent.setOnClickListener {
            clickEvent.recentItemClicked()
        }
    }

    class RecentMainListViewHolder(itemView:View): RecyclerView.ViewHolder(itemView) {

        var itemRecent = itemView.findViewById<AppCompatImageView>(R.id.item_recent_list)!!
    }

    interface RecentItemClicked{
        fun recentItemClicked()
    }
}
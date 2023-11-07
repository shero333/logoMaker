package com.esport.logo.maker.unlimited.main.edit_create_logo.features.background.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.TabItem

class TabsAdapterBackground(
    var context: Context,
    private var clickedTab: ItemClickedTabs) :
    RecyclerView.Adapter<TabsAdapterBackground.TabsListViewHolder>() {

    private var tabs:ArrayList<TabItem> = ArrayList()
    private var selectedItemPosition = 0

    fun setTabsNames(tabs:ArrayList<TabItem>){
        this.tabs = tabs
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabsListViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.tabs_list_itemview,parent,false)
        return TabsListViewHolder(view)
    }

    override fun getItemCount(): Int {

        return if(tabs.isNotEmpty())
            tabs.size
        else
            0
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: TabsListViewHolder, position: Int) {

        val item_position = position

        //load the image in imageview through Glide

        holder.tab.text = tabs[position].tabName

        //change background of clicked item
        changeItemBackground(holder, tabs[position].isSelected && selectedItemPosition == position)

        holder.tab.setOnClickListener{

            //set the card stroke color and width and send the clicked item to the fragment
            if (selectedItemPosition != item_position) {
                selectedItemPosition = item_position
                clickedTab.tabClicked(tabs[position], item_position)
            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeSelectionStatus(position: Int) {
        for (tab in tabs) {
            tab.isSelected = selectedItemPosition == position
        }
        notifyDataSetChanged()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun changeItemBackground(holder: TabsListViewHolder, isSelected: Boolean) {
        if (isSelected) {
            holder.tab.isSelected = true
            //set the background of tab selected
            holder.tab.background = ContextCompat.getDrawable(context,R.drawable.item_click_background_fragment)
            holder.tab.setPadding(10,0,10,0)

        } else {
            holder.tab.isSelected = false
            //set the background of tab un-selected
            holder.tab.background = null
            holder.tab.setPadding(0,0,0,0)

        }
    }

    class TabsListViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){

        val tab: AppCompatTextView = itemView.findViewById(R.id.tab_item_button)
    }

    interface ItemClickedTabs{
        fun tabClicked(tab: TabItem, position: Int)
    }
}
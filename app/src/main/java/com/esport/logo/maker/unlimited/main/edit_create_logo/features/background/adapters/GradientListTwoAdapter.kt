package com.esport.logo.maker.unlimited.main.edit_create_logo.features.background.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.UserColors
import com.google.android.material.card.MaterialCardView

class GradientListTwoAdapter(private var gradientColorClick: GradientColor2ClickedEvent)  : RecyclerView.Adapter<GradientListTwoAdapter.GradientListTwoViewHolder>(){

    private var listColors: MutableList<UserColors> = ArrayList()
    var selectedItemPosition = 0

    fun setListColors(listColors: MutableList<UserColors>){
        this.listColors = listColors
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradientListTwoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.colorlist2_itemview,parent,false)

        return GradientListTwoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listColors.size
    }

    override fun onBindViewHolder(holder: GradientListTwoViewHolder, position: Int) {

        val item_position = position

        holder.colorItem.setCardBackgroundColor(listColors[position].colorCode)

        changeItemBackground(holder, listColors[position].isSelected && selectedItemPosition == position)

        holder.itemView.setOnClickListener {

            //set the card stroke color and width and send the clicked item to the fragment
            if (selectedItemPosition != item_position) {
                selectedItemPosition = item_position
                gradientColorClick.gradientColor2Clicked(listColors[position].colorCode,position)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeSelectionStatus(position: Int) {
        for (color in listColors) {
            color.isSelected = selectedItemPosition == position
        }
        notifyDataSetChanged()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun changeItemBackground(holder: GradientListTwoViewHolder, isSelected: Boolean) {
        if (isSelected) {
            holder.colorItem.isSelected = true
            //set the background of tab selected
            holder.colorItem.layoutParams = ViewGroup.LayoutParams(45, 55)
            holder.colorItem.cardElevation = 15F

        } else {
            holder.colorItem.isSelected = false
            //set the background of tab un-selected
        }
    }

    class GradientListTwoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val colorItem: MaterialCardView = itemView.findViewById(R.id.color_item)
    }

    interface GradientColor2ClickedEvent{
        fun gradientColor2Clicked(i: Int,position:Int)
    }
}

package com.esport.logo.maker.unlimited.main.edit_create_logo.features.elements.adapters

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.application.LogoMakerApp
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.Image
import com.esport.logo.maker.unlimited.main.edit_create_logo.utils.Utils
import com.google.android.material.card.MaterialCardView


class ElementsListAdapter(private var clickedImage: ItemClickedImages) :
    RecyclerView.Adapter<ElementsListAdapter.ElementsListViewHolder>() {

    private var logoImageList:ArrayList<Image> = ArrayList()
    private var selectedItemPosition = 0

    fun setElementsList(logoImageList:ArrayList<Image>){
        this.logoImageList = logoImageList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElementsListViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.background_image_list_itemview,parent,false)
        return ElementsListViewHolder(view)
    }

    override fun getItemCount(): Int {

        return if(logoImageList.isNotEmpty())
            logoImageList.size
        else
            0
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: ElementsListViewHolder, position: Int) {

        val item_position = position

        //load the image in imageview through Glide
        holder.placeholderImage.playAnimation()
        Glide
            .with(LogoMakerApp.appContext)
            .load(Utils.stringToDrawable(logoImageList[position].logoImage))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {

                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {

                    holder.placeholderImage.pauseAnimation()
                    holder.placeholderImage.visibility = View.GONE
                    return false
                }

            })
            .into(holder.backgroundItem)

        //change background of clicked item
        changeItemBackground(holder, logoImageList[position].isSelected && selectedItemPosition == position)

        holder.itemView.setOnClickListener{

            //set the card stroke color and width and send the clicked item to the fragment
            if (selectedItemPosition != item_position) {
                selectedItemPosition = item_position
                clickedImage.itemClicked(logoImageList[position], item_position)
            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeSelectionStatus(position: Int) {
        for (image in logoImageList) {
            image.isSelected = selectedItemPosition == position
        }
        notifyDataSetChanged()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun changeItemBackground(holder: ElementsListViewHolder, isSelected: Boolean) {
        if (isSelected) {
            holder.backgroundItem.isSelected = true
            holder.strokeImage.strokeColor = ContextCompat.getColor(LogoMakerApp.appContext,R.color.theme_color)
            holder.strokeImage.strokeWidth = 1
        } else {
            holder.backgroundItem.isSelected = false
            holder.strokeImage.strokeColor = 0
            holder.strokeImage.strokeWidth = 0
        }
    }

    class ElementsListViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){

        val backgroundItem: AppCompatImageView = itemView.findViewById(R.id.logo_item)
        val strokeImage: MaterialCardView = itemView.findViewById(R.id.image_card)
        val placeholderImage: LottieAnimationView = itemView.findViewById(R.id.placeholder_image)
    }

    interface ItemClickedImages{
        fun itemClicked(item: Image, position: Int)
    }
}
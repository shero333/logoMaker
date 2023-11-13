package com.esport.logo.maker.unlimited.main.edit_create_logo.features.background.adapters

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


class BackgroundListAdapter(
    private var clickedImage: ItemClickedImages,
    private var closeButton: CloseButtonClicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var imageList:ArrayList<Image> = ArrayList()
    var selectedItemPosition = 0
    private lateinit var listName:String

    private val FIRST_ITEM_VIEW_TYPE = 0
    private val REGULAR_ITEM_VIEW_TYPE = 1

    fun setImagesList(listName:String,imageList:ArrayList<Image>){
        this.imageList = imageList
        this.listName = listName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == FIRST_ITEM_VIEW_TYPE){

            //remove background button view
            val view = LayoutInflater.from(parent.context).inflate(R.layout.background_image_remove_itemview,parent,false)
            RemoveBackgroundViewHolder(view)
        } else{

            val view = LayoutInflater.from(parent.context).inflate(R.layout.background_image_list_itemview,parent,false)
            BackgroundListViewHolder(view)
        }
    }

    override fun getItemCount(): Int {

        return if(imageList.isNotEmpty())
            imageList.size
        else
            0
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item_position = position

        if (holder is BackgroundListViewHolder) {

            //load the image in imageview through Glide
            holder.placeholderImage.playAnimation()
            Glide
                .with(LogoMakerApp.appContext)
                .load(Utils.stringToDrawable(imageList[position].logoImage))
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
            changeItemBackground(holder, imageList[position].isSelected && selectedItemPosition == position)
        }
        else if (holder is RemoveBackgroundViewHolder){

            //change background of clicked item
            changeItemBackground(holder, imageList[position].isSelected && selectedItemPosition == position)
        }

        holder.itemView.setOnClickListener{

            if (holder is BackgroundListViewHolder){

                //set the card stroke color and width and send the clicked item to the fragment
                if (selectedItemPosition != item_position) {
                    selectedItemPosition = item_position
                    clickedImage.itemClicked(imageList[position], item_position,listName)
                }
            }
            else if (holder is RemoveBackgroundViewHolder){

                //click for the remove background button
                closeButton.closeButtonClicked(listName)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeSelectionStatus(position: Int) {
        for (image in imageList) {
            image.isSelected = selectedItemPosition == position
        }
        notifyDataSetChanged()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun changeItemBackground(holder: RecyclerView.ViewHolder, isSelected: Boolean) {
        if (holder is BackgroundListViewHolder){

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
        else if (holder is RemoveBackgroundViewHolder){
            if (isSelected) {
                holder.closeButton.isSelected = true
                holder.strokeImage.strokeColor = ContextCompat.getColor(LogoMakerApp.appContext,R.color.theme_color)
                holder.strokeImage.strokeWidth = 1
            } else {
                holder.closeButton.isSelected = false
                holder.strokeImage.strokeColor = 0
                holder.strokeImage.strokeWidth = 0
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) FIRST_ITEM_VIEW_TYPE else REGULAR_ITEM_VIEW_TYPE
    }

    class BackgroundListViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){

        val backgroundItem: AppCompatImageView = itemView.findViewById(R.id.logo_item)
        val strokeImage: MaterialCardView = itemView.findViewById(R.id.image_card)
        val placeholderImage: LottieAnimationView = itemView.findViewById(R.id.placeholder_image)
    }

    class RemoveBackgroundViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){

        val closeButton: AppCompatImageView = itemView.findViewById(R.id.remove_button)
        val strokeImage: MaterialCardView = itemView.findViewById(R.id.close_card)
    }

    interface ItemClickedImages{
        fun itemClicked(item: Image, position: Int, listName: String)
    }

    interface CloseButtonClicked{
        fun closeButtonClicked(listName: String)
    }
}
package com.esport.logo.maker.unlimited.main.edit_create_logo.interface_click_events

import android.text.Layout
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.FontItem

interface TextFragmentInterface {
    fun opacityOfTextOfStickerTextFragment(alpha:Int)
    fun controllerViewMoveStickerButtonsClickEventsTextFragment(direction:String)
    fun controllerViewAlignStickerTextButtonsClickEventsTextFragment(alignment: Layout.Alignment)
    fun controllerViewEditStickerTextButtonsClickEventTextFragment()
    fun controllerViewCopyStickerButtonClickEventTextFragment()
    fun colorListItemClickApplyOnTextOfStickerTextFragment(color:Int)
    fun fontListItemClickApplyOnTextOfStickerTextFragment(font: FontItem)
    fun shadowColorAndIntensityForTheStickerTextTextFragment(color: Int, shadow: Int)
}
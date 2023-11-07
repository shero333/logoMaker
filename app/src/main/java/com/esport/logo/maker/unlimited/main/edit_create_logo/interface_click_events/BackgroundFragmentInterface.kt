package com.esport.logo.maker.unlimited.main.edit_create_logo.interface_click_events

import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.Image

interface BackgroundFragmentInterface {
    fun opacityLogoBackgroundFragment(opacity:Float)
    fun clickOnGraphicsItemBackgroundFragment()
    fun clickOnTextureItemBackgroundFragment()
    fun clickOnColorsItemBackgroundFragment()
    fun clickOnShapesItemBackgroundFragment()
    fun clickOnImageItemForBackgroundLogoBackgroundFragment(item: Image, listName: String)
    fun clickOnColorItemForBackgroundLogoBackgroundFragment(colorClicked:Int)
    fun clickOnGradientButtonBackgroundFragment()
    fun clickOnGradient1ItemForBackgroundLogoBackgroundFragment(gradient1ColorClicked:Int)
    fun clickOnGradient2ItemForBackgroundLogoBackgroundFragment(gradient2ColorClicked:Int)
    fun clickOnRemoveButtonBackgroundItemListBackgroundFragment(listName: String)
}
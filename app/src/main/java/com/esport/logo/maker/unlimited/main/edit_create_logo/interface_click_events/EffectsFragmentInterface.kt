package com.esport.logo.maker.unlimited.main.edit_create_logo.interface_click_events

import android.graphics.Bitmap

interface EffectsFragmentInterface {
    fun effectItemClickOpacityProgressEffectsFragment(effectSelected: Bitmap, background: Bitmap?, alphaProgress: Int)
    fun effectItemClickApplyBackgroundEffectsFragment(effectSelected: Bitmap, background: Bitmap?, opacity: Int)
    fun effectCleanButtonClickedEffectsFragment()
}
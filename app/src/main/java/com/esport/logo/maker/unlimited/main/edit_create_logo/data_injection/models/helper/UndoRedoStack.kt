package com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.helper

import android.graphics.Bitmap
import com.xiaopo.flying.sticker.Sticker

data class UndoRedoStack(
    var background: Bitmap?,
    var sticker: Sticker?
)

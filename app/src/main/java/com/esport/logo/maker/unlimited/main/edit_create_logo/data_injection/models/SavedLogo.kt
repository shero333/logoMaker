package com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models

import android.os.Parcel
import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import java.io.Serial
import java.io.Serializable

data class SavedLogo(
    var filePath: String,
    var timeNDate: Long
) : Serializable

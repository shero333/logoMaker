package com.esport.logo.maker.unlimited.main.edit_create_logo.utils

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import androidx.annotation.RequiresApi
import java.io.File
import java.util.Date
import java.util.Locale


object FileUtil {
    private fun getFolderName(name: String?): String {
        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), name!!)
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return ""
            }
        }
        return mediaStorageDir.absolutePath
    }

    private val isSDAvailable: Boolean
        /**
         * 判断sd卡是否可以用
         */
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    @RequiresApi(Build.VERSION_CODES.N)
    fun getNewFile(context: Context, folderName: String?): File? {
        val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val timeStamp: String = simpleDateFormat.format(Date())
        val path: String = if (isSDAvailable) {
            getFolderName(folderName) + File.separator + timeStamp + ".png"
        } else {
            context.filesDir.path + File.separator + timeStamp + ".png"
        }
        return if (TextUtils.isEmpty(path)) {
            null
        } else File(path)
    }
}

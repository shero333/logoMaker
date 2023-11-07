package com.esport.logo.maker.unlimited.main.edit_create_logo.utils

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Base64
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.esport.logo.maker.unlimited.databinding.GalleryDialogBinding
import com.esport.logo.maker.unlimited.main.edit_create_logo.ViewModelMain
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.preview.PreviewActivity
import com.xiaopo.flying.sticker.StickerView
import java.io.ByteArrayOutputStream
import java.io.File


class Utils {
    companion object {

        private var REQUEST_CODE_STORAGE = 3
        private var REQUEST_CODE = 100

        fun drawableToString(drawable: Drawable?): String {
            val bitmap = drawableToBitmap(drawable)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
        private fun drawableToBitmap(drawable: Drawable?): Bitmap {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            val bitmap = Bitmap.createBitmap(
                drawable!!.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
        fun stringToDrawable(encodedString: String): Drawable {
            val decodedByteArray = Base64.decode(encodedString, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
            return BitmapDrawable(null, bitmap)
        }
        fun stringToBitmap(imageString: String): Bitmap? {
            val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
            if (imageBytes.isEmpty()) {
                return null
            }
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
        fun bitmapToString(bitmap: Bitmap): String? {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val b = baos.toByteArray()
            return Base64.encodeToString(b, Base64.DEFAULT)
        }

        //permission functions
        fun cameraAndGalleryPermissionDialog(activity: Activity) {
            val dialog = Dialog(activity)

            val galleryDialogBinding: GalleryDialogBinding =
                GalleryDialogBinding.inflate(LayoutInflater.from(activity))
            dialog.setContentView(galleryDialogBinding.root)

            //button continue
            galleryDialogBinding.btnSettings.setOnClickListener {
                activity.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package: " + "com.example.thelogomaker")
                    )
                )

                dialog.dismiss()
            }

            //img view cancel dialog
            galleryDialogBinding.imgCancelDialog.setOnClickListener { dialog.dismiss() }

            //setting the transparent background

            //setting the transparent background
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            //this sets the width of dialog to 90%

            //this sets the width of dialog to 90%
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = (displayMetrics.widthPixels * 0.9).toInt()

            //setting the width and height of alert dialog

            //setting the width and height of alert dialog
            dialog.window!!.setLayout(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)

            dialog.show()
        }
        fun hasStoragePermission(context: Context): Boolean {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            return context.checkCallingOrSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED &&
                    context.checkCallingOrSelfPermission(permissions[1]) == PackageManager.PERMISSION_GRANTED
        }
        fun getStoragePermission(activity: Activity) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(permissions, REQUEST_CODE_STORAGE)
            }
        }

        //applying shape to the image selected
        fun toCustomShapeBitmap(viewModelMain: ViewModelMain, bitmap: Bitmap?, shapeSelected: Int, context: Context): Bitmap {
            val width = bitmap!!.width
            val height = bitmap.height
            val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            //drawing bitmap object
            val canvas = Canvas(output)
            val color = -0xbdbdbe
            val paint = Paint()

            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color

            // Get solid heart to mask out the portion of the image we want to keep.
            var shape = BitmapFactory.decodeResource(context.resources, shapeSelected)
            shape = Bitmap.createScaledBitmap(shape!!, width, height, true)
            canvas.drawBitmap(shape, 0f, 0f, null)

            // SRC_IN means to keep the portion of the bitmap that overlaps the solid heart. All pixels
            // from the solid heart and outside the solid heart area of the bitmap are tossed.
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            //saving to the preference
            viewModelMain.selectedImageShapeApplied = bitmapToString(output)!!
            return output
        }

        // Function to convert Bitmap to PNG and save it to a file
        @RequiresApi(Build.VERSION_CODES.N)
        fun convertBitmapToPNGAndSave(activity: Activity, sticker: StickerView?) {

            //removing background from the stickerView to save the sticker
            sticker!!.setBackgroundResource(0)

            val file: File? = FileUtil.getNewFile(activity, "LogoMakerStickers")
            if (file != null) {

                sticker.save(file)
                Toast.makeText(activity, "Your logo saved to the gallery", Toast.LENGTH_SHORT).show()

                //to preview activity
                val intent = Intent(activity, PreviewActivity::class.java)
                intent.putExtra("imagePreview",file.absoluteFile)
                activity.startActivityForResult(intent,REQUEST_CODE)

            } else {
                Toast.makeText(activity, "Unable to save the logo!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
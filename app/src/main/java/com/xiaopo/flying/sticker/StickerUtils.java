package com.xiaopo.flying.sticker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static java.lang.Math.round;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.SavedLogo;
import com.esport.logo.maker.unlimited.preferences.SharedPreference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

class StickerUtils {
    private static final String TAG = "StickerView";
    public static void saveImageToGallery(@NonNull File file, @NonNull Bitmap bmp) {
        if (bmp == null) {
            throw new IllegalArgumentException("bmp should not be null");
        }
        try {

            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //concatenating the items being saved to gallery into the list which is save in the preference
        String savedLogos = SharedPreference.getDownloadedLogos();

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<SavedLogo>>() {}.getType();
        ArrayList<SavedLogo> savedLogosList = gson.fromJson(savedLogos, type);

        //check for the 'Null Pointer Exception'
        if (savedLogosList != null)
            savedLogosList.add(new SavedLogo(file.getAbsolutePath(),System.currentTimeMillis()));
        else{
            savedLogosList = new ArrayList<>();
            savedLogosList.add(new SavedLogo(file.getAbsolutePath(),System.currentTimeMillis()));
        }

        String downloadedImages = gson.toJson(savedLogosList);
        SharedPreference.setDownloadedLogos(downloadedImages);

        Log.e(TAG, "saveImageToGallery: the path of bmp is " + file.getAbsolutePath());
    }

    public static void notifySystemGallery(@NonNull Context context, @NonNull File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("bmp should not be null");
        }

        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), null);

        } catch (FileNotFoundException e) {
            throw new IllegalStateException("File couldn't be found");
        }
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }

    @NonNull
    public static RectF trapToRect(@NonNull float[] array) {
        RectF r = new RectF();
        trapToRect(r, array);
        return r;
    }

    public static void trapToRect(@NonNull RectF r, @NonNull float[] array) {
        r.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY);
        for (int i = 1; i < array.length; i += 2) {
            float x = round(array[i - 1] * 10) / 10.f;
            float y = round(array[i] * 10) / 10.f;
            r.left = Math.min(x, r.left);
            r.top = Math.min(y, r.top);
            r.right = Math.max(x, r.right);
            r.bottom = Math.max(y, r.bottom);
        }
        r.sort();
    }
}

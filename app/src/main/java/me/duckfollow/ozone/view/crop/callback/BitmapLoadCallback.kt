package me.duckfollow.ozone.view.crop.callback

import android.graphics.Bitmap
import me.duckfollow.ozone.view.crop.model.ExifInfo

interface BitmapLoadCallback {

    fun onBitmapLoaded(
        bitmap: Bitmap,
        exifInfo: ExifInfo,
        imageInputPath: String,
        imageOutputPath: String?
    )

    fun onFailure(bitmapWorkerException: Exception)

}
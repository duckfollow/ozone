package me.duckfollow.ozone.view.crop.model

import android.graphics.Bitmap

class CropParameters(
    val maxResultImageSizeX: Int, val maxResultImageSizeY: Int,
    val compressFormat: Bitmap.CompressFormat, val compressQuality: Int,
    val imageInputPath: String, val imageOutputPath: String, val exifInfo: ExifInfo
)
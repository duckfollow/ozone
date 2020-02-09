package me.duckfollow.ozone.utils

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory
import android.graphics.Matrix


class ConvertImagetoBase64 {

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun base64ToBitmap(b64: String): Bitmap {
        val imageAsBytes = Base64.decode(b64.toByteArray(), Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
    }

    //resize image
    fun getResizedBitmap(bm: Bitmap?, H: Int, W: Int): Bitmap {
        val width = bm!!.width
        val height = bm.height

        val scaleWidth = W.toFloat() / width
        val scaleHeight = H.toFloat() / height

        val matrix = Matrix()

        matrix.postScale(scaleWidth, scaleHeight)

        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
        //return Bitmap.createScaledBitmap(bm, width, height, true)
    }


    fun getResizedBitmap(bm: Bitmap, newWidth: Int): Bitmap {
        val ratio = Math.min(newWidth.toFloat() / bm.getWidth(), newWidth.toFloat() / bm.getHeight())
        val width = Math.round(ratio * bm.getWidth())
        val height = Math.round(ratio * bm.getHeight())

        return Bitmap.createScaledBitmap(bm, width, height, false
        )
    }

}
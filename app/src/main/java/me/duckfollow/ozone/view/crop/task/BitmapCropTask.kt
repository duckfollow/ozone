package me.duckfollow.ozone.view.crop.task

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.media.ExifInterface
import android.net.Uri
import android.os.AsyncTask
import android.util.Log

import java.io.File
import java.io.IOException
import me.duckfollow.ozone.view.crop.callback.BitmapCropCallback
//import com.prasit.theboxshop.view.crop.model.CropParameters
//import com.prasit.theboxshop.view.crop.model.ExifInfo
//import com.prasit.theboxshop.view.crop.model.ImageState
import me.duckfollow.ozone.view.crop.util.FileUtils
import me.duckfollow.ozone.view.crop.util.ImageHeaderParser
import com.yalantis.ucrop.model.CropParameters
import com.yalantis.ucrop.model.ExifInfo
import com.yalantis.ucrop.model.ImageState

/**
 * Crops part of image that fills the crop bounds.
 *
 *
 * First image is downscaled if max size was set and if resulting image is larger that max size.
 * Then image is rotated accordingly.
 * Finally new Bitmap object is created and saved to file.
 */
class BitmapCropTask(private var mViewBitmap: Bitmap?, imageState: ImageState, cropParameters: CropParameters, private val mCropCallback: BitmapCropCallback?) : AsyncTask<Void, Void, Throwable>() {

    private val mCropRect: RectF
    private val mCurrentImageRect: RectF

    private var mCurrentScale: Float = 0.toFloat()
    private val mCurrentAngle: Float
    private val mMaxResultImageSizeX: Int
    private val mMaxResultImageSizeY: Int

    private val mCompressFormat: Bitmap.CompressFormat
    private val mCompressQuality: Int
    private val mImageInputPath: String
    private val mImageOutputPath: String
    private val mExifInfo: ExifInfo

    private var mCroppedImageWidth: Int = 0
    private var mCroppedImageHeight: Int = 0
    private var cropOffsetX: Int = 0
    private var cropOffsetY: Int = 0

    init {
        mCropRect = imageState.cropRect
        mCurrentImageRect = imageState.currentImageRect

        mCurrentScale = imageState.currentScale
        mCurrentAngle = imageState.currentAngle
        mMaxResultImageSizeX = cropParameters.maxResultImageSizeX
        mMaxResultImageSizeY = cropParameters.maxResultImageSizeY

        mCompressFormat = cropParameters.compressFormat
        mCompressQuality = cropParameters.compressQuality

        mImageInputPath = cropParameters.imageInputPath
        mImageOutputPath = cropParameters.imageOutputPath
        mExifInfo = cropParameters.exifInfo
    }

    override fun doInBackground(vararg params: Void): Throwable? {
        if (mViewBitmap == null) {
            return NullPointerException("ViewBitmap is null")
        } else if (mViewBitmap!!.isRecycled) {
            return NullPointerException("ViewBitmap is recycled")
        } else if (mCurrentImageRect.isEmpty) {
            return NullPointerException("CurrentImageRect is empty")
        }

        val resizeScale = resize()

        try {
            crop(resizeScale)
            mViewBitmap = null
        } catch (throwable: Throwable) {
            return throwable
        }

        return null
    }

    private fun resize(): Float {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(mImageInputPath, options)

        val swapSides = mExifInfo.exifDegrees == 90 || mExifInfo.exifDegrees == 270
        var scaleX =
            (if (swapSides) options.outHeight else options.outWidth) / mViewBitmap!!.width.toFloat()
        var scaleY =
            (if (swapSides) options.outWidth else options.outHeight) / mViewBitmap!!.height.toFloat()

        var resizeScale = Math.min(scaleX, scaleY)

        mCurrentScale /= resizeScale

        resizeScale = 1f
        if (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) {
            val cropWidth = mCropRect.width() / mCurrentScale
            val cropHeight = mCropRect.height() / mCurrentScale

            if (cropWidth > mMaxResultImageSizeX || cropHeight > mMaxResultImageSizeY) {

                scaleX = mMaxResultImageSizeX / cropWidth
                scaleY = mMaxResultImageSizeY / cropHeight
                resizeScale = Math.min(scaleX, scaleY)

                mCurrentScale /= resizeScale
            }
        }
        return resizeScale
    }

    @Throws(IOException::class)
    private fun crop(resizeScale: Float): Boolean {
        val originalExif = ExifInterface(mImageInputPath)

        cropOffsetX = Math.round((mCropRect.left - mCurrentImageRect.left) / mCurrentScale)
        cropOffsetY = Math.round((mCropRect.top - mCurrentImageRect.top) / mCurrentScale)
        mCroppedImageWidth = Math.round(mCropRect.width() / mCurrentScale)
        mCroppedImageHeight = Math.round(mCropRect.height() / mCurrentScale)

        val shouldCrop = shouldCrop(mCroppedImageWidth, mCroppedImageHeight)
        Log.i(TAG, "Should crop: $shouldCrop")

        if (shouldCrop) {
            Log.d("str1",mImageInputPath)
            Log.d("str2",mImageOutputPath)
            Log.d("int1",cropOffsetX.toString())
            Log.d("int2",cropOffsetY.toString())
            Log.d("int3",mCroppedImageWidth.toString())
            Log.d("int4",mCroppedImageHeight.toString())
            Log.d("Floast1",mCurrentAngle.toString())
            Log.d("Floast2",resizeScale.toString())
            Log.d("int5",mCompressFormat.ordinal.toString())
            Log.d("int6",mCompressQuality.toString())
            Log.d("int7",mExifInfo.exifDegrees.toString())
            Log.d("int8",mExifInfo.exifTranslation.toString())
            val cropped = cropCImg(
                    mImageInputPath, mImageOutputPath,
                    cropOffsetX, cropOffsetY, mCroppedImageWidth, mCroppedImageHeight,
                    mCurrentAngle, resizeScale, mCompressFormat.ordinal, mCompressQuality,
                    mExifInfo.exifDegrees, mExifInfo.exifTranslation
                )

//            val cropped = true
//            Log.d("file",mCompressFormat.toString()+"//"+Bitmap.CompressFormat.JPEG)

            if (cropped && mCompressFormat == Bitmap.CompressFormat.JPEG) {
                ImageHeaderParser.copyExif(
                    originalExif,
                    mCroppedImageWidth,
                    mCroppedImageHeight,
                    mImageOutputPath
                )
            }
            return cropped
        } else {
            FileUtils.copyFile(mImageInputPath, mImageOutputPath)
            return false
        }
    }



    /**
     * Check whether an image should be cropped at all or just file can be copied to the destination path.
     * For each 1000 pixels there is one pixel of error due to matrix calculations etc.
     *
     * @param width  - crop area width
     * @param height - crop area height
     * @return - true if image must be cropped, false - if original image fits requirements
     */
    private fun shouldCrop(width: Int, height: Int): Boolean {
        var pixelError = 1
        pixelError += Math.round(Math.max(width, height) / 1000f)
        return (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0
                || Math.abs(mCropRect.left - mCurrentImageRect.left) > pixelError
                || Math.abs(mCropRect.top - mCurrentImageRect.top) > pixelError
                || Math.abs(mCropRect.bottom - mCurrentImageRect.bottom) > pixelError
                || Math.abs(mCropRect.right - mCurrentImageRect.right) > pixelError
                || mCurrentAngle != 0f)
    }

    override fun onPostExecute(t: Throwable?) {
        if (mCropCallback != null) {
            if (t == null) {
                val uri = Uri.fromFile(File(mImageOutputPath))
                mCropCallback.onBitmapCropped(
                    uri,
                    cropOffsetX,
                    cropOffsetY,
                    mCroppedImageWidth,
                    mCroppedImageHeight
                )
            } else {
                mCropCallback.onCropFailure(t)
            }
        }
    }

    companion object {

        const val TAG = "BitmapCropTask"

        init {
            System.loadLibrary("ucrop")
        }
        @Throws(IOException::class, OutOfMemoryError::class)
        external fun cropCImg(
            inputPath: String, outputPath: String,
            left: Int, top: Int, width: Int, height: Int,
            angle: Float, resizeScale: Float,
            format: Int, quality: Int,
            exifDegrees: Int, exifTranslation: Int
        ): Boolean
    }

}
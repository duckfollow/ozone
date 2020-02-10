package me.duckfollow.ozone.view.crop.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

class FastBitmapDrawable(b: Bitmap) : Drawable() {

    private val mPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    var bitmap: Bitmap? = null
        set(b) {
            field = b
            if (b != null) {
                mWidth = bitmap!!.width
                mHeight = bitmap!!.height
            } else {
                mHeight = 0
                mWidth = mHeight
            }
        }
    private var mAlpha: Int = 0
    private var mWidth: Int = 0
    private var mHeight: Int = 0

    init {
        mAlpha = 255
        bitmap = b
    }

    override fun draw(canvas: Canvas) {
        if (bitmap != null && !bitmap!!.isRecycled) {
            canvas.drawBitmap(bitmap!!, null, bounds, mPaint)
        }
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setFilterBitmap(filterBitmap: Boolean) {
        mPaint.isFilterBitmap = filterBitmap
    }

    override fun getAlpha(): Int {
        return mAlpha
    }

    override fun setAlpha(alpha: Int) {
        mAlpha = alpha
        mPaint.alpha = alpha
    }

    override fun getIntrinsicWidth(): Int {
        return mWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mHeight
    }

    override fun getMinimumWidth(): Int {
        return mWidth
    }

    override fun getMinimumHeight(): Int {
        return mHeight
    }

}
package me.duckfollow.ozone.view.crop.callback

import android.graphics.RectF

interface OverlayViewChangeListener {

    fun onCropRectUpdated(cropRect: RectF)

}
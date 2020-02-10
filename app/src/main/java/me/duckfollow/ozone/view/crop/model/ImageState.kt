package me.duckfollow.ozone.view.crop.model

import android.graphics.RectF

class ImageState(
    val cropRect: RectF,
    val currentImageRect: RectF,
    val currentScale: Float,
    val currentAngle: Float
)
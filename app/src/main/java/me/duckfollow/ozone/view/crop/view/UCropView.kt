package me.duckfollow.ozone.view.crop.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import me.duckfollow.ozone.R
import me.duckfollow.ozone.view.crop.callback.CropBoundsChangeListener
import me.duckfollow.ozone.view.crop.callback.OverlayViewChangeListener

class UCropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var cropImageView: GestureCropImageView? = null
        private set
    val overlayView: OverlayView

    init {

        LayoutInflater.from(context).inflate(R.layout.ucrop_view, this, true)
        cropImageView = findViewById(R.id.image_view_crop) as GestureCropImageView?
        overlayView = findViewById(R.id.view_overlay) as OverlayView

        val a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_UCropView)
        overlayView.processStyledAttributes(a)
        cropImageView!!.processStyledAttributes(a)
        a.recycle()


        setListenersToViews()
    }

    private fun setListenersToViews() {
        cropImageView!!.cropBoundsChangeListener = object : CropBoundsChangeListener {
            override fun onCropAspectRatioChanged(cropRatio: Float) {
                overlayView.setTargetAspectRatio(cropRatio)
            }
        }

        overlayView.overlayViewChangeListener = object : OverlayViewChangeListener {
            override fun onCropRectUpdated(cropRect: RectF) {
                cropImageView!!.setCropRect(cropRect)
            }
        }
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    /**
     * Method for reset state for UCropImageView such as rotation, scale, translation.
     * Be careful: this method recreate UCropImageView instance and reattach it to layout.
     */
    fun resetCropImageView() {
        removeView(cropImageView)
        cropImageView = GestureCropImageView(context)
        setListenersToViews()
        cropImageView!!.setCropRect(overlayView.cropViewRect)
        addView(cropImageView, 0)
    }
}
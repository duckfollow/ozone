package me.duckfollow.ozone.view

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec
import android.widget.LinearLayout


class SquareLinearLayout(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (widthSize == 0 && heightSize == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)

            val minSize = Math.min(measuredWidth, measuredHeight)
            setMeasuredDimension(minSize, minSize)
            return
        }

        val size: Int
        if (widthSize == 0 || heightSize == 0) {
            size = Math.max(widthSize, heightSize)
        } else {
            size = Math.min(widthSize, heightSize)
        }

        val newMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        super.onMeasure(newMeasureSpec, newMeasureSpec)
    }
}
package me.duckfollow.ozone.util

import android.content.res.Resources

/**
 * Created by bruce on 14-11-6.
 */
object Utils {

    fun dp2px(resources: Resources, dp: Float): Float {
        val scale = resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    fun sp2px(resources: Resources, sp: Float): Float {
        val scale = resources.displayMetrics.scaledDensity
        return sp * scale
    }

}
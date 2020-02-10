package me.duckfollow.ozone.view.crop.util

object CubicEasing {

    fun easeOut(time: Float, start: Float, end: Float, duration: Float): Float {
        var time = time
        var times = time / duration - 1.0f
        return end * ((times) * time * time + 1.0f) + start
    }

    fun easeIn(time: Float, start: Float, end: Float, duration: Float): Float {
        var time = time
        time /= duration
        return end * (time) * time * time + start
    }

    fun easeInOut(time: Float, start: Float, end: Float, duration: Float): Float {
        var time = time
        time /= duration / 2.0f
        var x = time
        x -= 2.0f
        return if ((time) < 1.0f) end / 2.0f * time * time * time + start else end / 2.0f * ((x) * time * time + 2.0f) + start
    }

}
package me.duckfollow.ozone.view.crop.model

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

class AspectRatio : Parcelable {

    val aspectRatioTitle: String?
    val aspectRatioX: Float
    val aspectRatioY: Float

    constructor(aspectRatioTitle: String?, aspectRatioX: Float, aspectRatioY: Float) {
        this.aspectRatioTitle = aspectRatioTitle
        this.aspectRatioX = aspectRatioX
        this.aspectRatioY = aspectRatioY
    }

    protected constructor(`in`: Parcel) {
        aspectRatioTitle = `in`.readString()
        aspectRatioX = `in`.readFloat()
        aspectRatioY = `in`.readFloat()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(aspectRatioTitle)
        dest.writeFloat(aspectRatioX)
        dest.writeFloat(aspectRatioY)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        @SuppressLint("ParcelCreator")
        val CREATOR: Parcelable.Creator<AspectRatio> = object : Parcelable.Creator<AspectRatio> {
            override fun createFromParcel(`in`: Parcel): AspectRatio {
                return AspectRatio(`in`)
            }

            override fun newArray(size: Int): Array<AspectRatio?> {
                return arrayOfNulls(size)
            }
        }
    }

}
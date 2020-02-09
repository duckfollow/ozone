package com.yalantis.ucrop.callback;

import android.net.Uri;

public interface BitmapCropCallback {

    void onBitmapCropped(Uri resultUri, int offsetX, int offsetY, int imageWidth, int imageHeight);

    void onCropFailure(Throwable t);

}
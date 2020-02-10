package me.duckfollow.ozone.view.crop

interface UCropFragmentCallback {

    /**
     * Return loader status
     * @param showLoader
     */
    fun loadingProgress(showLoader: Boolean)

    /**
     * Return cropping result or error
     * @param result
     */
    fun onCropFinish(result: UCropFragment.UCropResult)

}
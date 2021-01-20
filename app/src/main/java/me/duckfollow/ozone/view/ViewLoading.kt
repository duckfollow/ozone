package me.duckfollow.ozone.view

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import me.duckfollow.ozone.R

class ViewLoading (val context: Activity){
    fun create():BottomSheetDialog {
        val mView = context.layoutInflater.inflate(R.layout.layout_loading, null)
        val bottomSheetDialogLoading = BottomSheetDialog(context, R.style.BottomSheetDialog)
        bottomSheetDialogLoading.setContentView(mView)
        bottomSheetDialogLoading.setCancelable(false)

        val bottomSheet = bottomSheetDialogLoading.findViewById<View>(R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.peekHeight = Resources.getSystem().getDisplayMetrics().heightPixels* Resources.getSystem().displayMetrics.density.toInt()

        return bottomSheetDialogLoading
    }
}
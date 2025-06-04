package com.yoake.widgets.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import com.yoake.widgets.R

/**
 * loading dialog
 */
class R2LoadingDialog(context: Context) : Dialog(context, R.style.R2LoadingDialog) {
    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        val rootView =
            LayoutInflater.from(context).inflate(R.layout.r2_layout_loading_dialog, null, false)
        setContentView(rootView)
        val window = window
        val lp = window!!.attributes
        lp.gravity = Gravity.CENTER
        window.attributes = lp
    }

}
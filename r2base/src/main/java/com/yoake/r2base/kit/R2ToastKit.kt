package com.yoake.r2base.kit

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.yoake.r2base.R
import com.yoake.r2base.helper.R2ToastHelper

fun toast(context: Context, message: String?, duration: Int = Toast.LENGTH_SHORT) {
    //Android 11 自定义的 Toast 视图已被弃用
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    } else {
        val view = LayoutInflater.from(context).inflate(R.layout.m_toast_text_layout, null)
        val text: TextView = view.findViewById(R.id.tv_message)
        text.text = message
        val toast = Toast(context)
        toast.view = view
        toast.duration = duration
        toast.setGravity(Gravity.CENTER, 0, 0)
        R2ToastHelper.show(toast)
    }

}

fun View.toast(message: String?) {
    toast(context, message)
}

fun View.toast(@StringRes resId: Int) {
    toast(context, resources.getString(resId))
}

fun Context.toast(message: String?) {
    toast(this, message)
}

fun Fragment.toast(message: String) {
    toast(requireContext(), message)
}


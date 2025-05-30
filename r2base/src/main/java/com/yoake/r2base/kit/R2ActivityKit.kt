package com.yoake.r2base.kit


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.WindowManager
import androidx.fragment.app.Fragment


/**
 * Activity 相关的扩展方法
 * @receiver Context
 */


inline fun <reified T> Context.startActivityX(noinline block: (Intent.() -> Unit)? = null) {
    val intent = Intent(this, T::class.java)
    block?.invoke(intent)
    startActivity(intent)
}

fun Context.startActivityX(className: String, block: (Intent.() -> Unit)? = null) {
    val intent = Intent()
    intent.setClassName(this, className)
    block?.invoke(intent)
    startActivity(intent)
}


inline fun <reified T> Context.startServiceX(noinline block: (Intent.() -> Unit)? = null) {
    val intent = Intent(this, T::class.java)
    block?.invoke(intent)
    startService(intent)
}

inline fun <reified T> Fragment.startServiceX(noinline block: (Intent.() -> Unit)? = null) {
    val intent = Intent(requireContext(), T::class.java)
    block?.invoke(intent)
    requireContext().startService(intent)
}

/**
 * 设置window透明度
 */
fun Activity.setWindowAlpha(alpha: Float) {
    val lp = window.attributes
    lp.alpha = alpha
    if (alpha < 1f) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
    window.attributes = lp
}


/**
 * 兼容获取ParcelableExtra
 */
fun <T> Intent.getCompatParcelableExtra(name: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, clazz)
    } else {
        getParcelableExtra(name)
    }
}

fun <T> Bundle.getCompatParcelableExtra(name: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(name, clazz)
    } else {
        getParcelable(name)
    }
}

fun <T : Parcelable> Intent.getCompatParcelableArrayListExtra(
    name: String,
    clazz: Class<T>
): MutableList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(name, clazz)
    } else {
        getParcelableArrayListExtra(name)
    }
}

fun <T : Parcelable> Bundle.getCompatParcelableArrayList(
    name: String,
    clazz: Class<T>
): MutableList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayList(name, clazz)
    } else {
        getParcelableArrayList(name)
    }
}

package com.yoake.r2base.kit

import android.content.Context
import android.view.View
import androidx.annotation.BoolRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


//获取drawable资源
fun Context.drawable(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)
fun Fragment.drawable(@DrawableRes id: Int) = ContextCompat.getDrawable(requireContext(), id)
fun View.drawable(@DrawableRes id: Int) = ContextCompat.getDrawable(context, id)

//获取颜色资源
fun Context.color(@ColorRes id: Int) = ContextCompat.getColor(this, id)
fun Fragment.color(@ColorRes id: Int) = ContextCompat.getColor(requireContext(), id)
fun View.color(@ColorRes id: Int) = ContextCompat.getColor(context, id)

//
fun Context.dimenFloat(@DimenRes resource: Int): Float = resources.getDimension(resource)
fun Fragment.dimenFloat(@DimenRes resource: Int): Float = resources.getDimension(resource)
fun View.dimenFloat(@DimenRes resource: Int): Float = resources.getDimension(resource)

//
fun Context.bool(@BoolRes resource: Int): Boolean = resources.getBoolean(resource)
fun Fragment.bool(@BoolRes resource: Int): Boolean = resources.getBoolean(resource)
fun View.bool(@BoolRes resource: Int): Boolean = resources.getBoolean(resource)
//

fun Context.integer(@IntegerRes resource: Int): Int = resources.getInteger(resource)
fun Fragment.integer(@IntegerRes resource: Int): Int = resources.getInteger(resource)
fun View.integer(@IntegerRes resource: Int): Int = resources.getInteger(resource)

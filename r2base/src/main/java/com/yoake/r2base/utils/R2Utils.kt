package com.yoake.r2base.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import com.yoake.r2base.kit.dip

import java.util.regex.Matcher
import java.util.regex.Pattern

object R2Utils {
    /**
     * 网络连接的状态
     */
    @JvmStatic
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                // 其他网络类型，如蓝牙、以太网等
                else -> false
            }
        } else {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            return networkInfo?.isConnected == true
        }
    }


    @JvmStatic
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    @JvmStatic
    fun dp2px(context: Context, value: Int): Int {
        return context.dip(value)
    }


    /**
     * 对目标Drawable 进行着色
     * @param drawable Drawable
     * @param color Int
     * @return Drawable
     */
    @JvmStatic
    fun tintDrawable(drawable: Drawable, color: Int): Drawable {
        val wrappedDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(wrappedDrawable, color)
        return wrappedDrawable
    }

    /**
     * 构建一个启用、禁用状态的selector color
     */
    @JvmStatic
    fun buildEnableStateColor(color: Int, alpha: Float = 0.8f): ColorStateList {
        val colors = intArrayOf(color, setColorAlpha(color, alpha))
        val states = arrayOfNulls<IntArray>(2)
        states[0] = intArrayOf(android.R.attr.state_enabled)
        states[1] = intArrayOf()
        return ColorStateList(states, colors)
    }

    /**
     * 构建一个启用、禁用状态的selector drawable
     */
    @JvmStatic
    fun buildEnableStateDrawable(color: Int, corner: Float, alpha: Float = 0.8f): GradientDrawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = corner
        drawable.color = buildEnableStateColor(color, alpha)
        return drawable
    }

    @JvmStatic
    fun buildShape(@ColorInt color: Int, radius: Float): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color) // 背景颜色
            cornerRadius = radius // 圆角半径
        }
    }

    @JvmStatic
    fun buildShape(
        @ColorInt color: Int,
        radii: FloatArray,
        strokeWidth: Int,
        @ColorInt strokeColor: Int
    ): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color) // 背景颜色
            setCornerRadii(radii) // 圆角半径
            setStroke(strokeWidth, strokeColor)
        }
    }


    /**
     *解析颜色值 DEBUG时异常返回绿色 便于观察 Release返回黑色  确保不闪退
     */
    @JvmStatic
    fun parseColor(colorString: String?, @ColorInt default: Int = Color.BLACK): Int {
        return try {
            Color.parseColor(colorString)
        } catch (e: Exception) {
            e.printStackTrace()
            default
        }
    }

    /**
     * 字符串解析成颜色值
     */
    @ColorInt
    fun parseColor(colorString: String?, alpha: Float, @ColorInt default: Int = Color.BLACK): Int {
        return try {
            val color = Color.parseColor(colorString)
            if (alpha == 1f) {
                color
            } else {
                setColorAlpha(color, alpha)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            default
        }
    }

    /**
     * 设置颜色透明度
     */
    private fun setColorAlpha(color: Int, alpha: Float): Int {
        val clampedAlpha = when {
            alpha < 0f -> 0f
            alpha > 1f -> 1f
            else -> alpha
        }
        val alphaValue = (clampedAlpha * 255).toInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alphaValue, red, green, blue)
    }

    /**
     * 复制bitmap
     */
    @JvmStatic
    fun copyBitmapDrawable(resources: Resources, drawable: BitmapDrawable): BitmapDrawable? {
        return try {
            val originalBitmap = drawable.bitmap
            val copiedBitmap =
                originalBitmap.copy(originalBitmap.config!!, originalBitmap.isMutable)
            BitmapDrawable(resources, copiedBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    /**
     * 是否有有效的电话号码
     */
    @JvmStatic
    fun isValidPhoneNumber(phoneNumber: String?): Boolean {
        phoneNumber?.let {
            val pattern = "^(13[0-9]|15[012356789]|17[013678]|18[0-9]|14[57]|19[89]|166)[0-9]{8}"
            val r: Pattern = Pattern.compile(pattern)
            val m: Matcher = r.matcher(it)
            return m.matches()
        } ?: return false
    }


    /**
     * 获取设备名称
     */
    @JvmStatic
    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else {
            "${capitalize(manufacturer)} $model"
        }
    }

    private fun capitalize(s: String): String {
        if (s.isEmpty()) {
            return ""
        }
        val firstChar = s[0]
        return if (Character.isUpperCase(firstChar)) {
            s
        } else {
            Character.toUpperCase(firstChar) + s.substring(1)
        }
    }

    /**
     * 将View从父控件中移除
     */
    @JvmStatic
    fun removeViewFormParent(v: View?) {
        if (v == null) return
        val parent = v.parent
        if (parent is ViewGroup) {
            parent.removeView(v)
        }
    }


}
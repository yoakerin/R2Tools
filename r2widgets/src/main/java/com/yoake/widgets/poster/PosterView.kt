package com.yoake.widgets.poster

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import com.yoake.graphic.utils.load
import com.yoake.graphic.utils.loadBlur

import com.yoake.widgets.R
import com.yoake.widgets.layout.R2FrameLayout


class PosterView(context: Context, attrs: AttributeSet? = null) :
    R2FrameLayout(context, attrs) {
    companion object {
        //这里不要动
        const val CUT_FIT_CENTER = 1
        const val CUT_CENTER_CROP = 2
        var imgCut = CUT_FIT_CENTER
        var placeholder: Drawable? = null
        var error: Drawable? = null
        var imgRadius = 18
    }

    private val imageView = ImageView(context)
    private val backView: ImageView by lazy(LazyThreadSafetyMode.NONE) {
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_XY
        }
    }

    init {
        if (isInEditMode) {
            imageView.setImageResource(R.drawable.tools_preview)
        }
        when (imgCut) {
            CUT_CENTER_CROP -> {
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            }

            else -> {
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                addView(backView, -1, -1)
            }
        }
        addView(imageView, -1, -1)
    }


    fun loadUrl(url: String?, isRoundCorner: Boolean = true): PosterView {
        if (isRoundCorner) {
            radius = imgRadius
        }
        if (imgCut == CUT_FIT_CENTER) {
            backView.loadBlur(url = url, radius = 15, overrideSize = 40)
        }
        imageView.load(url = url, placeholder = placeholder, error = error)
        return this
    }
}
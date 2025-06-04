package com.yoake.graphic.utils

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.IntRange
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.yoake.graphic.R2GraphicLoader
//import com.yoake.graphic.config.GlideApp


fun ImageView.preload(url: String?) {
    //GlideApp.with(context).load(url).preload()
    Glide.with(context).load(url).preload()
}

fun ImageView.load(
    url: String?,
    default: Drawable? = null,
    transitionAnim: Boolean = true,
    overrideSize: Int = -1,
    listener: RequestListener<Drawable>? = null
) {
    load(url, default, default, transitionAnim, overrideSize, listener)

}

fun ImageView.load(
    url: String?,
    placeholder: Drawable? = null,
    error: Drawable? = null,
    transitionAnim: Boolean = true,
    overrideSize: Int = -1,
    listener: RequestListener<Drawable>? = null
) {
    R2GraphicLoader.load(this, url, placeholder, error, transitionAnim, overrideSize, listener)
}


fun ImageView.loadCircle(
    url: String?,
    default: Drawable? = null,
    overrideSize: Int = -1,
    transitionAnim: Boolean = true,
    listener: RequestListener<Drawable>? = null
) {
    loadCircle(url, default, default, transitionAnim, overrideSize, listener)
}


fun ImageView.loadCircle(
    url: String?,
    placeholder: Drawable? = null,
    error: Drawable? = null,
    transitionAnim: Boolean = true,
    overrideSize: Int = -1,
    listener: RequestListener<Drawable>? = null
) {
    R2GraphicLoader.loadCircle(this, url, placeholder, error, transitionAnim, overrideSize, listener)
}


fun ImageView.loadRoundCorner(
    url: String?,
    @IntRange(from = 1) radius: Int,
    default: Drawable? = null,
    transitionAnim: Boolean = true,
    overrideSize: Int = -1,
    listener: RequestListener<Drawable>? = null
) {
    loadRoundCorner(url, radius, default, default, transitionAnim, overrideSize, listener)
}

fun ImageView.loadRoundCorner(
    url: String?,
    @IntRange(from = 1) radius: Int,
    placeholder: Drawable? = null,
    error: Drawable? = null,
    transitionAnim: Boolean = true,
    overrideSize: Int = -1,
    listener: RequestListener<Drawable>? = null
) {
    R2GraphicLoader.loadRoundCorner(
        this, url, radius, placeholder, error, transitionAnim, overrideSize, listener
    )
}

fun ImageView.loadBlur(
    url: String?,
    @IntRange(from = 1, to = 25) radius: Int = 20,
    placeholder: Drawable? = null,
    error: Drawable? = null,
    transitionAnim: Boolean = true,
    overrideSize: Int = -1,
    listener: RequestListener<Drawable>? = null
) {
    R2GraphicLoader.loadBlur(
        this, url, radius, placeholder, error, transitionAnim, overrideSize, listener
    )
}


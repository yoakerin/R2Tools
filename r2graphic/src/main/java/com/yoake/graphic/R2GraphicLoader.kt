package com.yoake.graphic

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.IntRange
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.yoake.graphic.utils.HolderDrawable
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * 图像加载的基类
 */
object R2GraphicLoader {


    fun <Y : Target<Drawable>> load(
        context: Context, any: Any?, options: RequestOptions?, target: Y
    ): Y {
        val request = Glide.with(context).load(any)
        options?.let {
            request.apply(options)
        }
        return request.into(target)
    }

    /**
     * 加载图片
     * @param iv ImageView
     * @param any Any?
     * @param placeholder Drawable?
     * @param error Drawable?
     * @param transitionAnim Boolean
     * @param listener GraphicLoadingResultListener?
     */
    fun load(
        iv: ImageView,
        any: Any?,
        placeholder: Drawable? = null,
        error: Drawable? = null,
        transitionAnim: Boolean = true,
        overrideSize: Int = -1,
        listener: RequestListener<Drawable>? = null
    ) {
        baseRequest(
            iv.context, any, 0, placeholder, error, transitionAnim, overrideSize, listener
        ).into(iv)
    }


    /**
     * 加载图片并进行圆形裁剪
     * @param iv ImageView
     * @param any: Any?,
     * @param placeholder Drawable?
     * @param error Drawable?
     * @param transitionAnim Boolean
     * @param listener GraphicLoadingResultListener?
     */
    fun loadCircle(
        iv: ImageView,
        any: Any?,
        placeholder: Drawable? = null,
        error: Drawable? = null,
        transitionAnim: Boolean = true,
        overrideSize: Int = -1,
        listener: RequestListener<Drawable>? = null
    ) {

        baseRequest(
            iv.context, any, -1, placeholder, error, transitionAnim, overrideSize, listener
        ).circleCrop().into(iv)
    }

    /**
     * 加载图片并进行圆角处理
     * @param iv ImageView
     * @param url String?
     * @param radius Int
     * @param placeholder Drawable?
     * @param error Drawable?
     * @param transitionAnim Boolean
     * @param listener GraphicLoadingResultListener?
     */

    fun loadRoundCorner(
        iv: ImageView,
        url: String?,
        radius: Int,
        placeholder: Drawable?,
        error: Drawable?,
        transitionAnim: Boolean = true,
        overrideSize: Int = -1,
        listener: RequestListener<Drawable>?
    ) {

        baseRequest(
            iv.context, url, radius, placeholder, error, transitionAnim, overrideSize, listener
        ).transform(
            CenterCrop(), RoundedCornersTransformation(radius, 0)
        ).into(iv)
    }

    /**
     * 加载图片并进行高斯模糊
     * @param iv ImageView
     * @param any: Any?,
     * @param radius Int
     * @param placeholder Drawable?
     * @param error Drawable?
     * @param transitionAnim Boolean
     * @param listener GraphicLoadingResultListener?
     */
    fun loadBlur(
        iv: ImageView,
        any: Any?,
        @IntRange(from = 1, to = 25) radius: Int,
        placeholder: Drawable?,
        error: Drawable?,
        transitionAnim: Boolean = true,
        overrideSize: Int = -1,
        listener: RequestListener<Drawable>?
    ) {
        baseRequest(
            iv.context, any, 0, placeholder, error, transitionAnim, overrideSize, listener
        ).transform(
            BlurTransformation(radius, 1)
        ).into(iv)
    }

    /**
     *
     * @param context Context
     * @param any Any?
     * @param radius Int  -1=圆形  0=矩形 其它正数=圆角矩形
     * @param placeholder Drawable?
     * @param error Drawable?
     * @param transitionAnim Boolean
     * @param listener GraphicLoadingResultListener?
     * @return GlideRequest<*>
     */
    private fun baseRequest(
        context: Context,
        any: Any?,
        radius: Int,
        placeholder: Drawable? = null,
        error: Drawable? = null,
        transitionAnim: Boolean = true,
        overrideSize: Int = -1,
        listener: RequestListener<Drawable>? = null
    ): RequestBuilder<Drawable> {
        val placeholderWrap = placeholder?.let {
            if (it is BitmapDrawable && it.bitmap.height > 0 && it.bitmap.width > 0) {
                HolderDrawable(0, it, radius)
            } else {
                it
            }
        }

        val errorWrap = error?.let {
            if (error == placeholder) {
                placeholderWrap
            } else {
                if (it is BitmapDrawable && it.bitmap.height > 0 && it.bitmap.width > 0) {
                    HolderDrawable(1, it, radius)
                } else {
                    it
                }
            }
        }

        //  var request = GlideApp.with(context).load(any).addListener(this.LISTENER)
        var request = Glide.with(context).load(any).format(DecodeFormat.PREFER_RGB_565)
        //.load("http://pic1.win4000.com/wallpaper/2019-12-30/5e0968b5a3701.jpg")

        placeholderWrap?.let {
            request = request.placeholder(placeholderWrap)
        }
        errorWrap?.let {
            request = request.error(errorWrap)
        }
        if (overrideSize != -1) {
            request = request.override(overrideSize)
        }
        listener?.let {
            request = request.listener(listener)
        }
        if (transitionAnim) {
            // 淡入效果默认时禁用交叉淡入的，但当待加载的图片包含透明像素时仍然可能造成问题。
            // 当占位符比实际加载的图片要大，或者图片部分为透明时，禁用交叉淡入会导致动画完成后占位符在图片后面仍然可见。
            val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
            request = request.transition(DrawableTransitionOptions.withCrossFade(factory))
        }
        return request

    }

}
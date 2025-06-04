package com.yoake.graphic.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.yoake.graphic.BuildConfig


/**
 *  Glide进行placeholder和image进行过渡时,使用普通的资源placeholder会变形
 * 而这个类对图片资源进行了处理,使过渡前后placeholder一致
 *
 * @property type Int  placeHolder和error可能是不同的图 用于区分一下
 * @property radius Int -1圆形 0矩形 其它正数圆角矩形
 * @property mPaint Paint
 * @property resource Bitmap
 * @constructor
 */
class HolderDrawable(private val type: Int, drawable: BitmapDrawable, private val radius: Int) :
    Drawable() {
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    companion object {
        //大于这个阈值才压缩  相差很小的没必要压缩 浪费
        private const val SCALE_THRESHOLD = 40
        private val TAG: String = HolderDrawable::class.java.simpleName
    }

    var resource: Bitmap = drawable.bitmap

    /**
     *  根据画布的尺寸 计算处 需要绘制bitmap的内容范围
     *
     * @param canvas Canvas
     */
    override fun draw(canvas: Canvas) {

        val canvasWidth = bounds.width()
        val canvasHeight = bounds.height()

        val key = HolderDrawableCache.ofKey(
            type, canvasWidth, canvasHeight
        )
        var cache = HolderDrawableCache.getInstance().getBitmapFromCache(key)
        if (cache == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "$key 无缓存")
            }
            resource = scaledBitmap(resource)
            cache = centerCrop(resource, radius)
            HolderDrawableCache.getInstance().addBitmapToCache(key, cache)
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "$key 使用缓存")
            }
        }
        canvas.drawBitmap(
            cache,
            Rect(0, 0, cache.width, cache.height),
            Rect(0, 0, canvasWidth, canvasHeight),
            mPaint
        )
    }


    /**
     * 中心裁剪
     * @param bitmap Bitmap
     * @return Bitmap
     */
    private fun centerCrop(bitmap: Bitmap, radius: Int): Bitmap {
        try {
            val centerX = bitmap.width / 2
            val centerY = bitmap.height / 2
            //drawable宽高比
            val ratio = bitmap.width * 1f / bitmap.height
            val canvasRadio = bounds.width() * 1f / bounds.height()
            val targetWidth: Int
            val targetHeight: Int
            //用裁剪矩形的Rect
            val croppedBitmap = if (ratio == canvasRadio) {
                when (radius) {
                    0 -> bitmap
                    -1 -> circle(bitmap, Rect(0, 0, bitmap.width, bitmap.height))
                    else -> radius(
                        bitmap,
                        radius.toFloat(),
                        Rect(0, 0, bitmap.width, bitmap.height)
                    )
                }
            } else if (ratio > canvasRadio) {
                //以高为基准,进行中心裁剪
                targetWidth = (bitmap.height * canvasRadio).toInt()
                targetHeight = bitmap.height
                when (radius) {
                    0 -> Bitmap.createBitmap(
                        bitmap,
                        centerX - targetWidth / 2,
                        0,
                        targetWidth,
                        targetHeight
                    )

                    -1 -> circle(
                        bitmap,
                        Rect(centerX - targetWidth / 2, 0, centerX + targetWidth / 2, targetHeight)
                    )

                    else -> radius(
                        bitmap,
                        radius.toFloat(),
                        Rect(centerX - targetWidth / 2, 0, centerX + targetWidth / 2, targetHeight)
                    )

                }
            } else {
                //以宽为基准,进行中心裁剪
                targetWidth = bitmap.width
                targetHeight = (bitmap.width / canvasRadio).toInt()
                when (radius) {
                    0 -> Bitmap.createBitmap(
                        bitmap,
                        0,
                        centerY - targetHeight / 2,
                        targetWidth,
                        targetHeight
                    )

                    -1 -> circle(
                        bitmap,
                        Rect(0, centerY - targetHeight / 2, targetWidth, centerY + targetHeight / 2)
                    )

                    else -> radius(
                        bitmap,
                        radius.toFloat(),
                        Rect(0, centerY - targetHeight / 2, targetWidth, centerY + targetHeight / 2)
                    )

                }
            }

            return croppedBitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap


    }


    private fun radius(bitmap: Bitmap, radius: Float, rect: Rect): Bitmap {
        // 创建裁剪后的 Bitmap 对象
        val croppedBitmap =
            Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(croppedBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // 绘制圆角矩形
        canvas.drawRoundRect(
            0f,
            0f,
            bounds.width().toFloat(),
            bounds.height().toFloat(),
            radius,
            radius,
            paint
        )
        // 设置绘制模式，保留裁剪区域内的图像
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        // 绘制裁剪后的图像
        canvas.drawBitmap(bitmap, rect, Rect(0, 0, bounds.width(), bounds.height()), paint)
        canvas.setBitmap(null)
        return croppedBitmap
    }

    private fun circle(bitmap: Bitmap, rect: Rect): Bitmap {
        try {
            val width: Int = bitmap.width
            val height: Int = bitmap.height
            val diameter = width.coerceAtMost(height)
            // 创建裁剪后的 Bitmap 对象
            val croppedBitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(croppedBitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            // 绘制圆形
            val radius = diameter / 2f
            canvas.drawCircle(radius, radius, radius, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            // 绘制裁剪后的图像
            canvas.drawBitmap(bitmap, rect, Rect(0, 0, bounds.width(), bounds.height()), paint)
            canvas.setBitmap(null)
            return croppedBitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    /**
     * 根据画布的尺寸进行压缩
     * @param originalBitmap Bitmap
     * @return Bitmap
     */
    private fun scaledBitmap(originalBitmap: Bitmap): Bitmap {
        try {
            val originalWidth: Int = originalBitmap.width
            val originalHeight: Int = originalBitmap.height
            val canvasWidth = bounds.width()
            val canvasHeight = bounds.height()
            val ratio = originalBitmap.width * 1f / originalBitmap.height
            val canvasRadio = bounds.width() * 1f / bounds.height()
            if (ratio > canvasRadio) {
                //bitmap的高度大于了画布的高度 就以画布高为基准进行压缩
                if (originalHeight > canvasHeight && originalHeight - canvasHeight > SCALE_THRESHOLD) {
                    val scaleFactor = canvasHeight * 1f / originalHeight
                    val targetWidth: Int = (scaleFactor * originalWidth).toInt()
                    return Bitmap.createScaledBitmap(
                        originalBitmap,
                        targetWidth,
                        canvasHeight,
                        true
                    )
                }
            } else {
                if (originalWidth > canvasWidth && originalWidth - canvasWidth > SCALE_THRESHOLD) {
                    val scaleFactor = canvasWidth * 1f / originalWidth
                    val targetHeight: Int = (scaleFactor * originalHeight).toInt()
                    return Bitmap.createScaledBitmap(
                        originalBitmap,
                        canvasWidth,
                        targetHeight,
                        true
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
        }

        return originalBitmap
    }

    override fun setAlpha(i: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }
}
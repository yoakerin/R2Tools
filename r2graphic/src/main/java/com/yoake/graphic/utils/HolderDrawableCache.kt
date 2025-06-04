package com.yoake.graphic.utils

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import com.yoake.graphic.BuildConfig


/**
 *
 * 简单的的封装用于缓存HolderDrawable中的bitmap
 */
class HolderDrawableCache private constructor() {

    private val cache: LruCache<String, Bitmap> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        // 使用最大可用内存值的1/32作为缓存的大小。
        val cacheSize = (Runtime.getRuntime().maxMemory() / 1024).toInt() / 16
        object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                val size = value.rowBytes * value.height / 1024
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "sizeOf $size")
                }
                return size
            }
        }

    }

    companion object {
        private val TAG: String = HolderDrawableCache::class.java.simpleName
        private val INSTANCE by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            HolderDrawableCache()
        }

        fun getInstance(): HolderDrawableCache = INSTANCE


        /**
         * 这里仅判断宽高，Placeholder
         * @param width Int
         * @param height Int
         * @return String
         */

        fun ofKey(type: Int, width: Int, height: Int): String {
            return "${type}_${width}x${height}"
        }
    }

    fun addBitmapToCache(key: String?, bitmap: Bitmap?) {
        if (getBitmapFromCache(key) == null) {
            cache.put(key, bitmap)
        }
    }

    fun getBitmapFromCache(key: String?): Bitmap? {
        return cache[key]
    }

    fun clearCache() {
        cache.evictAll()
    }


}
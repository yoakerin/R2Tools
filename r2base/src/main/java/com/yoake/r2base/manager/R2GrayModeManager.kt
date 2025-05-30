package com.yoake.r2base.manager

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.View

/**
 * 灰色模式
 * @property mGrayPaint Paint
 * @property mGrayMatrix ColorMatrix
 */
class R2GrayModeManager private constructor() {
    private val mGrayPaint by lazy(LazyThreadSafetyMode.NONE) {
        Paint()
    }
    private val mGrayMatrix by lazy(LazyThreadSafetyMode.NONE) {
        ColorMatrix()
    }

    //初始化
    init {
        mGrayMatrix.setSaturation(0f)
        mGrayPaint.colorFilter = ColorMatrixColorFilter(mGrayMatrix)
    }

    //硬件加速置灰方法
    fun setLayerGrayType(view: View) {
        view.setLayerType(View.LAYER_TYPE_HARDWARE, mGrayPaint)
    }

    companion object {


        private val INSTANCE: R2GrayModeManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            R2GrayModeManager()
        }

        fun getInstance(): R2GrayModeManager {
            return INSTANCE
        }
    }
}
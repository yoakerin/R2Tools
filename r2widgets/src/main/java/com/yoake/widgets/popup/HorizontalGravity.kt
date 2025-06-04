package com.yoake.widgets.popup

import androidx.annotation.IntDef
import com.yoake.widgets.popup.HorizontalGravity


/**
 * 水平布局方式
 *
 * @author xuexiang
 * @since 2019/1/14 下午10:07
 */
@IntDef(
    HorizontalGravity.CENTER,
    HorizontalGravity.LEFT,
    HorizontalGravity.RIGHT,
    HorizontalGravity.ALIGN_LEFT,
    HorizontalGravity.ALIGN_RIGHT

)
@Retention(AnnotationRetention.SOURCE)
annotation class HorizontalGravity {
    companion object {
        const val CENTER: Int = 0
        const val LEFT: Int = 1
        const val RIGHT: Int = 2
        const val ALIGN_LEFT: Int = 3
        const val ALIGN_RIGHT: Int = 4
    }
}
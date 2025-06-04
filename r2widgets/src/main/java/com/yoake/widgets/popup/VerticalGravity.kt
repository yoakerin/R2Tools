package com.yoake.widgets.popup

import androidx.annotation.IntDef


/**
 * 垂直布局方式
 *
 * @author xuexiang
 * @since 2019/1/14 下午10:05
 */
@IntDef(
    VerticalGravity.CENTER,
    VerticalGravity.ABOVE,
    VerticalGravity.BELOW,
    VerticalGravity.ALIGN_TOP, VerticalGravity.ALIGN_BOTTOM

)
@Retention(AnnotationRetention.SOURCE)
annotation class VerticalGravity {
    companion object {
        const val CENTER: Int = 0
        const val ABOVE: Int = 1
        const val BELOW: Int = 2
        const val ALIGN_TOP: Int = 3
        const val ALIGN_BOTTOM: Int = 4
    }
}
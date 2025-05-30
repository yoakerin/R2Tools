package com.yoake.r2base.obj.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * 仅支持每行个数不变单一的宫格
 */
class R2GridItemDecoration(
    var spanCount: Int,
    var horizontalSpace: Int,
    var verticalSpace: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // 获取item在Adapter中的位置
        val column = position % spanCount // 计算item所在列数
        if (includeEdge) {
            outRect.left = horizontalSpace - column * horizontalSpace / spanCount // 左偏移量
            outRect.right = (column + 1) * horizontalSpace / spanCount // 右偏移量
            if (position < spanCount) {
                outRect.top = verticalSpace // 顶部偏移量
            }
            outRect.bottom = verticalSpace // 底部偏移量
        } else {
            outRect.left = column * horizontalSpace / spanCount // 左偏移量
            outRect.right = horizontalSpace - (column + 1) * horizontalSpace / spanCount // 右偏移量
            if (position >= spanCount) {
                outRect.top = verticalSpace // 顶部偏移量
            }
        }
    }
}
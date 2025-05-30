package com.yoake.r2base.obj.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 用于横向LinearLayoutManager的
 */
class R2LinearItemDecoration(private val itemSpacing: Int, private val edgeSpacing: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        // 获取当前 View 在 RecyclerView 中的位置
        val position = parent.getChildAdapterPosition(view)

        // 只对 LinearLayoutManager 生效
        if (parent.layoutManager is LinearLayoutManager) {
            val layoutManager = parent.layoutManager as LinearLayoutManager

            // 设置左侧边缘距离
            if (position == 0) {
                outRect.left = edgeSpacing
            }

            // 设置右侧边缘距离
            if (position == parent.adapter!!.itemCount - 1) {
                outRect.right = edgeSpacing
            } else {
                // 设置 item 之间的距离
                outRect.right = itemSpacing
            }
        }
    }

}

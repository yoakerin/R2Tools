package com.yoake.r2base.obj.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 高级的可以支持不同行数显示不同个数
 * 如果每行行数一致的 请用[R2GridItemDecoration] 内部计算量小很多
 */
class R2AdvGridItemDecoration(
    var horizontalSpace: Int,
    var verticalSpace: Int,
    var topBottomOut: Int = 0,
    var leftRightOut: Int = 0

) : RecyclerView.ItemDecoration() {
    private var mSpanCount = -1
    private var lastRowFirstIndex = -1

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        try {
            if (parent.layoutManager is GridLayoutManager) {
                val layoutManager = parent.layoutManager as GridLayoutManager
                val layoutParams = view.layoutParams as GridLayoutManager.LayoutParams
                mSpanCount = layoutManager.spanCount //Grid列数(固定不变)，初始化GridLayoutManager时传入的
                val spanSize = layoutParams.spanSize //设置setSpanSizeLookup后，这个数值等于setSpanSizeLookup的
                val itemPosition = parent.getChildAdapterPosition(view) //view在适配器里的位置
                val currentColumn = layoutParams.spanIndex //view在当前行的列数下标
                val currentRow = layoutManager.spanSizeLookup.getSpanGroupIndex(
                    itemPosition,
                    mSpanCount
                ) //当前所处行,从0行计数
                if (lastRowFirstIndex == -1) {
                    val itemCount = layoutManager.itemCount
                    lastRowFirstIndex =
                        if (currentColumn == 0 && itemPosition + mSpanCount >= itemCount) {
                            currentRow
                        } else {
                            -1
                        }
                }
                val hafHorizontalSpace = horizontalSpace / 2
                if (currentColumn == 0 && spanSize == mSpanCount) { //特殊列
                    outRect.left = leftRightOut
                    outRect.right = leftRightOut
                } else if (currentColumn == 0) { //第一列
                    outRect.left = leftRightOut
                    outRect.right = hafHorizontalSpace
                } else if (currentColumn == mSpanCount - 1) { //最后一列
                    outRect.left = hafHorizontalSpace
                    outRect.right = leftRightOut
                } else {
                    outRect.left = hafHorizontalSpace
                    outRect.right = hafHorizontalSpace
                }
                when (currentRow) {
                    0 -> { //第一行
                        outRect.top = topBottomOut
                    }

                    lastRowFirstIndex -> { //最后一行
                        outRect.top = verticalSpace
                        outRect.bottom = topBottomOut
                    }

                    else -> {
                        outRect.top = verticalSpace
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
package com.yoake.widgets.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class R2MediumTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {
    private val mStrokeWidth = 1f
    private var flag: Boolean = true


    override fun onDraw(canvas: Canvas) {
        if (flag) {
            paint.strokeWidth = mStrokeWidth
            paint.style = Paint.Style.FILL_AND_STROKE
        }
        super.onDraw(canvas)
    }

    fun setMedium(flag: Boolean) {
        this.flag = flag
        invalidate()
    }
}
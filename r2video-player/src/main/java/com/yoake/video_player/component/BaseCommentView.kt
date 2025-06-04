package com.yoake.video_player.component

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import com.yoake.video_player.util.OperationEventListener
import xyz.doikki.videoplayer.controller.ControlWrapper
import xyz.doikki.videoplayer.controller.IControlComponent

abstract class BaseCommentView(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), IControlComponent {
    @JvmField
    var operationEventListener: OperationEventListener? = null
    @JvmField
    protected var mControlWrapper: ControlWrapper? = null

    @CallSuper
    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView() = this


    override fun setProgress(duration: Int, position: Int) {}

    override fun onLockStateChanged(isLocked: Boolean) {}

}
package com.yoake.video_player.controller

import android.content.Context
import android.util.AttributeSet
import com.yoake.video_player.component.BaseCommentView
import com.yoake.video_player.util.OperationEventListener
import xyz.doikki.videoplayer.controller.GestureVideoController

/**
 * 对播控进行扩展
 */
abstract class VideoControllerExt(context: Context, attrs: AttributeSet? = null) :
    GestureVideoController(context, attrs), IController {
    var operationEventListener: OperationEventListener? = null
        set(value) {
            field = value
            mControlComponents.forEach {
                val controlComponent = it.key
                if (controlComponent is BaseCommentView) {
                    controlComponent.operationEventListener = value
                }
            }
        }

}
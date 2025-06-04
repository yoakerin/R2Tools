package com.yoake.video_player.util

import android.content.Context
import xyz.doikki.videoplayer.controller.ControlWrapper
import xyz.doikki.videoplayer.util.PlayerUtils

 object VideoControlHelper {
    @JvmStatic
    fun toggleFullScreen(context: Context, mControlWrapper: ControlWrapper) {
        val activity = PlayerUtils.scanForActivity(context)
        val mVideoSize = mControlWrapper.videoSize
        if (mVideoSize != null && mVideoSize.size == 2) {
            if (mVideoSize[0] < mVideoSize[1]) {
                if (activity == null || activity.isFinishing) return
                if (mControlWrapper.isFullScreen) {
                    mControlWrapper.stopFullScreen()
                } else {
                    mControlWrapper.startFullScreen()
                }
            } else {
                mControlWrapper.toggleFullScreen(activity)
            }
        } else {
            mControlWrapper.toggleFullScreen(activity)
        }

    }
}
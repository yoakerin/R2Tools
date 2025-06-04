package com.yoake.video_player.util

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import com.yoake.video_player.VideoPlayer
import com.yoake.video_player.controller.StandardVideoController
import xyz.doikki.videoplayer.player.VideoViewManager

class ActivityPlayerHelper {
    private var mPIPManager: PIPManager? = null
    var player: VideoPlayer? = null
    private var controller: StandardVideoController? = null
    private var fromFloat = false
    fun init(activity: ComponentActivity, playerContainer: ViewGroup, isLive: Boolean = false) {
        mPIPManager = PIPManager.getInstance(activity)
        mPIPManager?.bindLifecycleOwner(activity)
        player = VideoViewManager.instance().get(PIPManager.PIP) as VideoPlayer
        controller = StandardVideoController(activity)
        player?.setVideoController(controller)
        controller?.apply {
            addDefaultControlComponent(isLive)
            setFloatWindowEnabled(true)
            setPlayerState(player!!.currentPlayerState)
            setPlayState(player!!.currentPlayState)
        }
        mPIPManager?.apply {
            if (isShowing) {
                fromFloat = true
                stopFloatWindow()
            } else {
                actClass = activity::class.java
            }
        }

        playerContainer.addView(player)
    }

    fun setVideoInfo(videoInfo: VideoInfo) {
        controller?.setVideoInfo(videoInfo)
        player?.setVideoInfo(videoInfo, !fromFloat)
        fromFloat = false
    }

    fun onBackPress(): Boolean {
        return mPIPManager?.onBackPress() ?: false
    }

    fun onActivityResult(activity: ComponentActivity, requestCode: Int) {
        if (requestCode == PIPManager.REQUEST_OVERLAY_CODE) {
            mPIPManager?.onOverlayPermission(activity)
        }
    }
}
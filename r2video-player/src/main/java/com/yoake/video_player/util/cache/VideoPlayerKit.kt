package com.yoake.video_player.util.cache

import android.content.Context
import com.yoake.r2base.utils.R2Utils
import com.yoake.video_player.VideoPlayer
import com.yoake.video_player.controller.StandardVideoController
import com.yoake.video_player.util.VideoInfo


object VideoPlayerKit {
    fun setStandardVideoController(
        context: Context,
        player: VideoPlayer,
        videoInfo: VideoInfo,
        resetPlayer: Boolean,
    ): VideoPlayer {
        val controller = StandardVideoController(context!!)
        player.setVideoController(controller)
        controller.addDefaultControlComponent(false)
        controller.setFloatWindowEnabled(true)
        controller.setPlayerState(player.currentPlayerState)
        controller.setPlayState(player.currentPlayState)
        controller.setVideoInfo(videoInfo)
        player.setVideoInfo(videoInfo, resetPlayer)
        R2Utils.removeViewFormParent(player)
        return player
    }
}

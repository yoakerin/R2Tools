package com.yoake.video_player.controller

import com.yoake.video_player.util.VideoInfo

interface IController {

    fun setPoster(thumb: String?)
    fun setVideoInfo(videoInfo: VideoInfo?)
    fun getVideoInfo(): VideoInfo?
    fun setFloatWindowEnabled(enable: Boolean)
}
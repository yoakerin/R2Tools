package com.yoake.r2tools

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yoake.video_player.VideoPlayer
import com.yoake.video_player.controller.StandardVideoController
import com.yoake.video_player.util.ActivityPlayerHelper
import com.yoake.video_player.util.PIPManager
import com.yoake.video_player.util.VideoInfo
import kotlinx.android.synthetic.main.activity_video.playerContainer
import xyz.doikki.videoplayer.player.VideoViewManager

class VideoActivity : AppCompatActivity() {

    private val activityPlayerHelper = ActivityPlayerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        activityPlayerHelper.init(this, playerContainer)
        activityPlayerHelper.player?.autoPlay = true
        activityPlayerHelper.player?.openCache = false
        val videoInfo = VideoInfo(
            "测试",
            "https://img2.baidu.com/it/u=4256758710,3144398338&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500"
        )
        videoInfo.lines.add(
            VideoInfo.Line(
                "标准",
                "https://stream7.iqilu.com/10339/upload_transcode/202002/09/20200209105011F0zPoYzHry.mp4"
            )
        )
        activityPlayerHelper.setVideoInfo(videoInfo)
    }

    override fun onBackPressed() {
        if (activityPlayerHelper.onBackPress()) return
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityPlayerHelper.onActivityResult(this, resultCode)
    }
}
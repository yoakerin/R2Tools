package com.yoake.video_player

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.yoake.video_player.controller.VideoControllerExt

import com.yoake.video_player.util.cache.ProxyVideoCacheManager
import com.yoake.video_player.util.VideoInfo
import com.yoake.video_player.util.VideoPlayerConfig
import com.danikula.videocache.HttpProxyCacheServer
import com.yoake.r2base.R2Log
import com.yoake.video_player.controller.StandardVideoController
import xyz.doikki.videoplayer.BuildConfig
import xyz.doikki.videoplayer.player.BaseVideoView
import xyz.doikki.videoplayer.player.VideoView


open class VideoPlayer(context: Context, attrs: AttributeSet? = null) : VideoView(context, attrs),
    DefaultLifecycleObserver, BaseVideoView.OnStateChangeListener {
    var playerId: String = ""

    //是否使用视频缓存框架
    var openCache = false

    //自动播放
    var autoPlay = false

    var video: VideoInfo? = null
    private val fadeIn = ValueAnimator.ofFloat(0f, 1.0f)

    //是否对用户可见
    var isVisibleToUser = true

    companion object {
        private val TAG = VideoPlayer::class.java.simpleName

    }

    init {
        mPlayerContainer.setBackgroundColor(
            ContextCompat.getColor(
                context, R.color.v_player_play_bg_color
            )
        )
        addOnStateChangeListener(this)
        if (context is LifecycleOwner) {
            context.lifecycle.addObserver(this)
        }
        setRenderViewAnim()

    }

    private fun setRenderViewAnim() {
        fadeIn.duration = 700
        fadeIn.repeatCount = 0
        fadeIn.interpolator = LinearInterpolator()
        fadeIn.addUpdateListener { animation ->
            mRenderView?.view?.alpha = animation.animatedValue as Float
        }
        fadeIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                mRenderView?.view?.alpha = 0f
            }

            override fun onAnimationEnd(animation: Animator) {
                mRenderView?.view?.alpha = 1f
            }
        })
    }

    /**
     * 非LifecycleOwner的context如果需要手动绑定
     */
    fun bindLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(this)
    }

    fun unBindLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
    }

    override fun onPause(owner: LifecycleOwner) {
        pause()
    }

    override fun onResume(owner: LifecycleOwner) {
        if (isVisibleToUser) {
            resume()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        if (openCache) {
            ProxyVideoCacheManager.getProxy(context).shutdown()
        }

        release()
    }


    override fun setUrl(url: String?) {
        if (openCache && url?.endsWith("mp4") == true) {
            val cacheServer: HttpProxyCacheServer =
                ProxyVideoCacheManager.getProxy(context)
            if (BuildConfig.DEBUG) {
                cacheServer.registerCacheListener({ _, _url, percentsAvailable ->
                    R2Log.d(TAG, "percentsAvailable $_url $percentsAvailable")
                }, url)
            }
            val proxyUrl = cacheServer.getProxyUrl(url)
            super.setUrl(proxyUrl)
        } else {
            super.setUrl(url)
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

    }


    /**
     * 真正承载播放器视图的容器
     */
    fun getPlayerContainer(): FrameLayout = mPlayerContainer

    fun getVideoController(): VideoControllerExt? = mVideoController as VideoControllerExt?


    fun setVideoInfo(
        video: VideoInfo, reset: Boolean = true, autoPlay: Boolean = this.autoPlay
    ) {
        if (reset) {
            release()
        }
        this.video = video
        if (video.lines.isNotEmpty()) {
            val line0 = video.lines[0]
            setUrl(line0.url)
            //自动播放 或者播放下一个
            if (autoPlay) start()
        }
    }

    override fun onPlayerStateChanged(playerState: Int) {}

    override fun onPlayStateChanged(playState: Int) {
        if (BuildConfig.DEBUG) {
            val state = when (playState) {
                STATE_ERROR -> "STATE_ERROR"
                STATE_PREPARING -> "STATE_PREPARING"
                STATE_PREPARED -> "STATE_PREPARED"
                STATE_PLAYING -> "STATE_PLAYING"
                STATE_PAUSED -> "STATE_PAUSED"
                STATE_PLAYBACK_COMPLETED -> "STATE_PLAYBACK_COMPLETED"
                STATE_BUFFERING -> "STATE_BUFFERING"
                STATE_BUFFERED -> "STATE_BUFFERED"
                STATE_START_ABORT -> "STATE_START_ABORT"
                else -> "STATE_IDLE"
            }
            R2Log.d(TAG, "playState:$state")
        }

        when (playState) {
            STATE_PREPARING -> {
                if (VideoPlayerConfig.useTransitionAnim) {
                    mRenderView?.view?.alpha = 0f
                }
            }

            STATE_PREPARED -> {
                if (VideoPlayerConfig.useTransitionAnim) {
                    postDelayed({ fadeIn.start() }, 400)
                }
            }
        }

    }

    /**
     * 根据是否对用户可见切换播放、启播或暂停
     */
    fun switchToggle() {
        if (isVisibleToUser) {
            start()
        } else {
            pause()
        }
    }

}
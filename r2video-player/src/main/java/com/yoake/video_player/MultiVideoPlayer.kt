package com.yoake.video_player

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.yoake.r2base.utils.R2Utils.removeViewFormParent

import com.yoake.video_player.component.IMinSize

import com.yoake.video_player.controller.MultiVideoController
import com.yoake.video_player.controller.VideoControllerExt
import com.yoake.video_player.util.PIPManager
import com.yoake.video_player.util.VideoInfo
import jp.wasabeef.glide.transformations.BlurTransformation

import xyz.doikki.videoplayer.controller.MediaPlayerControl
import xyz.doikki.videoplayer.player.VideoViewManager
import xyz.doikki.videoplayer.util.PlayerUtils


class MultiVideoPlayer(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs),
    MediaPlayerControl, DefaultLifecycleObserver, OnClickListener {

    private var lifecycleOwner: LifecycleOwner? = null
    var mIsFullScreen = false

    //主屏和列表之间的距离
    val hSpace = dip(10)

    //列表播放器垂直的距离
    val vSpace = dip(2)

    //退出多屏后的控制器
    var mediaController: VideoControllerExt? = null


    //悬浮窗管理器
    val mPIPManager: PIPManager = PIPManager.getInstance(context)


    //列表播放器集合
    val listPlayer: MutableList<VideoPlayer> by lazy(LazyThreadSafetyMode.NONE) {
        ArrayList()
    }

    var multiVideoPlayerListener: MultiVideoPlayerListener? = null
    val root: View =
        LayoutInflater.from(context).inflate(R.layout.v_player_layout_multi, null, false)
    val playerContainer: FrameLayout
    val listContainer: LinearLayout
    val backgroundThumb: ImageView
    val scrollView: ScrollView
    val closeView: ImageView
    val fullscreenView: ImageView
    val backView: View

    init {
        playerContainer = root.findViewById(R.id.player_container)
        listContainer = root.findViewById(R.id.list_container)
        backgroundThumb = root.findViewById(R.id.background_thumb)
        scrollView = root.findViewById(R.id.scroll_view)
        closeView = root.findViewById(R.id.close_view)
        fullscreenView = root.findViewById(R.id.fullscreen_view)
        backView = root.findViewById(R.id.back_view)
        //不知道什么原因在预览中不显示高度。 添加这个是为了可以预览
        if (!isInEditMode) {
            addView(root, -1, -1)
            (scrollView.layoutParams as MarginLayoutParams).marginStart = hSpace
            closeView.setOnClickListener(this)
            fullscreenView.setOnClickListener(this)
            backView.setOnClickListener(this)
        }
    }

    fun initCore() {
        if (context is LifecycleOwner) {
            this.lifecycleOwner?.lifecycle?.addObserver(this)
            mPIPManager.bindLifecycleOwner(context as LifecycleOwner)
        }
        (VideoViewManager.instance().get(PIPManager.PIP) as VideoPlayer).apply {
            removeViewFormParent(this)
            layoutParams = LayoutParams(-1, -1)
            playerContainer.addView(this, 0)
        }
    }

    private fun dip(value: Int): Int {
        return value * resources.displayMetrics.density.toInt()
    }

    /**
     * 获取主屏播放器
     */
    fun getMainPlayer(): VideoPlayer {
        return playerContainer.getChildAt(0) as VideoPlayer
    }

    fun setVideoController(mediaController: VideoControllerExt?) {
        this.mediaController = mediaController
        getMainPlayer().setVideoController(mediaController)
    }

    fun switchNormalWindow() {
        this.mediaController?.let {
            it.setPlayerState(getMainPlayer().currentPlayerState)
            it.setPlayState(getMainPlayer().currentPlayState)
        } ?: kotlin.run {
            throw Exception("请在setVideoController后调用")
        }
        mPIPManager.stopFloatWindow2()
    }

    fun addPlayer(playerId: String, videoInfo: VideoInfo) {
        //小屏播放器
        val player: VideoPlayer = VideoPlayer(context.applicationContext).apply {
            this.playerId = playerId
            this.setVideoController(MultiVideoController(context).apply {
                this.setMinSize()
                this.setVideoInfo(videoInfo)
            })
            this.setEnableAudioFocus(false)
            this.isMute = true
            this.setVideoInfo(videoInfo)
        }
        //小屏播放器的容器
        val minContainer = FrameLayout(context).apply {
            this.layoutParams = LinearLayout.LayoutParams(0, 0).apply {
                this.gravity = Gravity.CENTER
            }
            this.addView(player, -1, -1)
            val closeView = ImageView(context).apply {
                this.setImageResource(R.drawable.v_player_ic_action_multi_mode_close)
                this.visibility = View.GONE
                val padding = resources.getDimensionPixelSize(R.dimen.r2dp4)
                setPadding(padding,padding,padding,padding)
                val size =
                    resources.getDimensionPixelSize(R.dimen.r2dp16) + 2 * padding
                this.layoutParams = LayoutParams(size, size).apply {
                    this.gravity = Gravity.END or Gravity.TOP
                }
            }
            closeView.setOnClickListener {
                removePlayerAnim(this.getChildAt(0) as VideoPlayer, this)
            }
            this.addView(closeView, 1)

        }
        //设置容器点击事情 - 切换主屏和副屏内容
        minContainer.setOnClickListener {
            val tempMinPlayer = minContainer.getChildAt(0) as VideoPlayer
            val tempMainPlayer = getMainPlayer()

            tempMainPlayer.apply {
                listPlayer.add(this)
                removeViewFormParent(this)
                this.setEnableAudioFocus(false)
                this.isMute = true
                minContainer.addView(this, 0)
                (this.getVideoController() as IMinSize).setMinSize()

            }
            tempMinPlayer.apply {
                listPlayer.remove(this)
                removeViewFormParent(this)
                this.setEnableAudioFocus(true)
                this.isMute = false
                playerContainer.addView(this, 0)
                (this.getVideoController() as IMinSize).resetSize()
            }
            multiVideoPlayerListener?.onPlayerSwitched(getMainPlayer().playerId)
        }
        listPlayer.add(player)
        // size==0 说明是添加的第一个 新建主屏控制器 并将常规播放器的控制器状态同步过去
        if (listContainer.childCount == 0) {
            backgroundThumb.visibility = View.VISIBLE
            val controller = MultiVideoController(context)
            val mediaController = getMainPlayer().getVideoController()
            getMainPlayer().setVideoController(controller)
            controller.setPlayerState(getMainPlayer().currentPlayerState)
            controller.setPlayState(getMainPlayer().currentPlayState)
            controller.setVideoInfo(mediaController?.getVideoInfo())
            enterMultiModeAnim(player, minContainer)
        } else {
            addPlayerAnim(player, minContainer)
        }
    }


    fun setBackgroundThumb(url: String?) {
        Glide.with(context).load(url).transform(BlurTransformation()).into(backgroundThumb)
    }


    override fun onPause(owner: LifecycleOwner) {
        listPlayer.forEach {
            it.pause()
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        listPlayer.forEach {
            it.resume()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        listPlayer.forEach {
            it.release()
        }
        owner.lifecycle.removeObserver(this)

    }

    fun setVideoInfo(playerId: String, videoInfo: VideoInfo) {
        getMainPlayer().playerId = playerId
        getMainPlayer().setVideoInfo(videoInfo)
    }

    override fun start() {
        getMainPlayer().start()
    }

    override fun pause() {
        getMainPlayer().pause()
    }

    override fun getDuration(): Long {
        return getMainPlayer().duration
    }

    override fun getCurrentPosition(): Long {
        return getMainPlayer().currentPosition
    }

    override fun seekTo(pos: Long) {
        getMainPlayer().seekTo(pos)
    }

    override fun isPlaying(): Boolean {
        return getMainPlayer().isPlaying
    }

    override fun getBufferedPercentage(): Int {
        return getMainPlayer().bufferedPercentage
    }

    override fun startFullScreen() {
        if (listPlayer.isEmpty()) {
            getMainPlayer().startFullScreen()
        } else {
            startMultiFullScreen()
            post {
                requestLayout2()
            }
        }
    }

    override fun stopFullScreen() {
        if (listPlayer.isEmpty()) {
            getMainPlayer().stopFullScreen()
        } else {
            stopMultiFullScreen()
            post {
                requestLayout2()
            }
        }
    }

    override fun isFullScreen(): Boolean {
        return if (listPlayer.isEmpty()) {
            getMainPlayer().isFullScreen
        } else {
            mIsFullScreen
        }
    }

    override fun setMute(isMute: Boolean) {
        getMainPlayer().isMute = isMute
    }

    override fun isMute(): Boolean {
        return getMainPlayer().isMute
    }

    override fun setScreenScaleType(screenScaleType: Int) {
        getMainPlayer().setScreenScaleType(screenScaleType)
    }

    override fun setSpeed(speed: Float) {
        getMainPlayer().speed = speed
    }

    override fun getSpeed(): Float {
        return getMainPlayer().speed
    }

    override fun getTcpSpeed(): Long {
        return getMainPlayer().tcpSpeed
    }

    override fun replay(resetPosition: Boolean) {
        getMainPlayer().replay(resetPosition)
    }

    override fun setMirrorRotation(enable: Boolean) {
        getMainPlayer().setMirrorRotation(enable)
    }

    override fun doScreenShot(): Bitmap {
        return getMainPlayer().doScreenShot()
    }

    override fun getVideoSize(): IntArray {
        return getMainPlayer().videoSize
    }

    override fun startTinyScreen() {
        getMainPlayer().startTinyScreen()
    }

    override fun stopTinyScreen() {
        getMainPlayer().stopTinyScreen()
    }

    override fun isTinyScreen(): Boolean {
        return getMainPlayer().isTinyScreen
    }


    override fun onClick(v: View?) {

        when (v) {
            backView -> {
                val activity = PlayerUtils.scanForActivity(context)
                if (activity != null && mIsFullScreen) {
                    toggleFullScreen(activity)
                    fullscreenView.isSelected = false
                } else {
                    activity?.finish()
                }
            }

            fullscreenView -> {
                toggleFullScreen(PlayerUtils.scanForActivity(context))
                fullscreenView.isSelected = !fullscreenView.isSelected
            }

            closeView -> {
                val removePlayer = getMainPlayer()
                removePlayer.release()
                removeViewFormParent(removePlayer)
                listPlayer[0].apply {
                    listPlayer.remove(this)
                    multiVideoPlayerListener?.onPlayerRemoved(removePlayer.playerId)
                    removeViewFormParent(this)
                    this.setEnableAudioFocus(true)
                    this.isMute = false
                    playerContainer.addView(this, 0)
                    (this.getVideoController() as IMinSize).resetSize()
                }
                listContainer.removeViewAt(0)
                if (listContainer.childCount == 0) {
                    exitMultiModeAnim()
                }
                multiVideoPlayerListener?.onPlayerSwitched(getMainPlayer().playerId)
            }
        }
    }

    /**
     * 退出多屏幕
     */
    fun exitMultiMode(call: ((Unit) -> Unit)? = null) {
        if (listPlayer.size > 0) {
            listPlayer.forEach {
                it.release()
            }
            listContainer.removeAllViews()
            exitMultiModeAnim(call)
        } else {
            call?.invoke(Unit)
        }
    }

    interface MultiVideoPlayerListener {
        fun onPlayerRemoved(playerId: String)
        fun onPlayerSwitched(mainPlayerId: String)
    }
}
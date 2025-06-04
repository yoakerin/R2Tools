package com.yoake.video_player

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.view.get
import xyz.doikki.videoplayer.util.PlayerUtils

/**
 * 切换屏幕模式
 */
fun MultiVideoPlayer.toggleFullScreen(activity: Activity?) {
    if (activity == null || activity.isFinishing) return
    if (mIsFullScreen) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        stopFullScreen()
        backView.visibility = View.GONE
    } else {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        startFullScreen()
        backView.visibility = View.VISIBLE
    }
}

/**
 * 开启多屏幕模式下的全屏
 */
fun MultiVideoPlayer.startMultiFullScreen() {
    if (mIsFullScreen) return
    mIsFullScreen = true
    val decorView: ViewGroup = getDecorView() ?: return
    //隐藏NavigationBar和StatusBar
    val uiOptions = decorView.systemUiVisibility or FrameLayout.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    decorView.systemUiVisibility = uiOptions
    PlayerUtils.scanForActivity(context)?.window?.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
    )
    //从当前FrameLayout中移除播放器视图
    removeView(root)
    //将播放器视图添加到DecorView中即实现了全屏
    decorView.addView(root)
}

/**
 * 关闭多屏模式下的全屏
 */
fun MultiVideoPlayer.stopMultiFullScreen() {
    if (!mIsFullScreen) return
    mIsFullScreen = false
    val decorView = getDecorView() ?: return
    //显示NavigationBar和StatusBar
    val uiOptions =
        decorView.systemUiVisibility and FrameLayout.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
    decorView.systemUiVisibility = uiOptions
    PlayerUtils.scanForActivity(context)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    //把播放器视图从DecorView中移除并添加到当前FrameLayout中即退出了全屏
    decorView.removeView(root)
    this.addView(root)

}

/**
 * 重新布局一下
 */
fun MultiVideoPlayer.requestLayout2() {
    playerContainer.apply {
        val layoutParams = playerContainer.layoutParams
        layoutParams.width = mainPlayerWidth()
        layoutParams.height = (layoutParams.width * 9f / 16f).toInt()
        requestLayout()
    }
    val childCount = listContainer.childCount
    val targetWidth = measuredWidth - mainPlayerWidth() - hSpace
    for (i in 0 until childCount) {
        val container = listContainer[i]
        container.layoutParams.apply {
            width = targetWidth
            height = (targetWidth * 9f / 16).toInt()
        }
        container.requestLayout()

    }
}

/**
 * 获取所在Activity的decorView
 */
fun MultiVideoPlayer.getDecorView(): ViewGroup? {
    val activity: Activity = PlayerUtils.scanForActivity(context) ?: return null
    return activity.window.decorView as ViewGroup
}


/**
 * 计算主屏播放器宽
 */
fun MultiVideoPlayer.mainPlayerWidth(): Int {
    return ((measuredWidth * 18f - 18f * hSpace + 16f * vSpace) / 27f).toInt()
}

private fun ValueAnimator.attribute(): ValueAnimator {
    duration = 200L
    interpolator = LinearInterpolator()
    return this
}


/**
 * 添加列表播放器动画
 */
fun MultiVideoPlayer.addPlayerAnim(player: VideoPlayer, container: FrameLayout) {
    val targetWidth = measuredWidth - mainPlayerWidth() - hSpace
    val alphaAnimator = ObjectAnimator.ofFloat(container, "alpha", 0f, 1.0f).attribute()
    val valueAnimator = ValueAnimator.ofInt(0, targetWidth).attribute()
    valueAnimator.addUpdateListener { animation ->
        // 在动画进行中更新视图的宽度
        val animatedValue = animation.animatedValue as Int
        container.layoutParams.apply {
            width = animatedValue
            height = (animatedValue * 9f / 16).toInt()
        }
        container.requestLayout()
    }
    valueAnimator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            container.alpha = 0f
            listContainer.addView(container)
        }

        override fun onAnimationCancel(animation: Animator) {}

        override fun onAnimationRepeat(animation: Animator) {}
        override fun onAnimationEnd(animation: Animator) {
            player.start()
            container[1].visibility = View.VISIBLE
        }
    })

    AnimatorSet().apply {
        playTogether(alphaAnimator, valueAnimator)
        start()
    }
}


/**
 * 移除列表播放器动画
 */
fun MultiVideoPlayer.removePlayerAnim(
    player: VideoPlayer, container: FrameLayout
) {
    val alphaAnimator = ObjectAnimator.ofFloat(container, "alpha", 1f, 0f).attribute()
    val valueAnimator = ValueAnimator.ofInt(container.measuredWidth, 0).attribute()
    valueAnimator.addUpdateListener { animation ->
        // 在动画进行中更新视图的宽度
        val animatedValue = animation.animatedValue as Int
        container.layoutParams.apply {
            width = animatedValue
            height = (animatedValue * 9f / 16).toInt()
        }
        container.requestLayout()

    }
    valueAnimator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            container[1].visibility = View.GONE
        }

        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
        override fun onAnimationEnd(animation: Animator) {
            player.release()
            listPlayer.remove(player)
            multiVideoPlayerListener?.onPlayerRemoved(player.playerId)
            listContainer.removeView(container)
            if (listContainer.childCount == 0) {
                exitMultiModeAnim()
            }
        }
    })
    AnimatorSet().apply {
        playTogether(alphaAnimator, valueAnimator)
        start()
    }
}

/**
 * 进入多屏模式的动画
 */
fun MultiVideoPlayer.enterMultiModeAnim(player: VideoPlayer, minContainer: FrameLayout) {

    val valueAnimator = ValueAnimator.ofInt(measuredWidth, mainPlayerWidth()).attribute()
    valueAnimator.addUpdateListener { animation ->
        // 在动画进行中更新视图的宽度
        val animatedValue = animation.animatedValue as Int
        playerContainer.layoutParams.apply {
            width = animatedValue
            height = (animatedValue * 9f / 16).toInt()
        }
        playerContainer.requestLayout()
    }
    valueAnimator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            scrollView.visibility = View.VISIBLE
        }

        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
        override fun onAnimationEnd(animation: Animator) {
            closeView.visibility = View.VISIBLE
            fullscreenView.visibility = View.VISIBLE
            addPlayerAnim(player, minContainer)
        }
    })

    valueAnimator.start()
}

/**
 * 退出多屏模式的动画
 */
fun MultiVideoPlayer.exitMultiModeAnim(animEnd: ((Unit) -> Unit)? = null) {
    fullscreenView.visibility = View.GONE
    backView.visibility = View.GONE
    val currentWidth = playerContainer.measuredWidth
    val currentHeight = playerContainer.measuredHeight
    val targetWidth = root.measuredWidth
    val targetHeight = root.measuredHeight
    val wValue = (targetWidth - currentWidth) * 1f
    val hValue = (targetHeight - currentHeight) * 1f
    val valueAnimator = ValueAnimator.ofInt(currentWidth, targetWidth).attribute()

    val layoutParams = playerContainer.layoutParams
    valueAnimator.addUpdateListener { animation ->
        // 在动画进行中更新视图的宽度
        val animatedValue = animation.animatedValue as Int
        //计算宽增加值占用总值的比例
        val ratio = (animatedValue - currentWidth) / wValue
        layoutParams.width = animatedValue
        layoutParams.height = (currentHeight + hValue * ratio).toInt()
        playerContainer.requestLayout()
    }
    valueAnimator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            listPlayer.clear()
            listContainer.removeAllViews()
            scrollView.visibility = View.GONE
        }

        override fun onAnimationCancel(animation: Animator) {}

        override fun onAnimationRepeat(animation: Animator) {}
        override fun onAnimationEnd(animation: Animator) {
            layoutParams.width = -1
            layoutParams.height = -1
            playerContainer.requestLayout()
            closeView.visibility = View.GONE
            backgroundThumb.visibility = View.GONE
            mPIPManager.updateVideoPlayer(getMainPlayer())
            val controller = getMainPlayer().getVideoController()
            getMainPlayer().setVideoController(mediaController)
            mediaController?.setPlayerState(getMainPlayer().currentPlayerState)
            mediaController?.setPlayState(getMainPlayer().currentPlayState)
            mediaController?.setVideoInfo(controller?.getVideoInfo())
            mediaController?.show()
            if (mIsFullScreen) {
                stopMultiFullScreen()
                startFullScreen()
            }
            animEnd?.invoke(Unit)
        }
    })
    valueAnimator.start()
}



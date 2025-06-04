package com.yoake.video_player.controller;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yoake.r2base.kit.R2ToastKitKt;
import com.yoake.video_player.R;
import com.yoake.video_player.component.CompleteView;
import com.yoake.video_player.component.ErrorView;
import com.yoake.video_player.component.GestureView;
import com.yoake.video_player.component.LiveControlView;
import com.yoake.video_player.component.PrepareView;
import com.yoake.video_player.component.TitleView;
import com.yoake.video_player.component.VodControlView;
import com.yoake.video_player.util.VideoInfo;
import com.yoake.video_player.util.VideoPlayerConfig;

import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 直播/点播控制器
 * 注意：此控制器仅做一个参考，如果想定制ui，你可以直接继承GestureVideoController或者BaseVideoController实现
 * 你自己的控制器
 */

public class StandardVideoController extends VideoControllerExt implements View.OnClickListener {
    protected ImageView mLockButton;
    protected ProgressBar mLoadingProgress;
    private boolean isBuffering;
    private int playState = VideoView.STATE_IDLE;
    private PrepareView prepareView;
    private ErrorView errorView;
    private CompleteView completeView;
    private TitleView titleView;
    private VideoInfo videoInfo;

    public StandardVideoController(@NonNull Context context) {
        this(context, null);
    }

    public StandardVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.v_player_layout_standard_controller;
    }

    @Override
    protected void initView() {
        super.initView();
        mLockButton = findViewById(R.id.lock);
        mLockButton.setOnClickListener(this);
        mLoadingProgress = findViewById(R.id.loading);
    }


    public void addDefaultControlComponent(boolean isLive) {
        completeView = new CompleteView(getContext());
        errorView = new ErrorView(getContext());
        prepareView = new PrepareView(getContext());
        titleView = new TitleView(getContext());
        addControlComponent(completeView, errorView, prepareView, titleView);
        if (isLive) {
            addControlComponent(new LiveControlView(getContext()));
        } else {
            addControlComponent(new VodControlView(getContext()));
        }
        addControlComponent(new GestureView(getContext()));
        setCanChangePosition(!isLive);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.lock) {
            mControlWrapper.toggleLockState();
        }
    }

    @Override
    protected void onLockStateChanged(boolean isLocked) {
        if (isLocked) {
            mLockButton.setSelected(true);
            R2ToastKitKt.toast(this, R.string.v_player_locked);
        } else {
            mLockButton.setSelected(false);
            R2ToastKitKt.toast(this, R.string.v_player_unlocked);
        }
    }

    @Override
    protected void onVisibilityChanged(boolean isVisible, Animation anim) {
        if (mControlWrapper.isFullScreen()) {
            if (isVisible) {
                if (mLockButton.getVisibility() == GONE) {
                    mLockButton.setVisibility(VISIBLE);
                    if (anim != null) {
                        mLockButton.startAnimation(anim);
                    }
                }
            } else {
                mLockButton.setVisibility(GONE);
                if (anim != null) {
                    mLockButton.startAnimation(anim);
                }
            }
        }
    }

    @Override
    protected void onPlayerStateChanged(int playerState) {
        super.onPlayerStateChanged(playerState);
        switch (playerState) {
            case VideoView.PLAYER_NORMAL:
                setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mLockButton.setVisibility(GONE);
                break;
            case VideoView.PLAYER_FULL_SCREEN:
                if (isShowing()) {
                    mLockButton.setVisibility(VISIBLE);
                } else {
                    mLockButton.setVisibility(GONE);
                }
                break;
        }

        if (mActivity != null && hasCutout()) {
            int orientation = mActivity.getRequestedOrientation();
            int dp24 = PlayerUtils.dp2px(getContext(), 24);
            int cutoutHeight = getCutoutHeight();
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                FrameLayout.LayoutParams lblp = (FrameLayout.LayoutParams) mLockButton.getLayoutParams();
                lblp.setMargins(dp24, 0, dp24, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mLockButton.getLayoutParams();
                layoutParams.setMargins(dp24 + cutoutHeight, 0, dp24 + cutoutHeight, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mLockButton.getLayoutParams();
                layoutParams.setMargins(dp24, 0, dp24, 0);
            }
        }

    }

    @Override
    protected void onPlayStateChanged(int playState) {
        super.onPlayStateChanged(playState);
        this.playState = playState;
        switch (playState) {
            //调用release方法会回到此状态
            case VideoView.STATE_IDLE:
                mLockButton.setSelected(false);
                mLoadingProgress.setVisibility(GONE);
                break;
            case VideoView.STATE_PLAYING:
            case VideoView.STATE_PAUSED:
            case VideoView.STATE_PREPARED:
            case VideoView.STATE_ERROR:
            case VideoView.STATE_BUFFERED:
                if (playState == VideoView.STATE_BUFFERED) {
                    isBuffering = false;
                }
                if (!isBuffering) {
                    mLoadingProgress.setVisibility(GONE);
                }
                break;
            case VideoView.STATE_PREPARING:
            case VideoView.STATE_BUFFERING:
                mLoadingProgress.setVisibility(VISIBLE);
                if (playState == VideoView.STATE_BUFFERING) {
                    isBuffering = true;
                }
                break;
            case VideoView.STATE_PLAYBACK_COMPLETED:
                mLoadingProgress.setVisibility(GONE);
                mLockButton.setVisibility(GONE);
                mLockButton.setSelected(false);
                break;
        }
        if (playState == VideoView.STATE_PAUSED && VideoPlayerConfig.INSTANCE.getShowComponentWhenPause()) {
            //显示控件
            show();
            //取消计时
            stopFadeOut();
        }
    }


    @Override
    public void startFadeOut() {
        if (playState == VideoView.STATE_PAUSED && VideoPlayerConfig.INSTANCE.getShowComponentWhenPause()) {
            //播放器处于暂停 并且开启了暂停的时候始终显示组件的功能就不做延迟隐藏的处理
        } else {
            super.startFadeOut();
        }
    }

    @Override
    protected void togglePlay() {
        super.togglePlay();
        startFadeOut();
    }

    @Override
    public boolean onBackPressed() {
        if (isLocked()) {
            show();
            R2ToastKitKt.toast(this, R.string.v_player_lock_tip);
            return true;
        }
        if (mControlWrapper.isFullScreen()) {
            return stopFullScreen();
        }
        return super.onBackPressed();
    }

    @Override
    public void setFloatWindowEnabled(boolean enabled) {
        if (titleView != null) {
            titleView.setFloatWindowEnabled(enabled);
        }
    }


    public PrepareView getPrepareView() {
        return prepareView;
    }

    public ErrorView getErrorView() {
        return errorView;
    }

    public CompleteView getCompleteView() {
        return completeView;
    }

    public TitleView getTitleView() {
        return titleView;
    }


    @Nullable
    @Override
    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    @Override
    public void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
        if (videoInfo != null) {
            if (titleView != null) {
                titleView.setTitle(videoInfo.getTitle());
            }
            if (prepareView != null) {
                prepareView.setThumb(videoInfo.getPoster());
            }
            if (completeView != null) {
                completeView.setThumb(videoInfo.getPoster());
            }
            if (titleView != null) {
                titleView.setThumbSrc(videoInfo.getPoster());
            }
        }
    }

    @Override
    public void setPoster(@Nullable String thumb) {
        if (titleView != null) {
            titleView.setTitle(thumb);
        }
        if (prepareView != null) {
            prepareView.setThumb(thumb);
        }
        if (completeView != null) {
            completeView.setThumb(thumb);
        }
        if (titleView != null) {
            titleView.setThumbSrc(thumb);
        }
    }
}

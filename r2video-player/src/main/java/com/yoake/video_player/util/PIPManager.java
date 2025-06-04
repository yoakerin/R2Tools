package com.yoake.video_player.util;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.yoake.r2base.utils.R2Utils;
import com.yoake.video_player.VideoPlayer;
import com.yoake.video_player.controller.PIPVideoController;
import com.yoake.video_player.widget.FloatView;

import xyz.doikki.videoplayer.player.VideoViewManager;

/**
 * 悬浮播放
 */

public class PIPManager implements DefaultLifecycleObserver {
    private static volatile PIPManager instance;
    private VideoPlayer mVideoView;
    private FloatView mFloatView;
    private final PIPVideoController mFloatController;
    private boolean mIsShowing;
    private Class<?> mActClass;
    private Bundle mExtras;
    private String playingId;

    public static String PIP = "PIP";
    public Context context;

    public static int REQUEST_OVERLAY_CODE = 10123;

    private PIPManager(Context context) {
        this.context = context;
        mVideoView = new VideoPlayer(context, null);
        VideoViewManager.instance().add(mVideoView, PIP);
        mFloatController = new PIPVideoController(context);
        mFloatView = new FloatView(context, 600, 1600);
    }


    public static PIPManager getInstance(Context context) {
        if (instance == null) {
            synchronized (PIPManager.class) {
                if (instance == null) {
                    instance = new PIPManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /**
     * 更新主屏播放器。
     * 用于多屏播放器切换了主屏播放器后的更新
     *
     * @param videoPlayer
     */
    public void updateVideoPlayer(VideoPlayer videoPlayer) {
        VideoViewManager.instance().remove(PIP);
        mVideoView = videoPlayer;
        VideoViewManager.instance().add(videoPlayer, PIP);
    }

    public void startFloatWindow() {
        if (mIsShowing) return;
        R2Utils.removeViewFormParent(mVideoView);
        mVideoView.setVideoController(mFloatController);
        mFloatController.setPlayState(mVideoView.getCurrentPlayState());
        mFloatController.setPlayerState(mVideoView.getCurrentPlayerState());
        if (mFloatView == null) {
            mFloatView = new FloatView(context, 600, 1600);
        }
        mFloatView.addView(mVideoView);
        mFloatView.addToWindow();
        mIsShowing = true;
    }


    public void bindLifecycleOwner(LifecycleOwner lifecycleOwner) {
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    public void stopFloatWindow() {
        if (!mIsShowing) return;
        mFloatView.removeFromWindow();
        R2Utils.removeViewFormParent(mVideoView);
        mIsShowing = false;
        mFloatView = null;
    }

    /**
     * 仅用于多屏播放器的调用
     */
    public void stopFloatWindow2() {
        if (!mIsShowing) return;
        mFloatView.removeFromWindow();
        mIsShowing = false;
        mFloatView = null;
    }

    @SuppressLint("ObsoleteSdkInt")
    public void startFloatWindow(ComponentActivity activity, Object thumb) {
        mFloatController.setThumbSrc(thumb);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, REQUEST_OVERLAY_CODE);
        } else {
            startFloatWindow();
            activity.finish();
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    public void onOverlayPermission(ComponentActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(activity)) {
            startFloatWindow();
            activity.finish();
            mVideoView.resume();
        }
    }


    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        pause();
    }

    public void pause() {
        if (mIsShowing) return;
        mVideoView.pause();
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        resume();
    }

    public void resume() {
        if (mIsShowing) return;
        mVideoView.resume();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        owner.getLifecycle().removeObserver(this);
        reset();
    }

    public void reset() {
        if (mIsShowing) return;
        R2Utils.removeViewFormParent(mVideoView);
        mVideoView.release();
        mVideoView.setVideoController(null);
        mActClass = null;
    }

    public boolean onBackPress() {
        return !mIsShowing && mVideoView.onBackPressed();
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    /**
     * 显示悬浮窗
     */
    public void setFloatViewVisible() {
        if (mIsShowing) {
            mVideoView.resume();
            mFloatView.setVisibility(View.VISIBLE);
        }
    }

    public void setActClass(Class<?> cls) {
        this.mActClass = cls;
    }

    public Class<?> getActClass() {
        return mActClass;
    }

    public Bundle getExtras() {
        return mExtras;
    }

    public void setExtras(Bundle mExtras) {
        this.mExtras = mExtras;
    }

    public String getPlayingId() {
        return playingId;
    }

    public void setPlayingId(String playingId) {
        this.playingId = playingId;
    }
}

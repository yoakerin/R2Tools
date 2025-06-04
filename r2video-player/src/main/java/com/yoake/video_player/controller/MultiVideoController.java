package com.yoake.video_player.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yoake.video_player.R;
import com.yoake.video_player.component.CompleteView;
import com.yoake.video_player.component.ErrorView;
import com.yoake.video_player.component.IMinSize;
import com.yoake.video_player.component.MultiControlView;
import com.yoake.video_player.component.PrepareView;
import com.yoake.video_player.util.VideoInfo;

import xyz.doikki.videoplayer.player.VideoView;

/**
 * 多屏幕模式控制器
 */
public class MultiVideoController extends VideoControllerExt implements IMinSize {
    private boolean isBuffering;
    protected ProgressBar mLoadingProgress;
    private PrepareView prepareView;
    private ErrorView errorView;
    private CompleteView completeView;
    private MultiControlView controlView;
    private VideoInfo videoInfo;

    public MultiVideoController(@NonNull Context context) {
        this(context, null);
    }

    public MultiVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.v_player_layout_multi_controller;
    }

    @Override
    protected void initView() {
        super.initView();
        mLoadingProgress = findViewById(R.id.loading);
        completeView = new CompleteView(getContext());
        errorView = new ErrorView(getContext());
        prepareView = new PrepareView(getContext());
        controlView = new MultiControlView(getContext());
        addControlComponent(completeView, errorView, prepareView, controlView);
    }

    @Override
    protected void onPlayStateChanged(int playState) {
        super.onPlayStateChanged(playState);
        switch (playState) {
            //调用release方法会回到此状态
            case VideoView.STATE_IDLE:
            case VideoView.STATE_PLAYBACK_COMPLETED:
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
        }
    }


    @Override
    public void setFloatWindowEnabled(boolean enable) {
    }


    @Override
    public void setMinSize() {
        setOnTouchListener(null);
        int size = getContext().getResources().getDimensionPixelSize(R.dimen.r2dp36);
        ViewGroup.LayoutParams layoutParams = mLoadingProgress.getLayoutParams();
        layoutParams.width = size;
        layoutParams.height = size;
        errorView.setMinSize();
        prepareView.setMinSize();
        completeView.setMinSize();
        controlView.setMinSize();
    }

    @Override
    public void resetSize() {
        setOnTouchListener(this);
        int size = getContext().getResources().getDimensionPixelSize(R.dimen.v_player_play_btn_size);
        ViewGroup.LayoutParams layoutParams = mLoadingProgress.getLayoutParams();
        layoutParams.width = size;
        layoutParams.height = size;
        errorView.resetSize();
        prepareView.resetSize();
        completeView.resetSize();
        controlView.resetSize();
    }

    @Override
    public void setVideoInfo(@Nullable VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
        if (videoInfo != null) {
            controlView.setTitle(videoInfo.getTitle());
            prepareView.setThumb(videoInfo.getPoster());
            completeView.setThumb(videoInfo.getPoster());
        }
    }

    @Nullable
    @Override
    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    @Override
    public void setPoster(@Nullable String thumb) {
        prepareView.setThumb(thumb);
        completeView.setThumb(thumb);
    }
}

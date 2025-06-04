package com.yoake.video_player.controller;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yoake.video_player.component.CompleteView;
import com.yoake.video_player.component.ErrorView;
import com.yoake.video_player.component.PIPControlView;
import com.yoake.video_player.component.PrepareView;
import com.yoake.video_player.util.VideoInfo;


/**
 * 悬浮播放控制器
 * Created by Doikki on 2017/6/1.
 */
public class PIPVideoController extends VideoControllerExt {


    private PrepareView prepareView;
    private ErrorView errorView;
    private CompleteView completeView;

    public PIPVideoController(@NonNull Context context) {
        this(context, null);
    }

    public PIPVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void initView() {
        super.initView();
        completeView = new CompleteView(getContext());
        errorView = new ErrorView(getContext());
        prepareView = new PrepareView(getContext());
        addControlComponent(completeView, errorView, prepareView, new PIPControlView(getContext()));
    }


    @Deprecated()
    @Override
    public void setVideoInfo(@Nullable VideoInfo videoInfo) {
        if (videoInfo != null) {
            prepareView.setThumb(videoInfo.getPoster());
            completeView.setThumb(videoInfo.getPoster());
        }
    }

    public void setThumbSrc(Object thumb) {
        prepareView.setThumb(thumb);
        completeView.setThumb(thumb);
    }

    @Nullable
    @Override
    public VideoInfo getVideoInfo() {
        return null;
    }

    @Override
    public void setFloatWindowEnabled(boolean enable) {

    }

    @Override
    public void setPoster(@Nullable String thumb) {
        prepareView.setThumb(thumb);
        completeView.setThumb(thumb);
    }
}

package com.yoake.video_player.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.yoake.video_player.R;
import com.yoake.video_player.util.VideoPlayerConfig;

import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.player.VideoViewManager;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 准备播放界面
 */
public class PrepareView extends BaseCommentView implements IMinSize {


    private final ImageView mThumb;
    private final ImageView mStartPlay;
    private final ProgressBar mLoading;
    private final FrameLayout mNetWarning;
    // 创建淡出动画，使用alpha属性
    private ValueAnimator fadeOut = ValueAnimator.ofFloat(1.0f, 0.0f);

    public PrepareView(@NonNull Context context) {
        this(context, null);
    }


    public PrepareView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    {
        // 设置动画持续时间，单位毫秒
        fadeOut.setDuration(300);
        // 设置动画结束后保持最终状态
        fadeOut.setRepeatCount(0);
        fadeOut.setInterpolator(new LinearInterpolator());

        fadeOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                PrepareView.this.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mThumb.setVisibility(GONE);
                PrepareView.this.setAlpha(1f);
                PrepareView.this.setVisibility(GONE);
            }
        });

        LayoutInflater.from(getContext()).inflate(R.layout.v_player_layout_prepare_view, this, true);
        mThumb = findViewById(R.id.thumb);
        mStartPlay = findViewById(R.id.start_play);
        mStartPlay.setOnClickListener(v -> mControlWrapper.start());
        mLoading = findViewById(R.id.loading);
        mNetWarning = findViewById(R.id.net_warning_layout);
        findViewById(R.id.status_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mNetWarning.setVisibility(GONE);
                VideoViewManager.instance().setPlayOnMobileNetwork(true);
                mControlWrapper.start();
            }
        });
    }


    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {

    }

    @Override
    public void onPlayStateChanged(int playState) {
        switch (playState) {
            case VideoView.STATE_PREPARING:
                bringToFront();
                cancelAnim();
                this.setVisibility(VISIBLE);
                mStartPlay.setVisibility(GONE);
                mNetWarning.setVisibility(GONE);
                mLoading.setVisibility(VISIBLE);
                break;
            case VideoView.STATE_PREPARED:
                cancelAnim();
                if (VideoPlayerConfig.INSTANCE.getUseTransitionAnim()) {
                    if (!fadeOut.isRunning() && mThumb.getVisibility() == VISIBLE) {
                        fadeOut.start();
                    } else {
                        this.setVisibility(GONE);
                    }
                }
                break;
            case VideoView.STATE_PLAYING:
            case VideoView.STATE_PAUSED:
            case VideoView.STATE_ERROR:
            case VideoView.STATE_BUFFERING:
            case VideoView.STATE_BUFFERED:
            case VideoView.STATE_PLAYBACK_COMPLETED:
                if (!fadeOut.isRunning()) {
                    this.setVisibility(GONE);
                }
                break;
            case VideoView.STATE_IDLE:
                bringToFront();
                cancelAnim();
                this.setVisibility(VISIBLE);
                mLoading.setVisibility(GONE);
                mNetWarning.setVisibility(GONE);
                mStartPlay.setVisibility(VISIBLE);
                mThumb.setVisibility(VISIBLE);
                break;
            case VideoView.STATE_START_ABORT:
                cancelAnim();
                this.setVisibility(VISIBLE);
                mNetWarning.setVisibility(VISIBLE);
                mNetWarning.bringToFront();
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {

    }

    private void cancelAnim() {
        if (fadeOut.isRunning()) {
            // 在中途取消动画
            fadeOut.cancel();
            // 恢复视图状态
            setAlpha(1.0f);
        }
    }

    public void setThumb(@Nullable Object src) {
        int width = PlayerUtils.getScreenWidth(getContext(), false);
        int height = (int) (width * 9f / 16f);
        Glide.with(getContext()).load(src).override(width, height).into(mThumb);
    }

    @Override
    public void setMinSize() {
        int size = getContext().getResources().getDimensionPixelSize(R.dimen.r2dp36);
        int padding1 = getContext().getResources().getDimensionPixelSize(R.dimen.r2dp10);
        mStartPlay.setPadding(padding1, padding1, padding1, padding1);
        ViewGroup.LayoutParams layoutParams1 = mStartPlay.getLayoutParams();
        layoutParams1.width = size;
        layoutParams1.height = size;
        ViewGroup.LayoutParams layoutParams2 = mLoading.getLayoutParams();
        layoutParams2.width = size;
        layoutParams2.height = size;

    }

    @Override
    public void resetSize() {
        int size = getContext().getResources().getDimensionPixelSize(R.dimen.v_player_play_btn_size);
        int padding1 = getContext().getResources().getDimensionPixelSize(R.dimen.r2dp15);
        mStartPlay.setPadding(padding1, padding1, padding1, padding1);
        ViewGroup.LayoutParams layoutParams1 = mStartPlay.getLayoutParams();
        layoutParams1.width = size;
        layoutParams1.height = size;

        ViewGroup.LayoutParams layoutParams2 = mLoading.getLayoutParams();
        layoutParams2.width = size;
        layoutParams2.height = size;

    }
}

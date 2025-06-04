package com.yoake.video_player.component;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.yoake.video_player.R;
import com.yoake.video_player.util.VideoPlayerConfig;

import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 自动播放完成界面
 */
public class CompleteView extends BaseCommentView implements IMinSize {

    private final ImageView mThumb;
    private final ImageView mReplay;
    private final ImageView mStopFullscreen;

    public CompleteView(@NonNull Context context) {
        this(context, null);
    }

    public CompleteView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    {
        this.setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.v_player_layout_complete_view, this, true);
        mThumb = findViewById(R.id.thumb);
        mReplay = findViewById(R.id.iv_replay);
        mReplay.setOnClickListener(v -> mControlWrapper.replay(true));
        mStopFullscreen = findViewById(R.id.stop_fullscreen);
        mStopFullscreen.setOnClickListener(v -> {
            if (mControlWrapper.isFullScreen()) {
                Activity activity = PlayerUtils.scanForActivity(getContext());
                if (activity != null && !activity.isFinishing()) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mControlWrapper.stopFullScreen();
                }
            }
        });
        setClickable(true);
    }


    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {

    }

    @Override
    public void onPlayStateChanged(int playState) {
        if (playState == VideoView.STATE_PLAYBACK_COMPLETED) {
            this.setVisibility(VISIBLE);
            mStopFullscreen.setVisibility(mControlWrapper.isFullScreen() ? VISIBLE : GONE);
            bringToFront();
        } else {
            this.setVisibility(GONE);
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        if (playerState == VideoView.PLAYER_FULL_SCREEN) {
            mStopFullscreen.setVisibility(VISIBLE);
        } else if (playerState == VideoView.PLAYER_NORMAL) {
            mStopFullscreen.setVisibility(GONE);
        }

        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity != null && mControlWrapper.hasCutout()) {
            int orientation = activity.getRequestedOrientation();
            int cutoutHeight = mControlWrapper.getCutoutHeight();
            LayoutParams sflp = (LayoutParams) mStopFullscreen.getLayoutParams();
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                sflp.setMargins(0, 0, 0, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                sflp.setMargins(cutoutHeight, 0, 0, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                sflp.setMargins(0, 0, 0, 0);
            }
        }
    }

    public void setThumb(@Nullable Object src) {
        if (VideoPlayerConfig.INSTANCE.getCompleteShowPoster()) {
            mThumb.setVisibility(VISIBLE);
            int width = PlayerUtils.getScreenWidth(getContext(), false);
            int height = (int) (width * 9f / 16f);
            Glide.with(getContext()).load(src).override(width, height).into(mThumb);
            mReplay.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.v_player_selector_play_button));
        }
    }

    @Override
    public void setMinSize() {
        int size = getContext().getResources().getDimensionPixelSize(R.dimen.r2dp36);
        int padding1 = getContext().getResources().getDimensionPixelSize(R.dimen.r2dp8);
        mReplay.setPadding(padding1, padding1, padding1, padding1);
        ViewGroup.LayoutParams layoutParams1 = mReplay.getLayoutParams();
        layoutParams1.width = size;
        layoutParams1.height = size;

    }

    @Override
    public void resetSize() {
        int size = getContext().getResources().getDimensionPixelSize(R.dimen.v_player_play_btn_size);
        int padding1 = getContext().getResources().getDimensionPixelSize(R.dimen.r2dp12);
        mReplay.setPadding(padding1, padding1, padding1, padding1);
        ViewGroup.LayoutParams layoutParams1 = mReplay.getLayoutParams();
        layoutParams1.width = size;
        layoutParams1.height = size;
    }
}

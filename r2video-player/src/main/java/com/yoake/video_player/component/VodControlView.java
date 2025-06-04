package com.yoake.video_player.component;

import static xyz.doikki.videoplayer.util.PlayerUtils.stringForTime;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yoake.video_player.R;
import com.yoake.video_player.util.VideoControlHelper;

import java.util.HashMap;
import java.util.Map;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 点播底部控制栏
 */
public class VodControlView extends BaseCommentView implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {


    private final TextView mTotalTime;
    private final TextView mCurrTime;
    private final ImageView mFullScreen;
    private final LinearLayout mBottomContainer;
    private final SeekBar mVideoProgress;
    private final ProgressBar mBottomProgress;
    private final ImageView mPlayButton;

    private final TextView mSpeedTv;
    private final LinearLayout mSpeedLayout;
    private boolean mIsDragging;

    private boolean mIsShowBottomProgress = true;

    private final Map<Float, Integer> speedToIndex = new HashMap<>();

    public VodControlView(@NonNull Context context) {
        this(context, null);
    }

    public VodControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    {
        this.setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(getLayoutId(), this, true);
        mFullScreen = findViewById(R.id.fullscreen);
        mFullScreen.setOnClickListener(this);
        mBottomContainer = findViewById(R.id.bottom_container);
        mVideoProgress = findViewById(R.id.seekBar);
        mVideoProgress.setOnSeekBarChangeListener(this);
        mTotalTime = findViewById(R.id.total_time);
        mCurrTime = findViewById(R.id.curr_time);
        mPlayButton = findViewById(R.id.iv_play);
        mSpeedTv = findViewById(R.id.speed_tv);
        mSpeedLayout = findViewById(R.id.speed_layout);
        mPlayButton.setOnClickListener(this);
        mSpeedTv.setOnClickListener(this);
        mBottomProgress = findViewById(R.id.bottom_progress);
        initSpeed();
        //5.1以下系统SeekBar高度需要设置成WRAP_CONTENT
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mVideoProgress.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }

    protected int getLayoutId() {
        return R.layout.v_player_layout_vod_control_view;
    }

    /**
     * 是否显示底部进度条，默认显示
     */
    public void showBottomProgress(boolean isShow) {
        mIsShowBottomProgress = isShow;
    }


    @Override
    public void attach(@NonNull ControlWrapper controlWrapper) {
        super.attach(controlWrapper);

        setSpeed(mControlWrapper.getSpeed());
    }

    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {
        if (isVisible) {
            mBottomContainer.setVisibility(VISIBLE);
            if (anim != null) {
                mBottomContainer.startAnimation(anim);
            }
            if (mIsShowBottomProgress) {
                mBottomProgress.setVisibility(GONE);
            }
        } else {
            mBottomContainer.setVisibility(GONE);
            mSpeedLayout.setVisibility(GONE);

            if (anim != null) {
                mBottomContainer.startAnimation(anim);
                mSpeedLayout.startAnimation(anim);
            }
            if (mIsShowBottomProgress) {
                mBottomProgress.setVisibility(VISIBLE);
                AlphaAnimation animation = new AlphaAnimation(0f, 1f);
                animation.setDuration(300);
                mBottomProgress.startAnimation(animation);
            }
        }
    }

    @Override
    public void onPlayStateChanged(int playState) {
        switch (playState) {
            case VideoView.STATE_IDLE:
            case VideoView.STATE_PLAYBACK_COMPLETED:
                mSpeedLayout.setVisibility(View.GONE);
                this.setVisibility(GONE);
                mBottomProgress.setProgress(0);
                mBottomProgress.setSecondaryProgress(0);
                mVideoProgress.setProgress(0);
                mVideoProgress.setSecondaryProgress(0);
                break;
            case VideoView.STATE_START_ABORT:
            case VideoView.STATE_PREPARING:
            case VideoView.STATE_PREPARED:
            case VideoView.STATE_ERROR:
                this.setVisibility(GONE);
                break;
            case VideoView.STATE_PLAYING:
                mPlayButton.setSelected(true);
                if (mIsShowBottomProgress) {
                    if (mControlWrapper.isShowing()) {
                        mBottomProgress.setVisibility(GONE);
                        mBottomContainer.setVisibility(VISIBLE);
                    } else {
                        mBottomContainer.setVisibility(GONE);
                        mBottomProgress.setVisibility(VISIBLE);
                    }
                } else {
                    mBottomContainer.setVisibility(GONE);
                }
                this.setVisibility(VISIBLE);
                //开始刷新进度
                mControlWrapper.startProgress();
                break;
            case VideoView.STATE_PAUSED:
                mPlayButton.setSelected(false);
                break;
            case VideoView.STATE_BUFFERING:
                mPlayButton.setSelected(mControlWrapper.isPlaying());
                // 停止刷新进度
                mControlWrapper.stopProgress();
                break;
            case VideoView.STATE_BUFFERED:
                mPlayButton.setSelected(mControlWrapper.isPlaying());
                //开始刷新进度
                mControlWrapper.startProgress();
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        switch (playerState) {
            case VideoView.PLAYER_NORMAL:
                mFullScreen.setSelected(false);
                mSpeedTv.setVisibility(View.GONE);
                mSpeedLayout.setVisibility(View.GONE);
                break;
            case VideoView.PLAYER_FULL_SCREEN:
                mFullScreen.setSelected(true);
                Activity activity = PlayerUtils.scanForActivity(getContext());
                if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    mSpeedTv.setVisibility(View.VISIBLE);
                }
                break;

        }

        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity != null && mControlWrapper.hasCutout()) {
            int orientation = activity.getRequestedOrientation();
            int cutoutHeight = mControlWrapper.getCutoutHeight();
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                mBottomContainer.setPadding(0, 0, 0, 0);
                mBottomProgress.setPadding(0, 0, 0, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mBottomContainer.setPadding(cutoutHeight, 0, 0, 0);
                mBottomProgress.setPadding(cutoutHeight, 0, 0, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                mBottomContainer.setPadding(0, 0, cutoutHeight, 0);
                mBottomProgress.setPadding(0, 0, cutoutHeight, 0);
            }
        }
    }

    @Override
    public void setProgress(int duration, int position) {
        if (mIsDragging) {
            return;
        }

        if (mVideoProgress != null) {
            if (duration > 0) {
                mVideoProgress.setEnabled(true);
                int pos = (int) (position * 1.0 / duration * mVideoProgress.getMax());
                mVideoProgress.setProgress(pos);
                mBottomProgress.setProgress(pos);
            } else {
                mVideoProgress.setEnabled(false);
            }
            int percent = mControlWrapper.getBufferedPercentage();
            if (percent >= 95) { //解决缓冲进度不能100%问题
                mVideoProgress.setSecondaryProgress(mVideoProgress.getMax());
                mBottomProgress.setSecondaryProgress(mBottomProgress.getMax());
            } else {
                mVideoProgress.setSecondaryProgress(percent * 10);
                mBottomProgress.setSecondaryProgress(percent * 10);
            }
        }

        if (mTotalTime != null) mTotalTime.setText(stringForTime(duration));
        if (mCurrTime != null) mCurrTime.setText(stringForTime(position));
    }

    @Override
    public void onLockStateChanged(boolean isLocked) {
        onVisibilityChanged(!isLocked, null);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.fullscreen) {
            mControlWrapper.startFadeOut();
            if (mControlWrapper != null) {
                if (operationEventListener != null) {
                    operationEventListener.onToggleFullScreen();
                }
                VideoControlHelper.toggleFullScreen(getContext(), mControlWrapper);
            }
        } else if (id == R.id.iv_play) {
            mControlWrapper.togglePlay();
            mControlWrapper.startFadeOut();
        } else if (id == R.id.speed_tv) {
            if (mSpeedLayout.getVisibility() == View.GONE) {
                mControlWrapper.startFadeOut();
                mSpeedLayout.setVisibility(View.VISIBLE);
            } else {
                mSpeedLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsDragging = true;
        mControlWrapper.stopProgress();
        mControlWrapper.stopFadeOut();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        long duration = mControlWrapper.getDuration();
        long newPosition = (duration * seekBar.getProgress()) / mVideoProgress.getMax();
        mControlWrapper.seekTo((int) newPosition);
        mIsDragging = false;
        mControlWrapper.startProgress();
        mControlWrapper.startFadeOut();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) {
            return;
        }

        long duration = mControlWrapper.getDuration();
        long newPosition = (duration * progress) / mVideoProgress.getMax();
        if (mCurrTime != null) mCurrTime.setText(stringForTime((int) newPosition));
    }

    private void initSpeed() {
        // 定义一个速度数组
        float[] speeds = {1.0f, 1.25f, 1.5f, 1.75f, 2.0f};

        int count = mSpeedLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mSpeedLayout.getChildAt(i);
            // 为每个子控件设置点击事件
            final float speed = speeds[i]; // 获取对应的速度值
            speedToIndex.put(speed, i);
            child.setOnClickListener(v -> {
                mControlWrapper.setSpeed(speed); // 设置控制器的速度
                setSpeed(speed); // 设置界面上的速度
            });
        }
    }


    private void setSpeed(float speed) {
        // 设置速度文本
        mSpeedTv.setText("x" + speed);
        // 获取对应的索引，如果没有匹配的速度值则默认设置为 0
        Integer index = speedToIndex.get(speed);
        if (index == null) {
            index = 0;  // 处理 null 的情况，使用默认值 0
        }
        // 遍历子控件并设置选中状态
        int count = mSpeedLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            mSpeedLayout.getChildAt(i).setSelected(i == index);
        }
    }
}

package com.yoake.video_player.component;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yoake.video_player.R;
import com.yoake.video_player.util.PIPManager;

import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 播放器顶部标题栏
 */
public class TitleView extends BaseCommentView implements View.OnClickListener {


    private final LinearLayout mTitleContainer;
    private final TextView mTitle;
    private final ImageView floatWindowView;

    private boolean enableFloatWindow = true;
    private final ImageView backView;
    private boolean mIsRegister;//是否注册BatteryReceiver
    //PIP创建播控的时候会用到
    private Object thumbSrc;

    private boolean hideBackView = false;

    public TitleView(@NonNull Context context) {
        this(context, null);
    }

    public TitleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    {
        this.setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.v_player_layout_title_view, this, true);
        mTitleContainer = findViewById(R.id.title_container);
        floatWindowView = findViewById(R.id.float_window);
        backView = findViewById(R.id.backView);
        floatWindowView.setOnClickListener(this);
        backView.setOnClickListener(this);
        mTitle = findViewById(R.id.title);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }


    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {
        //只在全屏时才有效
        if (isVisible) {
            if (getVisibility() == GONE) {
                this.setVisibility(VISIBLE);
                if (anim != null) {
                    startAnimation(anim);
                }
            }
        } else {
            if (getVisibility() == VISIBLE) {
                this.setVisibility(GONE);
                if (anim != null) {
                    startAnimation(anim);
                }
            }
        }
    }

    @Override
    public void onPlayStateChanged(int playState) {
        switch (playState) {
            case VideoView.STATE_IDLE:
            case VideoView.STATE_START_ABORT:
            case VideoView.STATE_PREPARING:
            case VideoView.STATE_PREPARED:
            case VideoView.STATE_ERROR:
            case VideoView.STATE_PLAYBACK_COMPLETED:
                this.setVisibility(GONE);
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        if (mControlWrapper.isShowing() && !mControlWrapper.isLocked()) {
            this.setVisibility(VISIBLE);
        }
        mTitle.setSelected(true);

        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity != null && mControlWrapper.hasCutout()) {
            int orientation = activity.getRequestedOrientation();
            int cutoutHeight = mControlWrapper.getCutoutHeight();
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                mTitleContainer.setPadding(0, 0, 0, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mTitleContainer.setPadding(cutoutHeight, 0, 0, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                mTitleContainer.setPadding(0, 0, cutoutHeight, 0);
            }
        }

        if (playerState == VideoView.PLAYER_NORMAL) {
            if (enableFloatWindow) {
                floatWindowView.setVisibility(View.VISIBLE);
            }
            if (hideBackView) {
                backView.setVisibility(View.GONE);
            }
        } else {
            floatWindowView.setVisibility(View.GONE);
            backView.setVisibility(View.VISIBLE);
        }
        int[] mVideoSize = mControlWrapper.getVideoSize();
        if (mVideoSize != null && mVideoSize.length == 2) {
            if (mVideoSize[0] < mVideoSize[1]) {
                if (activity != null && mControlWrapper.hasCutout()) {
                    int cutoutHeight = mControlWrapper.getCutoutHeight();
                    MarginLayoutParams layoutParams = (MarginLayoutParams) mTitleContainer.getLayoutParams();
                    if (playerState == VideoView.PLAYER_NORMAL) {
                        layoutParams.topMargin = 0;
                    } else {
                        layoutParams.topMargin = cutoutHeight;
                    }
                }
            }
        }
    }


    @Override
    public void onLockStateChanged(boolean isLocked) {
        if (isLocked) {
            this.setVisibility(GONE);
        } else {
            this.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == backView) {
            Activity activity = PlayerUtils.scanForActivity(getContext());
            if (activity != null) {

                if (mControlWrapper.isFullScreen()) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mControlWrapper.startFadeOut();
                    mControlWrapper.stopFullScreen();
                } else {
                    activity.finish();
                }
            }
        } else if (v == floatWindowView) {
            if (getContext() instanceof ComponentActivity) {
                PIPManager.getInstance(getContext()).startFloatWindow((ComponentActivity) getContext(), thumbSrc);
            }
        }
    }


    public void setFloatWindowEnabled(boolean enabled) {
        this.enableFloatWindow = enabled;
        if (enabled) {
            this.floatWindowView.setVisibility(View.VISIBLE);
        } else {
            this.floatWindowView.setVisibility(View.GONE);
        }
    }

    public void setThumbSrc(Object thumbSrc) {
        this.thumbSrc = thumbSrc;
    }

    public void hideBackView() {
        hideBackView = true;
    }

}

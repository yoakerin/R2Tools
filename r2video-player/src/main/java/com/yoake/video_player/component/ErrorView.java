package com.yoake.video_player.component;

import static com.yoake.r2base.kit.R2ToastKitKt.toast;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yoake.video_player.R;

import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 播放出错提示界面
 * Created by Doikki on 2017/4/13.
 */
public class ErrorView extends BaseCommentView implements IMinSize {

    private float mDownX;
    private float mDownY;

    private final ImageView mStopFullscreen;

    private final TextView message;
    private final TextView statusBtn;

    public ErrorView(Context context) {
        this(context, null);
    }

    public ErrorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    {
        this.setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.v_player_layout_error_view, this, true);
        message = findViewById(R.id.message);
        statusBtn = findViewById(R.id.status_btn);
        statusBtn.setOnClickListener(v -> {
            int networkType = PlayerUtils.getNetworkType(getContext());
            if (networkType == PlayerUtils.NO_NETWORK) {
                toast(this, R.string.v_player_no_network);
                return;
            }
            this.setVisibility(GONE);
            mControlWrapper.replay(false);
        });
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
        if (playState == VideoView.STATE_ERROR) {
            bringToFront();
            this.setVisibility(VISIBLE);
            mStopFullscreen.setVisibility(mControlWrapper.isFullScreen() ? VISIBLE : GONE);
        } else if (playState == VideoView.STATE_IDLE) {
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


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                // True if the child does not want the parent to intercept touch events.
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float absDeltaX = Math.abs(ev.getX() - mDownX);
                float absDeltaY = Math.abs(ev.getY() - mDownY);
                if (absDeltaX > ViewConfiguration.get(getContext()).getScaledTouchSlop() || absDeltaY > ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setMinSize() {
        message.setVisibility(GONE);
        statusBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        int lr = getContext().getResources().getDimensionPixelSize(R.dimen.r2dp14);
        int tb = getContext().getResources().getDimensionPixelSize(R.dimen.r2dp4);
        statusBtn.setPadding(lr, tb, lr, tb);
    }

    @Override
    public void resetSize() {
        message.setVisibility(VISIBLE);
        statusBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        int lr = getContext().getResources().getDimensionPixelSize(R.dimen.r2dp18);
        int tb = getContext().getResources().getDimensionPixelSize(R.dimen.r2dp14);
        statusBtn.setPadding(lr, tb, lr, tb);
    }
}

package com.yoake.video_player.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yoake.video_player.R;
import com.yoake.video_player.util.PIPManager;

import xyz.doikki.videoplayer.player.VideoView;

public class MultiControlView extends BaseCommentView implements View.OnClickListener, IMinSize {


    private final ImageView mPlayButton;
    private final TextView mTitle;
    private int playState = VideoView.STATE_IDLE;
    //是否是副屏
    private boolean minMode = false;
    private final Runnable hide = new Runnable() {
        @Override
        public void run() {
            mTitle.setVisibility(GONE);
        }
    };

    public MultiControlView(@NonNull Context context) {
        this(context, null);
    }

    public MultiControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.v_player_layout_multi_max_control_view, this, true);
        mPlayButton = findViewById(R.id.start_play);
        mTitle = findViewById(R.id.title);
        mPlayButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_close) {
            PIPManager.getInstance(getContext()).stopFloatWindow();
            PIPManager.getInstance(getContext()).reset();
        } else if (id == R.id.start_play) {
            mControlWrapper.togglePlay();
        }
    }


    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {
        if (minMode) return;
        if (isVisible) {
            if (mPlayButton.getVisibility() == VISIBLE) return;
            mPlayButton.setVisibility(VISIBLE);
            mTitle.setVisibility(VISIBLE);
        } else {
            if (mPlayButton.getVisibility() == GONE) return;
            mPlayButton.setVisibility(GONE);
            mTitle.setVisibility(GONE);
        }
        mPlayButton.startAnimation(anim);
        mTitle.startAnimation(anim);
    }

    @Override
    public void onPlayStateChanged(int playState) {
        this.playState = playState;
        if (minMode) return;
        switch (playState) {
            case VideoView.STATE_IDLE:
            case VideoView.STATE_PAUSED:
                mPlayButton.setSelected(false);
                mPlayButton.setVisibility(VISIBLE);
                break;
            case VideoView.STATE_PLAYING:
            case VideoView.STATE_PREPARING:
            case VideoView.STATE_BUFFERING:
            case VideoView.STATE_PREPARED:
                mPlayButton.setSelected(true);
                mPlayButton.setVisibility(GONE);
                break;
            case VideoView.STATE_ERROR:
            case VideoView.STATE_PLAYBACK_COMPLETED:
                mPlayButton.setVisibility(GONE);
                bringToFront();
                break;
            case VideoView.STATE_BUFFERED:
                mPlayButton.setVisibility(GONE);
                mPlayButton.setSelected(mControlWrapper.isPlaying());
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
    }


    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public void setMinSize() {
        minMode = true;
        mPlayButton.setVisibility(GONE);
        mTitle.removeCallbacks(hide);
        mTitle.setVisibility(GONE);
    }

    @Override
    public void resetSize() {
        minMode = false;
        //暂停的时候需要显示播放按钮
        if (this.playState == VideoView.STATE_PAUSED) {
            mPlayButton.setVisibility(VISIBLE);
        }
        mTitle.setVisibility(VISIBLE);
        mTitle.removeCallbacks(hide);
        mTitle.postDelayed(hide, 4000);
    }

}
package com.yoake.video_player.component;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yoake.video_player.R;
import com.yoake.video_player.util.PIPManager;

import xyz.doikki.videoplayer.player.VideoView;

public class PIPControlView extends BaseCommentView implements View.OnClickListener {


    private final ImageView mPlayButton;

    public PIPControlView(@NonNull Context context) {
        this(context, null);
    }

    public PIPControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    {
        LayoutInflater.from(getContext()).inflate(R.layout.v_player_layout_pip_control_view, this, true);
        mPlayButton = findViewById(R.id.start_play);
        ImageView mClose = findViewById(R.id.btn_close);
        mClose.setOnClickListener(this);
        mPlayButton.setOnClickListener(this);
        findViewById(R.id.btn_skip).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_close) {
            PIPManager.getInstance(getContext()).stopFloatWindow();
            PIPManager.getInstance(getContext()).reset();
        } else if (id == R.id.start_play) {
            mControlWrapper.togglePlay();
        } else if (id == R.id.btn_skip) {
            if (PIPManager.getInstance(getContext()).getActClass() != null) {
                Intent intent = new Intent(getContext(), PIPManager.getInstance(getContext()).getActClass());
                Bundle mExtras = PIPManager.getInstance(getContext()).getExtras();
                if (mExtras != null) {
                    intent.putExtras(mExtras);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        }
    }


    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {
        if (isVisible) {
            if (mPlayButton.getVisibility() == VISIBLE) return;
            mPlayButton.setVisibility(VISIBLE);
        } else {
            if (mPlayButton.getVisibility() == GONE) return;
            mPlayButton.setVisibility(GONE);
        }
        mPlayButton.startAnimation(anim);
    }

    @Override
    public void onPlayStateChanged(int playState) {

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


}
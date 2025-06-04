package com.yoake.widgets.text;


import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;


public class R2MarqueeTextView extends AppCompatTextView {

    public R2MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }


    public R2MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public R2MarqueeTextView(Context context) {
        this(context, null);
    }


    private void initView(Context context) {

        this.setSingleLine(true);
        this.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        this.setMarqueeRepeatLimit(-1);

    }

    @Override
    public boolean isFocused() {
        //realmo must be return true ,have marquee effection.
        return true;
    }


}
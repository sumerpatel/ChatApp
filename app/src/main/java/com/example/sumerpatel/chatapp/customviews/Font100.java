package com.example.sumerpatel.chatapp.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * This {@link TextView} class is customised to set 'MuseoSans_100.otf' typeface.
 */
public class Font100 extends TextView {

    public Font100(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public Font100(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Font100(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/MuseoSans_100.otf");
            setTypeface(tf);
        }
    }

}
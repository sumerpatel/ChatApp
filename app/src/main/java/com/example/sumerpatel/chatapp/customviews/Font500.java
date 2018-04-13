package com.example.sumerpatel.chatapp.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * This {@link TextView} class is customised to set 'MuseoSans_500.otf' typeface.
 */
public class Font500 extends TextView {

    public Font500(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public Font500(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Font500(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/MuseoSans_500.otf");
            setTypeface(tf);
        }
    }
}
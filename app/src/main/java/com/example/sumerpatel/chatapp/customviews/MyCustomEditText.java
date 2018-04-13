package com.example.sumerpatel.chatapp.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;

/**
 * This {@link TextInputEditText} class is customised to set 'MuseoSans_100.otf' typeface.
 */
public class MyCustomEditText extends TextInputEditText {

    public MyCustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyCustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyCustomEditText(Context context) {
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
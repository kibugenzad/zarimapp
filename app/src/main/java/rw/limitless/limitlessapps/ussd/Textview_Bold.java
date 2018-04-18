package rw.limitless.limitlessapps.ussd;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by foram on 20/2/17.
 */

public class Textview_Bold extends TextView {

    public Textview_Bold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public Textview_Bold(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Textview_Bold(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Raleway-Bold.ttf");
            setTypeface(tf);
        }
    }

}

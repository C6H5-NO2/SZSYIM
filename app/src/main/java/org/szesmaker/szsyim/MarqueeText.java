package org.szesmaker.szsyim;
import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
public class MarqueeText extends AppCompatTextView {
    public MarqueeText(Context context) {
        super(context);
    }
    public MarqueeText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MarqueeText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override public boolean isFocused() {
        return true;
    }
}

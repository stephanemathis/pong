package fr.mathis.pong;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class FrameLayoutSquared extends FrameLayout {
    public FrameLayoutSquared(Context context) {
        super(context);
    }

    public FrameLayoutSquared(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FrameLayoutSquared(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
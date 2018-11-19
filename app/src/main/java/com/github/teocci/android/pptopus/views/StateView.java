package com.github.teocci.android.pptopus.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.github.teocci.android.pptopus.BuildConfig;

public class StateView extends View
{
    private final Paint[] paint;
    private int state;

    public StateView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(
                attrs,
                new int[]{android.R.attr.minHeight},
                android.R.attr.buttonStyle,
                0
        );

        if (a != null) {
            final int minHeight = a.getDimensionPixelSize(0, -1);
            if (minHeight != -1)
                setMinimumHeight(minHeight);
            a.recycle();
        }

        setWillNotDraw(false);

        paint = new Paint[2];

        paint[0] = new Paint();
        paint[0].setColor(Color.DKGRAY);

        paint[1] = new Paint();
        paint[1].setColor(Color.GREEN);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int suggestedMinHeight = getSuggestedMinimumHeight();
        setMeasuredDimension(suggestedMinHeight, suggestedMinHeight);
    }

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (state < paint.length) {
            final float cx = (getWidth() / 2);
            final float cy = (getHeight() / 2);
            final float cr = (cx - cx / 2f);
            canvas.drawCircle(cx, cy, cr, paint[state]);
        }
    }

    public void setIndicatorState(int state)
    {
        if (state < paint.length) {
            if (this.state != state) {
                this.state = state;
                invalidate();
            }
        } else if (BuildConfig.DEBUG)
            throw new AssertionError();
    }
}

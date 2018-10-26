package com.github.teocci.android.pptopus.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Button;

import com.github.teocci.android.pptopus.BuildConfig;
import com.github.teocci.android.pptopus.interfaces.SwitchButtonStateListener;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Oct-10
 */
public class SwitchButton extends Button
{
    private static final String LOG_TAG = SwitchButton.class.getSimpleName();

    private static final int STATE_IDLE = 0;
    private static final int STATE_DOWN = 1;
    private static final int STATE_DRAGGING_LEFT = 2;
    private static final int STATE_DRAGGING_RIGHT = 3;
    private static final int STATE_DRAGGING_DOWN = 4;
    private static final int STATE_LOCKED = 5;

    private SwitchButtonStateListener stateListener;
    private final Drawable defaultBackground;
    private final Drawable pressedBackground;
    private final int touchSlop;
    private final Paint paint;
    private Path pathLeft;
    private Path pathRight;
    private int state;
    private float touchX;
    private float touchY;

    public SwitchButton(Context context)
    {
        this(context, null);
    }

    public SwitchButton(Context context, AttributeSet attrs)
    {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public SwitchButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        defaultBackground = getBackground();
        pressedBackground = new ColorDrawable(getResources().getColor(android.R.color.holo_red_dark));

        final ViewConfiguration config = ViewConfiguration.get(context);
        touchSlop = config.getScaledTouchSlop();

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAlpha(80);

        state = STATE_IDLE;
    }

    public void setStateListener(SwitchButtonStateListener stateListener)
    {
        this.stateListener = stateListener;
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
    {
        final float centerX = (width / 2);
        final float centerY = (height / 2);
        final int hh = (height / 8);

        int w = (width / hh / 2);
        if (w < 14) {
            // Too small
            pathLeft = null;
            pathRight = null;
        } else {
            if (w > 20)
                w = 20;

            pathLeft = new Path();
            pathLeft.moveTo(centerX - hh * 2, centerY - hh); // 1
            pathLeft.lineTo(centerX - hh * (w - 4), centerY - hh); // 2
            pathLeft.lineTo(centerX - hh * (w - 4), centerY + hh * 2); // 3
            pathLeft.lineTo(centerX - hh * (w - 2), centerY + hh * 2); // 4
            pathLeft.lineTo(centerX - hh * (w - 5), centerY + hh * 4); // 5
            pathLeft.lineTo(centerX - hh * (w - 8), centerY + hh * 2); // 6
            pathLeft.lineTo(centerX - hh * (w - 6), centerY + hh * 2); // 7
            pathLeft.lineTo(centerX - hh * (w - 6), centerY + hh); // 8
            pathLeft.lineTo(centerX - hh * 2, centerY + hh); // 9
            pathLeft.close();

            pathRight = new Path();
            pathRight.moveTo(centerX + hh * 2, centerY - hh); // 1
            pathRight.lineTo(centerX + hh * (w - 4), centerY - hh); // 2
            pathRight.lineTo(centerX + hh * (w - 4), centerY + hh * 2); // 3
            pathRight.lineTo(centerX + hh * (w - 2), centerY + hh * 2); // 4
            pathRight.lineTo(centerX + hh * (w - 5), centerY + hh * 4); // 5
            pathRight.lineTo(centerX + hh * (w - 8), centerY + hh * 2); // 6
            pathRight.lineTo(centerX + hh * (w - 6), centerY + hh * 2); // 7
            pathRight.lineTo(centerX + hh * (w - 6), centerY + hh); // 8
            pathRight.lineTo(centerX + hh * 2, centerY + hh); // 9
            pathRight.close();
        }
    }

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if ((state == STATE_DOWN) && (pathLeft != null) && (pathRight != null)) {
            final int width = getWidth();
            final int height = getHeight();
            canvas.drawCircle(width / 2, height / 2, height / 8, paint);
            canvas.drawPath(pathLeft, paint);
            canvas.drawPath(pathRight, paint);
        }
    }

    public boolean onTouchEvent(MotionEvent ev)
    {
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isEnabled()) {
                    if (state == STATE_IDLE) {
                        setPressed(true);
                        setBackground(pressedBackground);
                        state = STATE_DOWN;
                        touchX = ev.getX();
                        touchY = ev.getY();
                        if (stateListener != null) {
                            stateListener.onStateChanged(true);
                        }
                        return true;
                    } else if (state == STATE_LOCKED) {
                        state = STATE_DOWN;
                        touchX = ev.getX();
                        touchY = ev.getY();
                        return true;
                    } else {
                        if (BuildConfig.DEBUG)
                            throw new AssertionError();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE: {
                final float x = ev.getX();
                final float y = ev.getY();
                final float dx = (x - touchX);
                final float dy = (y - touchY);

                switch (state) {
                    case STATE_IDLE:
                        break;

                    case STATE_DOWN:
                        if ((Math.abs(dx) > touchSlop) ||
                                (Math.abs(dy) > touchSlop)) {
                            if (Math.abs(dx) > Math.abs(dy)) {
                                if (dx > 0.0) {
                                    state = STATE_DRAGGING_RIGHT;
                                    Log.d(LOG_TAG, "STATE_DOWN -> STATE_DRAGGING_RIGHT");
                                } else if (dx < 0.0) {
                                    state = STATE_DRAGGING_LEFT;
                                    Log.d(LOG_TAG, "STATE_DOWN -> STATE_DRAGGING_LEFT");
                                }

                                getParent().requestDisallowInterceptTouchEvent(true);
                                touchX = x;
                                touchY = y;
                            }
                        }
                        return true;

                    case STATE_DRAGGING_RIGHT:
                        if ((dx > -0.5f) && (Math.abs(dx) > Math.abs(dy))) {
                            touchX = x;
                            touchY = y;
                        } else if (dy >= 0) {
                            touchX = x;
                            touchY = y;
                            state = STATE_DRAGGING_DOWN;
                            Log.d(LOG_TAG, "STATE_DRAGGING_RIGHT -> STATE_DRAGGING_DOWN");
                        } else {
                            getParent().requestDisallowInterceptTouchEvent(false);
                            state = STATE_IDLE;
                            Log.d(LOG_TAG, "STATE_DRAGGING_RIGHT -> STATE_IDLE");
                        }
                        return true;

                    case STATE_DRAGGING_LEFT:
                        if ((dx < 0.5f) && (Math.abs(dx) > Math.abs(dy))) {
                            touchX = x;
                            touchY = y;
                        } else if (dy >= 0) {
                            touchX = x;
                            touchY = y;
                            state = STATE_DRAGGING_DOWN;
                            Log.d(LOG_TAG, "STATE_DRAGGING_LEFT -> STATE_DRAGGING_DOWN");
                        } else {
                            getParent().requestDisallowInterceptTouchEvent(false);
                            state = STATE_IDLE;
                            Log.d(LOG_TAG, "STATE_DRAGGING_LEFT -> STATE_IDLE");
                        }
                        return true;

                    case STATE_DRAGGING_DOWN:
                        if ((dy > -1.0f) || (Math.abs(dx) < 1.0f)) {
                            touchX = x;
                            touchY = y;
                        } else {
                            getParent().requestDisallowInterceptTouchEvent(false);
                            state = STATE_IDLE;
                            Log.d(LOG_TAG, "STATE_DRAGGING_DOWN -> STATE_IDLE");
                        }
                        return true;
                }
            }
            break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (state == STATE_DRAGGING_DOWN) {
                    // Keep button pressed
                    state = STATE_LOCKED;
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    if (stateListener != null) {
                        stateListener.onStateChanged(false);
                    }
                    setBackground(defaultBackground);
                    setPressed(false);

                    if (state != STATE_IDLE) {
                        state = STATE_IDLE;
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean performClick()
    {
        super.performClick();
        return true;
    }
}

package com.ammuse.searchtoprogress;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class IndeterminateSearchDrawable extends Drawable implements Animatable {
    private static final int STROKE_WIDTH = 6;
    private static final int PADDING = 3;

    private final Paint paint;
    private final RectF bounds;
    private final RectF circleBounds;
    private final RectF handleBounds;
    private final float padding;
    private float startAngle;
    private float endAngle;
    private float rotation;
    private Animator animator;
    private Animator hideHandleAnimator;
    private Animator showHandleAnimator;
    private float handleEnd;

    private boolean drawHandle = true;
    private boolean isHandleHideRunning = false;

    public static IndeterminateSearchDrawable newInstance(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float strokeWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, STROKE_WIDTH, displayMetrics);
        float paddingPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING, displayMetrics);
        Paint paint = createPaint(fetchControlColour(context), strokeWidthPx);
        RectF bounds = new RectF();
        float padding = strokeWidthPx / 2 + paddingPx;
        IndeterminateSearchDrawable drawable = new IndeterminateSearchDrawable(paint, bounds, padding);
        drawable.createAnimator();
        return drawable;
    }

    @ColorInt
    private static int fetchControlColour(Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(0, new int[]{R.attr.colorControlActivated});
        try {
            return typedArray.getColor(0, 0);
        } finally {
            typedArray.recycle();
        }
    }

    static Paint createPaint(@ColorInt int colour, float strokeWidth) {
        Paint paint = new Paint();
        paint.setColor(colour);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        return paint;
    }

    IndeterminateSearchDrawable(Paint paint, RectF bounds, float padding) {
        this.paint = paint;
        this.bounds = bounds;
        this.circleBounds = new RectF(bounds);
        this.circleBounds.right = bounds.right * 2/3;
        this.circleBounds.bottom = bounds.bottom * 2/3;
        this.handleBounds = new RectF(bounds);
        this.handleBounds.left = bounds.right * 2/3;
        this.handleBounds.top = bounds.bottom * 2/3;
        this.padding = padding;

        this.startAngle = 0;
        this.endAngle = 360;
        this.rotation = 0;
    }

    private void createAnimator() {
        animator = IndeterminateAnimatorFactory.createIndeterminateDrawableAnimator(this);
        hideHandleAnimator = IndeterminateAnimatorFactory.createHideHandleAnimator(this);

        hideHandleAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Once the handle is hidden
                // start the Progress bar animation
                drawHandle = false;
                isHandleHideRunning = false;
                animator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });

        showHandleAnimator = IndeterminateAnimatorFactory.createShowHandleAnimator(this);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return paint.getAlpha();
    }

    @Override
    public void start() {
        isHandleHideRunning = true;
        hideHandleAnimator.start();
    }

    @Override
    public void stop() {
        // Stop the progress bar animation
        animator.cancel();

        // Start the animation that transforms the progress view circle to a complete 360 degree one.
        Animator stopProgressAnimator = IndeterminateAnimatorFactory.createStopProgressAnimator(this);
        stopProgressAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Once the complete circle is drawn start the animation
                // that will gradually show the magnifying glass handle
                drawHandle = true;
                showHandleAnimator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });

        stopProgressAnimator.start();
    }

    @Override
    public boolean isRunning() {
        return animator.isRunning();
    }

    @SuppressWarnings("unused")
    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
        invalidateSelf();
    }

    public float getStartAngle() {
        return startAngle;
    }

    public float getEndAngle() {
        return endAngle;
    }

    public float getRotation() {
        return rotation;
    }

    @SuppressWarnings("unused")
    public void setEndAngle(float endAngle) {
        this.endAngle = endAngle;
        invalidateSelf();
    }

    @SuppressWarnings("unused")
    public void setRotation(float rotation) {
        this.rotation = rotation;
        invalidateSelf();
    }

    @SuppressWarnings("unused")
    public void setHandleEnd(float endValue) {
        this.handleEnd = endValue;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        bounds.set(padding, padding, canvas.getWidth() - padding, canvas.getHeight() - padding);
        // The circle occupies 2/3 of the drawable area. The magnifying glass handle
        // is drawn at the 45 degrees diagonal and is 1/3 of the length of the diagonal
        this.circleBounds.set(padding, padding, (canvas.getWidth() - padding) * 2/3, (canvas.getHeight() - padding) * 2/3);
        this.handleBounds.set(bounds.width() * 2 / 3, bounds.height() * 2 / 3, bounds.width(), bounds.height());

        if(drawHandle) {
            canvas.drawLine(handleBounds.left,
                    handleBounds.top,
                    handleBounds.left + handleBounds.width() - (handleBounds.width() * handleEnd),
                    handleBounds.top + handleBounds.height() - (handleBounds.height() * handleEnd),
                    paint);
        }

        // When the hide handle animation is running draw the circle as if it's splitting
        // in 2 pieces from the opposite end of the handle towards it.
        // As the handle is drawn at 45 degrees rotation (0 degrees is at 3 o'clock)
        // the splitting point of the circle is at 225 degrees (that's in the middle of 10 and 11 o'clock).
        if(isHandleHideRunning) {
            float startAngle = 225f + 180 * handleEnd;
            float sweepAngle = 360 - 360 * handleEnd;
            canvas.drawArc(circleBounds, startAngle, sweepAngle, false, paint);
        }
        else {
            canvas.drawArc(circleBounds, rotation + startAngle, endAngle - startAngle, false, paint);
        }
    }
}

package com.ammuse.searchtoprogress;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by zatanasov on 4/4/16.
 */
public class IndeterminateAnimatorFactory {
    private static final String START_ANGLE = "startAngle";
    private static final String END_ANGLE = "endAngle";
    private static final String ROTATION = "rotation";
    private static final String HANDLE_END = "handleEnd";

    private static final int HANDLE_DURATION = 150;

    private static final int SWEEP_DURATION = 1333;
    private static final int ROTATION_DURATION = 6665;
    private static final float END_ANGLE_MAX = 405;
    private static final float START_ANGLE_MAX = END_ANGLE_MAX - 1;
    private static final int ROTATION_END_ANGLE = 719;


    private IndeterminateAnimatorFactory() {
        // NO-OP
    }

    public static Animator createIndeterminateDrawableAnimator(IndeterminateSearchDrawable drawable) {
        AnimatorSet animatorSet = new AnimatorSet();
        Animator startAngleAnimator = createStartAngleAnimator(drawable);
        Animator sweepAngleAnimator = createSweepAngleAnimator(drawable);
        Animator rotationAnimator = createAnimationAnimator(drawable);
        animatorSet.playTogether(startAngleAnimator, sweepAngleAnimator, rotationAnimator);
        return animatorSet;
    }

    private static Animator createStartAngleAnimator(IndeterminateSearchDrawable drawable) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(drawable, START_ANGLE, 44f, START_ANGLE_MAX);
        animator.setDuration(SWEEP_DURATION);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(createStartInterpolator());
        return animator;
    }

    //CHECKSTYLE IGNORE MagicNumber
    private static Interpolator createStartInterpolator() {
        Path path = new Path();
        path.cubicTo(0.3f, 0f, 0.1f, 0.75f, 0.5f, 0.85f);
        path.lineTo(1f, 1f);
        return PathInterpolatorCompat.create(path);
    }
    //CHECKSTYLE END IGNORE MagicNumber

    private static Animator createSweepAngleAnimator(IndeterminateSearchDrawable drawable) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(drawable, END_ANGLE, 45f, END_ANGLE_MAX);
        animator.setDuration(SWEEP_DURATION);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(createEndInterpolator());
        return animator;
    }

    //CHECKSTYLE IGNORE MagicNumber
    private static Interpolator createEndInterpolator() {
        Path path = new Path();
        path.lineTo(0.5f, 0.1f);
        path.cubicTo(0.7f, 0.15f, 0.6f, 0.75f, 1f, 1f);
        return PathInterpolatorCompat.create(path);
    }
    //CHECKSTYLE END IGNORE MagicNumber

    private static Animator createAnimationAnimator(IndeterminateSearchDrawable drawable) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(drawable, ROTATION, 0f, ROTATION_END_ANGLE);
        rotateAnimator.setDuration(ROTATION_DURATION);
        rotateAnimator.setRepeatMode(ValueAnimator.RESTART);
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        return rotateAnimator;
    }


    //region Search icon handle animators
    public static Animator createHideHandleAnimator(IndeterminateSearchDrawable drawable) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(drawable, HANDLE_END, 0f, 1f);
        animator.setDuration(HANDLE_DURATION * 2);
        animator.setInterpolator(new AnticipateInterpolator());
        return animator;
    }

    public static Animator createShowHandleAnimator(IndeterminateSearchDrawable drawable) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(drawable, HANDLE_END, 1f, 0f);
        animator.setDuration(HANDLE_DURATION);
        animator.setInterpolator(new OvershootInterpolator());
        return animator;
    }

    /**
     * Create an animator that will animate from rotating progress bar
     * to a full closed circle.
     * @param drawable
     * @return
     */
    public static Animator createStopProgressAnimator(IndeterminateSearchDrawable drawable) {
        AnimatorSet animatorSet = new AnimatorSet();
        Animator startAngleAnimator = createStartAngleHandleAnimator(drawable);
        Animator sweepAngleAnimator = createSweepAngleHandleAnimator(drawable);
        Animator rotationAnimator = createRotationHandleAnimator(drawable);

        if(drawable.getStartAngle() < drawable.getEndAngle()) {
            animatorSet.playTogether(sweepAngleAnimator, rotationAnimator);
        }
        else {
            animatorSet.playTogether(startAngleAnimator, rotationAnimator);
        }
        return animatorSet;
    }

    /**
     * Animate the start angle of the magnifying glass circle so that
     * it will connect with its other end
     * @param drawable
     * @return
     */
    private static Animator createStartAngleHandleAnimator(IndeterminateSearchDrawable drawable) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(drawable, START_ANGLE, Math.abs(drawable.getEndAngle() + 361));
        animator.setDuration(350l);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        return animator;
    }

    /**
     * Animate the sweep angle of the magnifying glass circle so that
     * it will connect with its other end
     * @param drawable
     * @return
     */
    private static Animator createSweepAngleHandleAnimator(IndeterminateSearchDrawable drawable) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(drawable, END_ANGLE, drawable.getStartAngle() + 360f);
        animator.setDuration(350l);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        return animator;
    }

    /**
     * Create an animator that will continue rotating at the same velocity/speed
     * as the Progress bar rotation animator.
     * @param drawable
     * @return
     */
    private static Animator createRotationHandleAnimator(IndeterminateSearchDrawable drawable) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(drawable, ROTATION, drawable.getRotation() +  ((float)ROTATION_END_ANGLE / ROTATION_DURATION) * 350f );
        rotateAnimator.setDuration(350l);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        return rotateAnimator;
    }



    //endregion
}

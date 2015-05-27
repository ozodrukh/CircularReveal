package io.codetail.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import io.codetail.animation.RevealAnimator;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class RevealFrameLayout extends FrameLayout implements RevealAnimator{

    private Path mRevealPath;

    private boolean mClipOutlines;

    private int mCenterX;
    private int mCenterY;
    private float mRadius;

    private View mTarget;

    private float mStartRadius;
    private float mEndRadius;

    private final Rect mTargetBounds = new Rect();

    public RevealFrameLayout(Context context) {
        this(context, null);
    }

    public RevealFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RevealFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mRevealPath = new Path();
    }

    /**
     * Animation target
     *
     * @hide
     */
    @Override
    public void setTarget(View view){
        mTarget = view;
        view.getHitRect(mTargetBounds);
    }

    /**
     * Epicenter of animation circle reveal
     *
     * @hide
     */
    @Override
    public void setCenter(int centerX, int centerY){
        mCenterX = centerX;
        mCenterY = centerY;
    }

    /**
     * Flag that animation is enabled
     *
     * @hide
     */
    @Override
    public void setClipOutlines(boolean clip){
        mClipOutlines = clip;
    }

    /**
     * Circle radius size
     *
     * @hide
     */
    @Override
    public void setRevealRadius(float radius){
        mRadius = radius;
        invalidate(mTargetBounds);
    }

    /**
     * Circle radius size
     *
     * @hide
     */
    @Override
    public float getRevealRadius(){
        return mRadius;
    }

    @Override
    public void setupStartValues() {
        mClipOutlines = false;
        mRadius = 0;
    }

    @Override
    public void setRadius(float start, float end) {
        mStartRadius = start;
        mEndRadius = end;
    }

    @Override
    public Rect getTargetBounds() {
        return mTargetBounds;
    }

    @Override
    public SupportAnimator startReverseAnimation() {
        return ViewAnimationUtils.createCircularReveal(mTarget, mCenterX, mCenterY,
                mEndRadius, mStartRadius);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if(!mClipOutlines){
            return super.drawChild(canvas, child, drawingTime);
        } else if(mTarget == child) {
            final int state = canvas.save();

            mRevealPath.reset();
            mRevealPath.addCircle(mCenterX, mCenterY, mRadius, Path.Direction.CW);

            canvas.clipPath(mRevealPath);

            boolean isInvalided = super.drawChild(canvas, child, drawingTime);

            canvas.restoreToCount(state);

            return isInvalided;
        }else{
            return super.drawChild(canvas, child, drawingTime);
        }
    }

}

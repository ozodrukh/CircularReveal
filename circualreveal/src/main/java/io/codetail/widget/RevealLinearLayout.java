package io.codetail.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;

public class RevealLinearLayout extends LinearLayoutCompat {

    Path mRevealPath;

    boolean mClipOutlines;

    float mCenterX;
    float mCenterY;
    float mRadius;

    View mTarget;

    public RevealLinearLayout(Context context) {
        this(context, null);
    }

    public RevealLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RevealLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mRevealPath = new Path();
    }

    private void setTarget(View view){
        mTarget = view;
    }

    private void setCenter(float centerX, float centerY){
        mCenterX = centerX;
        mCenterY = centerY;
    }

    private void setClipOutlines(boolean clip){
        mClipOutlines = clip;
    }

    private void setRevealRadius(float radius){
        mRadius = radius;
        invalidate();
    }

    private float getRevealRadius(){
        return mRadius;
    }


    @Override
    protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
        if (!mClipOutlines && child != mTarget)
            return super.drawChild(canvas, child, drawingTime);

        final int state = canvas.save();

        mRevealPath.reset();
        mRevealPath.addCircle(mCenterX, mCenterY, mRadius, Path.Direction.CW);

        canvas.clipPath(mRevealPath);

        boolean isInvalided = super.drawChild(canvas, child, drawingTime);

        canvas.restoreToCount(state);

        return isInvalided;
    }
}

package io.codetail.animation;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.animation.Interpolator;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
final class SupportAnimatorLollipop extends SupportAnimator{

    Animator mNativeAnimator;

    SupportAnimatorLollipop(Animator animator) {
        mNativeAnimator = animator;
    }

    @Override
    public boolean isNativeAnimator() {
        return true;
    }

    @Override
    public Object get() {
        return mNativeAnimator;
    }


    @Override
    public void start() {
        mNativeAnimator.start();
    }

    @Override
    public void setDuration(int duration) {
        mNativeAnimator.setDuration(duration);
    }

    @Override
    public void setInterpolator(Interpolator value) {
        mNativeAnimator.setInterpolator(value);
    }

    @Override
    public boolean isRunning() {
        return mNativeAnimator.isRunning();
    }
}

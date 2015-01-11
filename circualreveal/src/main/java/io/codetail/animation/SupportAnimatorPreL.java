package io.codetail.animation;

import android.view.animation.Interpolator;

import com.nineoldandroids.animation.Animator;

final class SupportAnimatorPreL extends SupportAnimator {

    Animator mSupportFramework;

    SupportAnimatorPreL(Animator animator) {
        mSupportFramework = animator;
    }

    @Override
    public boolean isNativeAnimator() {
        return false;
    }

    @Override
    public Object get() {
        return mSupportFramework;
    }

    @Override
    public void start() {
        mSupportFramework.start();
    }

    @Override
    public void setDuration(int duration) {
        mSupportFramework.setDuration(duration);
    }

    @Override
    public void setInterpolator(Interpolator value) {
        mSupportFramework.setInterpolator(value);
    }

    @Override
    public boolean isRunning() {
        return mSupportFramework.isRunning();
    }
}

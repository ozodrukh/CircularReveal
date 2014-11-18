package io.codetail.animation;

import android.graphics.Rect;
import android.view.View;

import com.nineoldandroids.animation.Animator;

import java.lang.ref.WeakReference;

/**
 * @hide
 */
public interface RevealAnimator{
        
    public void setClipOutlines(boolean clip);

    public void setCenter(float cx, float cy);

    public void setTarget(View target);

    public void setRevealRadius(float value);

    public float getRevealRadius();

    public void invalidate(Rect bounds);

    static class RevealFinished extends ViewAnimationUtils.SimpleAnimationListener {
        WeakReference<RevealAnimator> mReference;
        volatile Rect mInvalidateBounds;

        RevealFinished(RevealAnimator target, Rect bounds) {
            mReference = new WeakReference<RevealAnimator>(target);
            mInvalidateBounds = bounds;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);

            RevealAnimator target = mReference.get();

            if(target == null){
                return;
            }

            target.setClipOutlines(false);
            target.setCenter(0, 0);
            target.setTarget(null);
            target.invalidate(mInvalidateBounds);
        }
    }

}
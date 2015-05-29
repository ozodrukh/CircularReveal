package io.codetail.animation;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.util.FloatProperty;

import java.lang.ref.WeakReference;

import static io.codetail.animation.ViewAnimationUtils.SimpleAnimationListener;

/**
 * @hide
 */
public interface RevealAnimator{

    RevealRadius CLIP_RADIUS = new RevealRadius();

    /**
     * Listen when animation start/end/cancel
     * and setup view for it
     */
    void onRevealAnimationStart();
    void onRevealAnimationEnd();
    void onRevealAnimationCancel();

    /**
     * Used with animator to animate view clipping
     *
     * @param value clip radius
     */
    void setRevealRadius(float value);

    /**
     * Used with animator to animate view clipping
     *
     * @return current radius
     */
    float getRevealRadius();

    /**
     * Invalidate certain rectangle
     *
     * @param bounds bounds to redraw
     * @see View#invalidate(Rect)
     */
    void invalidate(Rect bounds);

    /**
     * {@link ViewAnimationUtils#createCircularReveal(View, int, int, float, float)} is
     * called it creates new {@link io.codetail.animation.RevealAnimator.RevealInfo}
     * and attaches to parent, here is necessary data about animation
     *
     * @param info reveal information
     *
     * @see RevealAnimator.RevealInfo
     */
    void attachRevealInfo(RevealInfo info);

    /**
     * Returns new {@link SupportAnimator} that plays
     * reversed animation of current one
     *
     * This method might be temporary, you should call
     * {@link SupportAnimator#reverse()} instead
     *
     * @hide
     * @return reverse {@link SupportAnimator}
     *
     * @see SupportAnimator#reverse()
     */
    SupportAnimator startReverseAnimation();

    class RevealInfo{
        public final int centerX;
        public final int centerY;
        public final float startRadius;
        public final float endRadius;
        public final WeakReference<View> target;

        public RevealInfo(int centerX, int centerY, float startRadius, float endRadius,
                          WeakReference<View> target) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.startRadius = startRadius;
            this.endRadius = endRadius;
            this.target = target;
        }

        public View getTarget(){
            return target.get();
        }

        public boolean hasTarget(){
            return getTarget() != null;
        }
    }

    class RevealFinishedGingerbread extends SimpleAnimationListener {
        WeakReference<RevealAnimator> mReference;

        RevealFinishedGingerbread(RevealAnimator target) {
            mReference = new WeakReference<>(target);
        }

        @Override
        public void onAnimationStart(Animator animation) {
            RevealAnimator target = mReference.get();
            target.onRevealAnimationStart();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            RevealAnimator target = mReference.get();
            target.onRevealAnimationCancel();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            RevealAnimator target = mReference.get();
            target.onRevealAnimationEnd();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class RevealFinishedIceCreamSandwich extends RevealFinishedGingerbread {
        int mFeaturedLayerType;
        int mLayerType;

        RevealFinishedIceCreamSandwich(RevealAnimator target) {
            super(target);

            mLayerType = ((View) target).getLayerType();
            mFeaturedLayerType = View.LAYER_TYPE_SOFTWARE;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            ((View) mReference.get()).setLayerType(mLayerType, null);
            super.onAnimationEnd(animation);
        }

        @Override
        public void onAnimationStart(Animator animation) {
            ((View) mReference.get()).setLayerType(mFeaturedLayerType, null);
            super.onAnimationStart(animation);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            ((View) mReference.get()).setLayerType(mLayerType, null);
            super.onAnimationEnd(animation);
        }
    }

    class RevealFinishedJellyBeanMr2 extends RevealFinishedIceCreamSandwich {

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        RevealFinishedJellyBeanMr2(RevealAnimator target) {
            super(target);

            mFeaturedLayerType = View.LAYER_TYPE_HARDWARE;
        }
    }

    class RevealRadius extends FloatProperty<RevealAnimator> {

        public RevealRadius() {
            super("revealRadius");
        }

        @Override
        public void setValue(RevealAnimator object, float value) {
            object.setRevealRadius(value);
        }

        @Override
        public Float get(RevealAnimator object) {
            return object.getRevealRadius();
        }
    }
}
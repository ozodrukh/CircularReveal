package io.codetail.animation;


import android.annotation.TargetApi;
import android.os.Build;
import android.view.animation.Interpolator;

import java.util.ArrayList;

public class Animator {

    public final static boolean LOLLIPOP = Build.VERSION.SDK_INT >= 21;

    android.animation.Animator mNativeAnimator;
    com.nineoldandroids.animation.Animator mSupportAnimator;

    /**
     * @hide
     */
    public Animator(com.nineoldandroids.animation.Animator animator) {
        mSupportAnimator = animator;
    }

    /**
     * @hide
     */
    public Animator(android.animation.Animator animator){
        mNativeAnimator = animator;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Animator setInterpolator(Interpolator interpolator){
        if(LOLLIPOP){
            mNativeAnimator.setInterpolator(interpolator);
        }else{
            mSupportAnimator.setInterpolator(interpolator);
        }
        return this;
    }

    /**
     * Sets the duration of the animation.
     *
     * @param duration The length of the animation, in milliseconds.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Animator setDuration(int duration){
        if(LOLLIPOP){
            mNativeAnimator.setDuration(duration);
        }else{
            mSupportAnimator.setDuration(duration);
        }
        return this;
    }

    public Animator addListener(com.nineoldandroids.animation.Animator.AnimatorListener listener){
        mSupportAnimator.addListener(listener);
        return this;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Animator addListener(android.animation.Animator.AnimatorListener listener){
        mNativeAnimator.addListener(listener);
        return this;
    }

    public Animator addPauseListener(com.nineoldandroids.animation.Animator.AnimatorPauseListener pauseListener){
        mSupportAnimator.addPauseListener(pauseListener);
        return this;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public Animator addPauseListener(android.animation.Animator.AnimatorPauseListener pauseListener){
        mNativeAnimator.addPauseListener(pauseListener);
        return this;
    }


    /**
     * Gets the duration of the animation.
     *
     * @return The length of the animation, in milliseconds.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public long getDuration(){
        if(LOLLIPOP){
            return mNativeAnimator.getDuration();
        }else{
            return mSupportAnimator.getDuration();
        }
    }

    /**
     * Returns whether this Animator is currently running (having been started and gone past any
     * initial startDelay period and not yet ended).
     *
     * @return Whether the Animator is running.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean isRunning(){
        if(LOLLIPOP){
            return mNativeAnimator.isRunning();
        }else{
            return mSupportAnimator.isRunning();
        }
    }

    /**
     * Returns whether this Animator has been started and not yet ended. This state is a superset
     * of the state of {@link #isRunning()}, because an Animator with a nonzero
     * will return true for {@link #isStarted()} during the
     * delay phase, whereas {@link #isRunning()} will return true only after the delay phase
     * is complete.
     *
     * @return Whether the Animator has been started and not yet ended.
     */
    public boolean isStarted() {
        // Default method returns value for isRunning(). Subclasses should override to return a
        // real value.
        return isRunning();
    }

    /**
     * Removes a listener from the set listening to this animation.
     *
     * @param listener the listener to be removed from the current set of listeners for this
     *                 animation.
     */
    public void removeListener(com.nineoldandroids.animation.Animator.AnimatorListener listener) {
        mSupportAnimator.removeListener(listener);
    }

    /**
     * Removes a listener from the set listening to this animation.
     *
     * @param listener the listener to be removed from the current set of listeners for this
     *                 animation.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void removeListener(android.animation.Animator.AnimatorListener listener) {
        mNativeAnimator.removeListener(listener);
    }

    /**
     * Gets the set of {@link android.animation.Animator.AnimatorListener} objects that are currently
     * listening for events on this <code>Animator</code> object.
     *
     * @return ArrayList<AnimatorListener> The set of listeners.
     */
    public ArrayList<com.nineoldandroids.animation.Animator.AnimatorListener> getSupportListeners() {
        return mSupportAnimator.getListeners();
    }

    /**
     * Gets the set of {@link android.animation.Animator.AnimatorListener} objects that are currently
     * listening for events on this <code>Animator</code> object.
     *
     * @return ArrayList<AnimatorListener> The set of listeners.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ArrayList<android.animation.Animator.AnimatorListener> getNativeListeners() {
        return mNativeAnimator.getListeners();
    }

    /**
     * Removes a pause listener from the set listening to this animation.
     *
     * @param listener the listener to be removed from the current set of pause
     * listeners for this animation.
     */
    public void removePauseListener(com.nineoldandroids.animation.Animator.AnimatorPauseListener listener) {
        mSupportAnimator.removePauseListener(listener);
    }

    /**
     * Removes a pause listener from the set listening to this animation.
     *
     * @param listener the listener to be removed from the current set of pause
     * listeners for this animation.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void removePauseListener(android.animation.Animator.AnimatorPauseListener listener) {
        mNativeAnimator.removePauseListener(listener);
    }

    /**
     * Removes all {@link #addListener(com.nineoldandroids.animation.Animator.AnimatorListener)}  listeners}
     * and {@link #addPauseListener(com.nineoldandroids.animation.Animator.AnimatorPauseListener)}
     * pauseListeners} from this object.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void removeAllListeners() {
        if(LOLLIPOP){
            mNativeAnimator.removeAllListeners();
        }else{
            mSupportAnimator.removeAllListeners();
        }
    }

    /**
     * Starts this animation. If the animation has a nonzero startDelay, the animation will start
     * running after that delay elapses. A non-delayed animation will have its initial
     * value(s) set immediately, followed by calls to AnimationUpdate for any listeners of this animator.
     *
     * <p>The animation started by calling this method will be run on the thread that called
     * this method. This thread should have a Looper on it (a runtime exception will be thrown if
     * this is not the case). Also, if the animation will animate
     * properties of objects in the view hierarchy, then the calling thread should be the UI
     * thread for that view hierarchy.</p>
     *
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void start() {
        if(LOLLIPOP){
            mNativeAnimator.start();
        }else{
            mSupportAnimator.start();
        }
    }


    public com.nineoldandroids.animation.Animator getSupportAnimator(){
        return mSupportAnimator;
    }

    public android.animation.Animator getNativeAnimator(){
        return mNativeAnimator;
    }

}

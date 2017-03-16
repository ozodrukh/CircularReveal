package io.codetail.animation;

import android.animation.ValueAnimator;

/**
 * created at 3/15/17
 *
 * @author Ozodrukh
 * @version 1.0
 */

class DynamicAnimator<T extends DynamicAnimation<T>>
    extends ValueAnimator implements DynamicAnimation.OnAnimationEndListener {
  private final DynamicAnimation<T> animation;

  public DynamicAnimator(DynamicAnimation<T> animation) {
    this.animation = animation;
    this.animation.addEndListener(this);
  }

  @Override public void start() {
    animation.start();

    for (AnimatorListener listener : getListeners()) {
      listener.onAnimationStart(this);
    }
  }

  @Override public void cancel() {
    animation.cancel();
  }

  @Override public boolean isRunning() {
    return animation.isRunning();
  }

  @Override public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value,
      float velocity) {
    for (AnimatorListener listener : getListeners()) {
      if (canceled) {
        listener.onAnimationCancel(this);
      } else {
        listener.onAnimationEnd(this);
      }
    }
  }
}

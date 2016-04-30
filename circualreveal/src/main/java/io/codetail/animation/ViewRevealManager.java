package io.codetail.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.Property;
import android.view.View;
import java.util.HashMap;
import java.util.Map;

public class ViewRevealManager {
  public static final RevealRadius REVEAL = new RevealRadius();

  private Map<View, RevealViewData> targets = new HashMap<>();

  public ViewRevealManager() {

  }

  protected ObjectAnimator start(RevealViewData data) {
    targets.put(data.target, data);

    ObjectAnimator animator =
        ObjectAnimator.ofFloat(data, REVEAL, data.startRadius, data.endRadius);

    animator.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationStart(Animator animation) {
        RevealViewData data = getTarget(animation);
        data.animating = true;
        targets.put(data.target, data);
      }

      @Override public void onAnimationEnd(Animator animation) {
        targets.remove(getTarget(animation).target).animating = false;
      }

      RevealViewData getTarget(Animator animator) {
        ObjectAnimator a = (ObjectAnimator) animator;
        return (RevealViewData) a.getTarget();
      }
    });

    return data.animator = animator;
  }

  /**
   * @return Map of started animators
   */
  protected final Map<View, RevealViewData> getTargets() {
    return targets;
  }

  /**
   * @return True if you don't want use Android native reveal animator
   * in order to use your own custom one
   */
  protected boolean hasCustomerRevealAnimator() {
    return false;
  }

  /**
   * @return True if animation was started and it is still running,
   * otherwise returns False
   */
  public boolean isAnimating(View child) {
    RevealViewData data = targets.get(child);
    return data != null && data.animator != null && data.animator.isRunning();
  }

  /**
   * Applies path clipping on a canvas before drawing child,
   * you should save canvas state before transformation and
   * restore it afterwards
   *
   * @param canvas Canvas to apply clipping before drawing
   * @param child Reveal animation target
   * @return True if transformation was successfully applied on
   * referenced child, otherwise child be not the target and
   * therefore animation was skipped
   */
  public boolean transform(Canvas canvas, View child) {
    final RevealViewData revealData = targets.get(child);

    return revealData != null && revealData.transform(canvas, child);
  }

  public static final class RevealViewData {
    public final int centerX;
    public final int centerY;

    public final float startRadius;
    public final float endRadius;

    /* Flag that indicates whether view is in animation mode, mutable */
    public boolean animating;

    /* Revealed radius */
    public float radius;

    /* Animation target */ View target;

    ObjectAnimator animator;

    /*
    * Android Canvas is tricky, we cannot clip circles directly with Canvas API
    * but it is allowed using Path, therefore we use it :|
    */ Path path = new Path();

    RevealViewData(View target, int centerX, int centerY, float startRadius, float endRadius) {
      this.target = target;
      this.centerX = centerX;
      this.centerY = centerY;
      this.startRadius = startRadius;
      this.endRadius = endRadius;
      this.path = new Path();
    }

    /**
     * Applies path clipping on a canvas before drawing child,
     * you should save canvas state before transformation and
     * restore it afterwards
     *
     * @param canvas Canvas to apply clipping before drawing
     * @param child Reveal animation target
     * @return True if transformation was successfully  applied on
     * referenced child, otherwise child be not the target and
     * therefore animation was skipped
     */
    boolean transform(Canvas canvas, View child) {
      if (child != target || !animating) {
        return false;
      }

      path.reset();
      path.addCircle(centerX, centerY, radius, Path.Direction.CW);

      canvas.clipPath(path);
      return true;
    }
  }

  /**
   * Property animator. For performance improvements better to use
   * directly variable member (but it's little enhancement that always
   * caught as dangerous, let's see)
   */
  private static final class RevealRadius extends Property<RevealViewData, Float> {

    public RevealRadius() {
      super(Float.class, "supportCircularReveal");
    }

    @Override public void set(RevealViewData v, Float value) {
      v.radius = value;
      v.target.invalidate();
    }

    @Override public Float get(RevealViewData v) {
      return v.radius;
    }
  }

  public static class EnhanceViewAnimatorAdapter extends AnimatorListenerAdapter {
    private RevealViewData viewData;
    private int featuredLayerType;
    private int originalLayerType;

    EnhanceViewAnimatorAdapter(RevealViewData viewData, int layerType) {
      this.viewData = viewData;
      this.featuredLayerType = layerType;
      this.originalLayerType = viewData.target.getLayerType();
    }

    @Override
    public void onAnimationStart(Animator animation) {
      viewData.target.setLayerType(featuredLayerType, null);
    }

    @Override
    public void onAnimationCancel(Animator animation) {
      viewData.target.setLayerType(originalLayerType, null);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
      viewData.target.setLayerType(originalLayerType, null);
    }
  }
}

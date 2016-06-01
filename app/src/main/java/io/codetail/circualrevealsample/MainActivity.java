package io.codetail.circualrevealsample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Aware section
 * https://www.google.com/design/spec/motion/material-motion.html#material-motion-how-does-material-move
 */
public class MainActivity extends AppCompatActivity {
  final static int SLOW_DURATION = 400;
  final static int FAST_DURATION = 200;

  @BindView(R.id.circlesLine) ViewGroup circlesLine;
  @BindView(R.id.cardsLine) ViewGroup cardsLine;
  @BindView(R.id.activator_mask) CardView activatorMask;

  private float maskElevation;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
  }

  @OnClick(R.id.activator) void activateAwareMotion(View target) {
    // Cancel all concurrent events on view
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      target.cancelPendingInputEvents();
    }
    target.setEnabled(false);

    // Coordinates of circle initial point
    final ViewGroup parent = (ViewGroup) activatorMask.getParent();
    final Rect bounds = new Rect();
    final Rect maskBounds = new Rect();

    target.getDrawingRect(bounds);
    activatorMask.getDrawingRect(maskBounds);
    parent.offsetDescendantRectToMyCoords(target, bounds);
    parent.offsetDescendantRectToMyCoords(activatorMask, maskBounds);

    // Put Mask view at circle initial points
    maskElevation = activatorMask.getCardElevation();
    activatorMask.setCardElevation(0);
    activatorMask.setVisibility(View.VISIBLE);
    activatorMask.setX(bounds.left - maskBounds.centerX());
    activatorMask.setY(bounds.top - maskBounds.centerY());

    circlesLine.setVisibility(View.INVISIBLE);

    final int cX = maskBounds.centerX();
    final int cY = maskBounds.centerY();

    Animator circularReveal =
        ViewAnimationUtils.createCircularReveal(activatorMask, cX, cY, target.getWidth() / 2,
            (float) Math.hypot(maskBounds.width() * .5f, maskBounds.height() * .5f),
            View.LAYER_TYPE_HARDWARE);

    final float c0X = bounds.centerX() - maskBounds.centerX();
    final float c0Y = bounds.centerY() - maskBounds.centerY();

    AnimatorPath path = new AnimatorPath();
    path.moveTo(c0X, c0Y);
    path.curveTo(c0X, c0Y, 0, c0Y, 0, 0);

    ObjectAnimator pathAnimator = ObjectAnimator.ofObject(this, "maskLocation", new PathEvaluator(),
        path.getPoints().toArray());

    AnimatorSet set = new AnimatorSet();
    set.playTogether(circularReveal, pathAnimator);
    set.setInterpolator(new FastOutSlowInInterpolator());
    set.setDuration(SLOW_DURATION);
    set.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        executeCardsSequentialAnimation();
        activatorMask.setCardElevation(maskElevation);
      }
    });
    set.start();
  }

  private void executeCardsSequentialAnimation() {
    final int length = cardsLine.getChildCount();
    cardsLine.setVisibility(View.VISIBLE);

    final Animator[] animators = new Animator[length];
    for (int i = 0; i < length; i++) {
      View target = cardsLine.getChildAt(i);
      final float x0 = 0;// i == 0 ? 0 : -10 * (1 + i * 0.2f);
      final float y0 = 10 * i;

      target.setTranslationX(x0);
      target.setTranslationY(y0);

      AnimatorPath path = new AnimatorPath();
      path.moveTo(x0, y0);
      path.lineTo(0, 0);

      PathPoint[] points = new PathPoint[path.getPoints().size()];
      path.getPoints().toArray(points);

      AnimatorSet set = new AnimatorSet();
      set.play(ObjectAnimator.ofObject(target, PATH_POINT, new PathEvaluator(), points))
          .with(ObjectAnimator.ofFloat(target, View.ALPHA, 0.8f, 1f));

      animators[i] = set;
      animators[i].setStartDelay(15 * i);
    }

    final AnimatorSet sequential = new AnimatorSet();
    sequential.playTogether(animators);
    sequential.setInterpolator(new FastOutLinearInInterpolator());
    sequential.setDuration(FAST_DURATION);
    sequential.start();
  }

  @OnClick(R.id.reset) void resetUi(View resetCard) {
    cardsLine.setVisibility(View.INVISIBLE);

    final View target = ButterKnife.findById(this, R.id.activator);

    // Coordinates of circle initial point
    final ViewGroup parent = (ViewGroup) activatorMask.getParent();
    final Rect bounds = new Rect();
    final Rect maskBounds = new Rect();

    target.getDrawingRect(bounds);
    activatorMask.getDrawingRect(maskBounds);
    parent.offsetDescendantRectToMyCoords(target, bounds);
    parent.offsetDescendantRectToMyCoords(activatorMask, maskBounds);

    maskElevation = activatorMask.getCardElevation();
    activatorMask.setCardElevation(0);

    final int cX = maskBounds.centerX();
    final int cY = maskBounds.centerY();

    final Animator circularReveal = ViewAnimationUtils.createCircularReveal(activatorMask, cX, cY,
        (float) Math.hypot(maskBounds.width() * .5f, maskBounds.height() * .5f),
        target.getWidth() / 2f, View.LAYER_TYPE_HARDWARE);

    final float c0X = bounds.centerX() - maskBounds.centerX();
    final float c0Y = bounds.centerY() - maskBounds.centerY();

    AnimatorPath path = new AnimatorPath();
    path.moveTo(0, 0);
    path.curveTo(0, 0, 0, c0Y, c0X, c0Y);

    ObjectAnimator pathAnimator = ObjectAnimator.ofObject(this, "maskLocation", new PathEvaluator(),
        path.getPoints().toArray());

    AnimatorSet set = new AnimatorSet();
    set.playTogether(circularReveal, pathAnimator);
    set.setInterpolator(new FastOutSlowInInterpolator());
    set.setDuration(SLOW_DURATION);
    set.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        activatorMask.setCardElevation(maskElevation);
        activatorMask.setVisibility(View.INVISIBLE);

        circlesLine.setVisibility(View.VISIBLE);
        executeCirclesDropDown();
        target.setEnabled(true);
      }
    });
    set.start();
  }

  private void executeCirclesDropDown() {
    final int length = circlesLine.getChildCount();
    Animator[] animators = new Animator[length];
    for (int i = 0; i < length; i++) {
      View target = circlesLine.getChildAt(i);
      final float x0 = -10 * i;
      final float y0 = -10 * i;

      target.setTranslationX(x0);
      target.setTranslationY(y0);

      AnimatorPath path = new AnimatorPath();
      path.moveTo(x0, y0);
      path.curveTo(x0, y0, 0, y0, 0, 0);

      PathPoint[] points = new PathPoint[path.getPoints().size()];
      path.getPoints().toArray(points);

      AnimatorSet set = new AnimatorSet();
      set.play(ObjectAnimator.ofObject(target, PATH_POINT, new PathEvaluator(), points))
          .with(ObjectAnimator.ofFloat(target, View.ALPHA, (length - i) * 0.1f + 0.6f, 1f));

      animators[i] = set;
      animators[i].setStartDelay(15 * i);
    }

    AnimatorSet set = new AnimatorSet();
    set.playTogether(animators);
    set.setInterpolator(new FastOutSlowInInterpolator());
    set.setDuration(FAST_DURATION);
    set.start();
  }

  private final static Property<View, PathPoint> PATH_POINT =
      new Property<View, PathPoint>(PathPoint.class, "PATH_POINT") {
        PathPoint point;

        @Override public PathPoint get(View object) {
          return point;
        }

        @Override public void set(View object, PathPoint value) {
          point = value;

          object.setTranslationX(value.mX);
          object.setTranslationY(value.mY);
        }
      };

  public void setMaskLocation(PathPoint location) {
    activatorMask.setX(location.mX);
    activatorMask.setY(location.mY);
  }

  @OnClick(R.id.open_radial_transformation) void open2Example() {
    Intent intent = new Intent(this, RadialTransformationActivity.class);
    startActivity(intent);
  }
}

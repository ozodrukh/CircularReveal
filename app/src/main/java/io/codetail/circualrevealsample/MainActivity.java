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
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.codetail.animation.ViewAnimationUtils;

public class MainActivity extends AppCompatActivity {
  public final static int LONG_DURATION = 400;
  public final static int SHORT_DURATION = 200;

  @BindView(R.id.circlesLine) ViewGroup circlesLine;
  @BindView(R.id.cardsLine) ViewGroup cardsLine;
  @BindView(R.id.activator_mask) CardView activatorMask;

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
    //target.setEnabled(false);

    // Coordinates of circle initial point
    final ViewGroup parent = (ViewGroup) activatorMask.getParent();
    final Rect bounds = new Rect();
    final Rect maskBounds = new Rect();

    target.getDrawingRect(bounds);
    activatorMask.getDrawingRect(maskBounds);
    parent.offsetDescendantRectToMyCoords(target, bounds);
    parent.offsetDescendantRectToMyCoords(activatorMask, maskBounds);

    // Put Mask view at circle initial points
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

    final int c0X = bounds.left - maskBounds.centerX(), c0Y = bounds.top - maskBounds.centerY();

    AnimatorPath path = new AnimatorPath();
    path.moveTo(c0X, c0Y);
    path.curveTo(c0X, c0Y, 0, c0Y, 0, 0);

    ObjectAnimator pathAnimator = ObjectAnimator.ofObject(this, "maskLocation", new PathEvaluator(),
        path.getPoints().toArray());

    AnimatorSet set = new AnimatorSet();
    set.playTogether(circularReveal, pathAnimator);
    set.setInterpolator(new FastOutSlowInInterpolator());
    set.setDuration(LONG_DURATION);
    set.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        executeCardsSequentialAnimation();
      }
    });
    set.start();
  }

  private void executeCardsSequentialAnimation() {
    cardsLine.setVisibility(View.VISIBLE);

    final Animator[] animators = new Animator[cardsLine.getChildCount()];
    for (int i = 0; i < cardsLine.getChildCount(); i++) {
      View card = cardsLine.getChildAt(i);
      card.setAlpha(0f);

      animators[i] = ObjectAnimator.ofFloat(card, View.ALPHA, 0f, 1f);
    }

    final AnimatorSet sequential = new AnimatorSet();
    sequential.playSequentially(animators);
    sequential.setInterpolator(new FastOutSlowInInterpolator());
    sequential.setDuration(SHORT_DURATION);
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

    final int cX = maskBounds.centerX();
    final int cY = maskBounds.centerY();

    Animator circularReveal = ViewAnimationUtils.createCircularReveal(activatorMask, cX, cY,
        (float) Math.hypot(maskBounds.width() * .5f, maskBounds.height() * .5f),
        target.getWidth() / 2, View.LAYER_TYPE_HARDWARE);

    final int c0X = bounds.left - maskBounds.centerX(), c0Y = bounds.top - maskBounds.centerY();

    AnimatorPath path = new AnimatorPath();
    path.moveTo(0, 0);
    path.curveTo(0, 0, 0, c0Y, c0X, c0Y);

    ObjectAnimator pathAnimator = ObjectAnimator.ofObject(this, "maskLocation", new PathEvaluator(),
        path.getPoints().toArray());

    AnimatorSet set = new AnimatorSet();
    set.playTogether(circularReveal, pathAnimator);
    set.setInterpolator(new FastOutSlowInInterpolator());
    set.setDuration(LONG_DURATION);
    set.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        activatorMask.setVisibility(View.INVISIBLE);

        circlesLine.setVisibility(View.VISIBLE);
        target.setEnabled(true);
      }
    });
    set.start();
  }

  public void setMaskLocation(PathPoint location) {
    activatorMask.setX(location.mX);
    activatorMask.setY(location.mY);
  }

  @OnClick(R.id.open_radial_transformation) void open2Example() {
    Intent intent = new Intent(this, RadialTransformationActivity.class);
    startActivity(intent);
  }
}

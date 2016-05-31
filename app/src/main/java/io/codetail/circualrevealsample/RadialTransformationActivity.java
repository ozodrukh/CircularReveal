package io.codetail.circualrevealsample;

import android.animation.Animator;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.codetail.animation.ViewAnimationUtils;

@SuppressWarnings("ConstantConditions") public class RadialTransformationActivity
    extends AppCompatActivity {

  @BindView(R.id.view_stack) ViewGroup stack;

  private int currentViewIndex = 0;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sample_2);
    ButterKnife.bind(this);

    final GestureDetector detector = new GestureDetector(this, tapDetector);

    for (int i = 0; i < stack.getChildCount(); i++) {
      View view = stack.getChildAt(i);
      view.setOnTouchListener(new View.OnTouchListener() {
        @Override public boolean onTouch(View v, MotionEvent event) {
          return detector.onTouchEvent(event);
        }
      });
    }
  }

  private GestureDetector.OnGestureListener tapDetector =
      new GestureDetector.SimpleOnGestureListener() {
        @Override public boolean onDown(MotionEvent e) {
          return true;
        }

        @Override public boolean onSingleTapUp(MotionEvent e) {
          View nextView = getNext();
          nextView.bringToFront();
          nextView.setVisibility(View.VISIBLE);

          final float finalRadius =
              (float) Math.hypot(nextView.getWidth() / 2f, nextView.getHeight() / 2f) + hypo(
                  nextView, e);

          Animator revealAnimator =
              ViewAnimationUtils.createCircularReveal(nextView, (int) e.getX(), (int) e.getY(), 0,
                  finalRadius, View.LAYER_TYPE_HARDWARE);

          revealAnimator.setDuration(MainActivity.LONG_DURATION);
          revealAnimator.setInterpolator(new FastOutLinearInInterpolator());
          revealAnimator.start();

          return true;
        }
      };

  private float hypo(View view, MotionEvent event) {
    Point p1 = new Point((int) event.getX(), (int) event.getY());
    Point p2 = new Point(view.getWidth() / 2, view.getHeight() / 2);

    return (float) Math.sqrt(Math.pow(p1.y - p2.y, 2) + Math.pow(p1.x - p2.x, 2));
  }

  private View getCurrentView() {
    return stack.getChildAt(currentViewIndex);
  }

  private View getNext() {
    if (++currentViewIndex == stack.getChildCount()) {
      currentViewIndex = 0;
    }
    return stack.getChildAt(currentViewIndex);
  }
}

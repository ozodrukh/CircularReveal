package io.codetail.circualrevealsample;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import butterknife.ButterKnife;
import butterknife.InjectView;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.circualrevealsample.widget.ViewUtils;
import io.codetail.widget.RevealFrameLayout;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

  Toolbar toolbar;
  RecyclerView recyclerView;
  FloatingActionButton floatingActionButton;

  LinearLayoutManager layoutManager;
  RecycleAdapter adapter;

  private Animator animator;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    toolbar = (Toolbar) findViewById(R.id.toolbar);
    recyclerView = (RecyclerView) findViewById(R.id.cardsGroup);
    floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);

    setSupportActionBar(toolbar);

    layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    adapter = new RecycleAdapter();
    adapter.setHasStableIds(true);

    recyclerView.addOnScrollListener(new HideExtraOnScroll(toolbar));
    recyclerView.setHasFixedSize(true);
    recyclerView.setItemViewCacheSize(3);
    recyclerView.setClipToPadding(false);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(layoutManager);

    toolbar.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            ViewUtils.removeGlobalListeners(toolbar, this);

            final int outOfScreenY =
                ((ViewGroup.MarginLayoutParams) floatingActionButton.getLayoutParams()).bottomMargin
                    + floatingActionButton.getHeight();

            liftingFromBottom(floatingActionButton, 0, outOfScreenY, 500, 0);
          }
        });

    floatingActionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (animator != null && animator.isRunning()) {
          animator.cancel();
          animator = null;
          return;
        } else {
          final View myView = ((RevealFrameLayout) recyclerView.getChildAt(0)).getChildAt(0);

          // get the center for the clipping circle
          //int cx = (myView.getLeft() + myView.getRight()) / 2;
          //int cy = (myView.getTop() + myView.getBottom()) / 2;
          int cx = myView.getRight();
          int cy = myView.getBottom();

          // get the final radius for the clipping circle
          float finalRadius = hypo(myView.getWidth(), myView.getHeight());

          animator = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
        }

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(1500);
        animator.start();
      }
    });
  }

  static float hypo(int a, int b) {
    return (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
  }

  public static class RecycleAdapter extends RecyclerView.Adapter<CardHolder> {

    @Override public CardHolder onCreateViewHolder(ViewGroup group, int i) {
      LayoutInflater factory = LayoutInflater.from(group.getContext());
      return new CardHolder(factory.inflate(R.layout.card_item, group, false));
    }

    @Override public void onBindViewHolder(CardHolder cardHolder, int i) {
    }

    @Override public int getItemCount() {
      return 10;
    }
  }

  public static class CardHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.card) CardView mCard;

    RevealFrameLayout mReveal;

    public CardHolder(View itemView) {
      super(itemView);
      ButterKnife.inject(CardHolder.this, itemView);

      mReveal = (RevealFrameLayout) mCard.getParent();
    }
  }

  public static class HideExtraOnScrollHelper {
    public final static int UNKNOWN = -1;
    public final static int TOP = 0;
    public final static int BOTTOM = 1;

    int mDraggedAmount;
    int mOldDirection;
    int mDragDirection;

    final int mMinFlingDistance;

    public HideExtraOnScrollHelper(int minFlingDistance) {
      mOldDirection = mDragDirection = mDraggedAmount = UNKNOWN;

      mMinFlingDistance = minFlingDistance;
    }

    /**
     * Checks need to hide extra objects on scroll or not
     *
     * @param dy scrolled distance y
     * @return true if need to hide extra objects on screen
     */
    public boolean isObjectsShouldBeOutside(int dy) {
      boolean needHide = false;
      mDragDirection = dy > 0 ? BOTTOM : TOP;

      if (mDragDirection != mOldDirection) {
        mDraggedAmount = 0;
      }

      mDraggedAmount += dy;
      boolean shouldBeOutside = false;

      if (mDragDirection == TOP && Math.abs(mDraggedAmount) > mMinFlingDistance) {
        shouldBeOutside = false;
      } else if (mDragDirection == BOTTOM && mDraggedAmount > mMinFlingDistance) {
        shouldBeOutside = true;
      }

      if (mOldDirection != mDragDirection) {
        mOldDirection = mDragDirection;
      }

      return shouldBeOutside;
    }
  }

  public static class HideExtraOnScroll extends RecyclerView.OnScrollListener {

    final static Interpolator ACCELERATE = new AccelerateInterpolator();
    final static Interpolator DECELERATE = new DecelerateInterpolator();

    WeakReference<View> mTarget;
    HideExtraOnScrollHelper mScrollHelper;

    boolean isExtraObjectsOutside;

    public HideExtraOnScroll(View target) {
      int minimumFlingVelocity =
          ViewConfiguration.get(target.getContext()).getScaledMinimumFlingVelocity();

      mScrollHelper = new HideExtraOnScrollHelper(minimumFlingVelocity);
      mTarget = new WeakReference<View>(target);
    }

    @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
      super.onScrolled(recyclerView, dx, dy);

      final View target = mTarget.get();

      if (target == null) {
        return;
      }

      boolean isObjectsShouldBeOutside = mScrollHelper.isObjectsShouldBeOutside(dy);

      if (!isVisible(target) && !isObjectsShouldBeOutside) {
        show(target);
        isExtraObjectsOutside = false;
      } else if (isVisible(target) && isObjectsShouldBeOutside) {
        hide(target, -target.getHeight());
        isExtraObjectsOutside = true;
      }
    }

    public boolean isVisible(View target) {
      return !isExtraObjectsOutside;
    }

    public void hide(final View target, float distance) {
      ObjectAnimator animator =
          ObjectAnimator.ofFloat(target, "translationY", target.getTranslationY(), distance);
      animator.setInterpolator(DECELERATE);
      animator.start();
    }

    public void show(final View target) {
      ObjectAnimator animator =
          ObjectAnimator.ofFloat(target, "translationY", target.getTranslationY(), 0f);
      animator.setInterpolator(ACCELERATE);
      animator.start();
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent = null;

    switch (item.getItemId()) {
      case R.id.sample2:
        intent = new Intent(this, Sample2Activity.class);
        break;

      case R.id.sample3:
        intent = new Intent(this, Sample3Activity.class);
        break;

      case R.id.sample4:

        getSupportFragmentManager().beginTransaction()
            .add(android.R.id.content, new FragmentRevealExample(), "fragment:reveal")
            .addToBackStack("fragment:reveal")
            .commit();

        return true;
    }

    startActivity(intent);
    return intent != null;
  }

  /**
   * Lifting view
   *
   * @param view The animation target
   * @param baseRotation initial Rotation X in 3D space
   * @param fromY initial Y position of view
   * @param duration aniamtion duration
   * @param startDelay start delay before animation begin
   */
  public static void liftingFromBottom(View view, float baseRotation, float fromY, int duration,
      int startDelay) {
    view.setRotationX(baseRotation);
    view.setTranslationY(fromY);

    view.animate()
        .setInterpolator(new AccelerateDecelerateInterpolator())
        .setDuration(duration)
        .setStartDelay(startDelay)
        .rotationX(0)
        .translationY(0)
        .start();
  }
}

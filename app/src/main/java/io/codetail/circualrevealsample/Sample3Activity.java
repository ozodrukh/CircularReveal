package io.codetail.circualrevealsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateDecelerateInterpolator;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class Sample3Activity extends AppCompatActivity
        implements OnPreDrawListener{

    private CardViewPlus mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_3);

        mContentView = (CardViewPlus) findViewById(R.id.content);

        getViewTreeObserver().addOnPreDrawListener(this);
    }

    protected View getRootView(){
        return mContentView;
    }

    protected ViewTreeObserver getViewTreeObserver(){
        return getRootView().getViewTreeObserver();
    }

    protected void startRevealTransition(){
        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(getRootView(),
                getRootView().getRight(), getRootView().getBottom(), 0,
                Sample2Activity.hypo(getRootView().getHeight(), getRootView().getWidth()),
                View.LAYER_TYPE_SOFTWARE);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    @Override
    public boolean onPreDraw() {
        getViewTreeObserver().removeOnPreDrawListener(this);
        startRevealTransition();
        return true;
    }
}

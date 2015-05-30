package io.codetail.circualrevealsample;

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class Sample3Activity extends AppCompatActivity
        implements ViewTreeObserver.OnGlobalLayoutListener{

    private CardViewPlus mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_3);

        mContentView = (CardViewPlus) findViewById(R.id.content);

        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    protected View getRootView(){
        return mContentView;
    }

    protected ViewTreeObserver getViewTreeObserver(){
        return getRootView().getViewTreeObserver();
    }

    protected void startRevealTransition(){
        final Rect bounds = new Rect();
        getRootView().getHitRect(bounds);
        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(getRootView(),
                bounds.right, bounds.bottom, 0, Sample2Activity.hypo(bounds.height(), bounds.width()));
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    @Override
    public void onGlobalLayout() {
        if(Build.VERSION.SDK_INT >= 16) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }else{
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        startRevealTransition();
    }
}

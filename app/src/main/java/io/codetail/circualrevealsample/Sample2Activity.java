package io.codetail.circualrevealsample;

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.animation.SupportAnimator.SimpleAnimatorListener;

public class Sample2Activity extends AppCompatActivity
        implements ViewTreeObserver.OnGlobalLayoutListener{
    private static final float[] TEMP_HSL = new float[]{0, 0, 0};
    private static final Creation[] REVEAL_CREATORS = new Creation[]{
        new LeftTopSide(), new RightTopSide(), new RightBottomSide(), new LeftBottomSide()
    };

    private CardViewPlus mCard1;
    private CardViewPlus mCard2;

    private final Rect mCard1Bounds = new Rect();
    private final Rect mCard2Bounds = new Rect();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_2);

        mCard1 = (CardViewPlus) findViewById(R.id.card1);
        mCard2 = (CardViewPlus) findViewById(R.id.card2);

        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    ViewTreeObserver getViewTreeObserver(){
        return getWindow().getDecorView().getViewTreeObserver();
    }

    private SimpleAnimatorListener mCard1AnimatorListener = new SimpleAnimatorListener() {
        @Override
        public void onAnimationEnd() {
            mCard2.setCardBackgroundColor(randomColor());
            mCard2.bringToFront();

            SupportAnimator c2a =  next().create(mCard2, mCard2Bounds);
            c2a.addListener(mCard2AnimatorListener);
            c2a.setDuration(1000);
            c2a.setInterpolator(new AccelerateDecelerateInterpolator());
            c2a.start();
        }
    };

    private SimpleAnimatorListener mCard2AnimatorListener = new SimpleAnimatorListener() {
        @Override
        public void onAnimationEnd() {
            startComplexAnimationOnCard1();
        }
    };

    void startComplexAnimationOnCard1(){
        mCard1.bringToFront();
        mCard1.setCardBackgroundColor(randomColor());

        SupportAnimator c1a =  next().create(mCard1, mCard1Bounds);

        c1a.addListener(mCard1AnimatorListener);
        c1a.setDuration(1000);
        c1a.setInterpolator(new AccelerateDecelerateInterpolator());
        c1a.start();
    }

    @Override
    public void onGlobalLayout() {
        if(Build.VERSION.SDK_INT >= 16) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }else{
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        mCard1.getHitRect(mCard1Bounds);
        mCard2.getHitRect(mCard2Bounds);

        startComplexAnimationOnCard1();
    }

    public static int randomColor(){
        float[] hsl = TEMP_HSL;
        hsl[0] = (float) (Math.random() * 360);
        hsl[1] = (float) (40 + (Math.random() * 60));
        hsl[2] = (float) (40 + (Math.random() * 60));
        return ColorUtils.HSLToColor(hsl);
    }

    private int mCreationIndex;
    public Creation next(){
        if(mCreationIndex == REVEAL_CREATORS.length){
            mCreationIndex = 0;
        }
        final Creation creation = REVEAL_CREATORS[mCreationIndex];
        mCreationIndex += 1;
        return creation;
    }

    interface Creation{
        SupportAnimator create(View view, Rect bounds);
    }

    public static float hypo(float a, float b){
        return (float) Math.sqrt( Math.pow(a, 2) + Math.pow(b, 2) );
    }

    static class LeftTopSide implements Creation{
        @Override
        public SupportAnimator create(View view, Rect bounds) {
            return ViewAnimationUtils.createCircularReveal(view, bounds.left, bounds.top, 0,
                    hypo(bounds.width(), bounds.height()));
        }
    }

    static class RightTopSide implements Creation{
        @Override
        public SupportAnimator create(View view, Rect bounds) {
            return ViewAnimationUtils.createCircularReveal(view, bounds.right, bounds.top, 0,
                    hypo(bounds.width(), bounds.height()));
        }
    }

    static class RightBottomSide implements Creation{
        @Override
        public SupportAnimator create(View view, Rect bounds) {
            return ViewAnimationUtils.createCircularReveal(view, bounds.right, bounds.bottom, 0,
                    hypo(bounds.width(), bounds.height()));
        }
    }

    static class LeftBottomSide implements Creation{
        @Override
        public SupportAnimator create(View view, Rect bounds) {
            return ViewAnimationUtils.createCircularReveal(view, bounds.left, bounds.bottom, 0,
                    hypo(bounds.width(), bounds.height()));
        }
    }
}

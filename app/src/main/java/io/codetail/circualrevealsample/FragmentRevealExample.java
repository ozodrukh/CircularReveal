package io.codetail.circualrevealsample;

import android.animation.Animator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import io.codetail.animation.ViewAnimationUtils;
import io.codetail.widget.RevealFrameLayout;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class FragmentRevealExample extends Fragment {

    private Animator mRevealAnimator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final FrameLayout frameLayout = new RevealFrameLayout(getContext());

        final FrameLayout content = new FrameLayout(getContext());
        content.setBackgroundColor(Color.WHITE);
        content.setVisibility(View.INVISIBLE);

        frameLayout.addView(content, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        final ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.example_raw_image);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        lp.topMargin = 16;
        lp.leftMargin = 16;
        lp.rightMargin = 16;
        lp.bottomMargin = 16;

        content.addView(imageView, lp);

        content.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                content.getViewTreeObserver().removeOnPreDrawListener(this);
                content.setVisibility(View.VISIBLE);

                // actually you need to set visibility before stating animation in listener

                mRevealAnimator = ViewAnimationUtils.createCircularReveal(content, 0, 0, 0,
                        MainActivity.hypo(content.getWidth(), content.getHeight()));
                mRevealAnimator.setDuration(500);
                mRevealAnimator.setStartDelay(100);
                mRevealAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                mRevealAnimator.start();
                return true;
            }
        });

        return frameLayout;
    }

}

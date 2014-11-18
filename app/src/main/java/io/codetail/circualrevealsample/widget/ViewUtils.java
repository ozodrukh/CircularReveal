package io.codetail.circualrevealsample.widget;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewTreeObserver;

public class ViewUtils {

    /**
     * Sets background, independence of API Level
     *
     * @param view target
     * @param drawable background
     */
    public static void setBackground(View view, Drawable drawable){
        if(Build.VERSION.SDK_INT > 16) {
            view.setBackground(drawable);
        }else{
            view.setBackgroundDrawable(drawable);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T findView(@NonNull Fragment destination, @IdRes int res){
        return (T) destination.getView().findViewById(res);
    }

    @SuppressWarnings("unchecked")
    public static <T> T findView(@NonNull Activity destination, @IdRes int res){
        return (T) destination.findViewById(res);
    }

    @SuppressWarnings("unchecked")
    public static <T> T findView(@NonNull View destination, @IdRes int res){
        return (T) destination.findViewById(res);
    }

    public static void removeGlobalListeners(View target, ViewTreeObserver.OnGlobalLayoutListener listener){
        if(Build.VERSION.SDK_INT > 16) {
            target.getViewTreeObserver()
                    .removeOnGlobalLayoutListener(listener);
        }else{
            target.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        }
    }

    /**
     * Make view visible or invisible
     */
    public static void setVisibility(View target, boolean visible){
        target.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * @param target The target view
     * @param visible false if need to set {@link android.view.View#GONE} flag
     */
    public static void setVisibilityWithGoneFlag(View target, boolean visible){
        target.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Checks view is visible or not
     *
     * @param view to check
     * @return true if {@link android.view.View#getVisibility()}
     *      equals to {@link android.view.View#VISIBLE}
     */
    public static boolean isVisible(View view){
        return view.getVisibility() == View.VISIBLE;
    }

}

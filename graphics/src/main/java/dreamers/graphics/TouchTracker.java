package dreamers.graphics;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.lang.ref.WeakReference;

public class TouchTracker implements View.OnTouchListener{

    RippleDrawable mHotspotDrawable;

    PerformClick mPerformClick;
    CheckForTap mPendingCheckForTap;
    CheckForLongPress mPendingCheckForLongPress;
    UnsetPressedState mUnsetPressedState;

    boolean mHasPerformedLongPress;

    int mTouchSlop;
    boolean mPrePressed;
    boolean mInsideScrollContainer;

    public TouchTracker(RippleDrawable hotspot){
        mHotspotDrawable = hotspot;

        mTouchSlop = -1;
    }

    public void setInsideScrollContainer(boolean inside){
        mInsideScrollContainer = inside;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        if(v.isClickable() || v.isLongClickable()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    if (mPrePressed || v.isPressed()) {
                        // take focus if we don't have it already and we should in
                        // touch mode.
                        boolean focusTaken = false;
                        if (v.isFocusable() && v.isFocusableInTouchMode() && !v.isFocused()) {
                            focusTaken = v.requestFocus();
                        }

                        if (mPrePressed) {
                            // The button is being released before we actually
                            // showed it as pressed.  Make it show the pressed
                            // state now (before scheduling the click) to ensure
                            // the user sees it.
                            setPressed(v, true, x, y);
                        }

                        if (!mHasPerformedLongPress) {
                            // This is a tap, so remove the longpress check
                            removeLongPressCallback(v);

                            // Only perform take click actions if we were in the pressed state
                            if (!focusTaken) {
                                // Use a Runnable and post this rather than calling
                                // performClick directly. This lets other visual state
                                // of the view update before click actions start.
                                if (mPerformClick == null) {
                                    mPerformClick = new PerformClick(v);
                                }
                                if (!v.post(mPerformClick)) {
                                    v.performClick();
                                }
                            }
                        }

                        if (mUnsetPressedState == null) {
                            mUnsetPressedState = new UnsetPressedState(v);
                        }

                        if (mPrePressed) {
                            v.postDelayed(mUnsetPressedState,
                                    ViewConfiguration.getPressedStateDuration());
                        } else if (!v.post(mUnsetPressedState)) {
                            // If the post failed, unpress right now
                            mUnsetPressedState.run();
                        }

                        removeTapCallback(v);

                    }
                    break;

                case MotionEvent.ACTION_DOWN:
                    mHasPerformedLongPress = false;

                    if (mInsideScrollContainer) {

                        mPrePressed = true;
                        if (mPendingCheckForTap == null) {
                            mPendingCheckForTap = new CheckForTap(v);
                        }

                        mPendingCheckForTap.x = event.getX();
                        mPendingCheckForTap.y = event.getY();
                        v.postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());

                    } else {
                        setPressed(v, true, x, y);
                        checkForLongClick(v, 0);
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    mHotspotDrawable.setHotspot(x, y);

                    if (mTouchSlop == -1) {
                        mTouchSlop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
                    }

                    // Be lenient about moving outside of buttons
                    if (!pointInView(v, x, y, mTouchSlop)) {
                        // Outside button
                        removeTapCallback(v);
                        if (v.isPressed()) {
                            // Remove any future long press/tap checks
                            removeLongPressCallback(v);

                            v.setPressed(false);
                        }
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                    v.setPressed(false);
                    removeTapCallback(v);
                    removeLongPressCallback(v);
                    break;
            }
            return true;
        }

        return false;
    }

    private void checkForLongClick(View target, int delayOffset) {
        if (target.isLongClickable()) {
            mHasPerformedLongPress = false;

            if (mPendingCheckForLongPress == null) {
                mPendingCheckForLongPress = new CheckForLongPress(target);
            }
            target.postDelayed(mPendingCheckForLongPress,
                    ViewConfiguration.getLongPressTimeout() - delayOffset);
        }
    }

    void setPressed(View target, boolean pressed, float x, float y){
        target.setPressed(pressed);
        mHotspotDrawable.setHotspot(x, y);
    }

    /**
     * Utility method to determine whether the given point, in local coordinates,
     * is inside the view, where the area of the view is expanded by the slop factor.
     * This method is called while processing touch-move events to determine if the event
     * is still within the view.
     *
     * @hide
     */
    public boolean pointInView(View target, float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < ((target.getRight() - target.getLeft()) + slop) &&
                localY < ((target.getBottom() - target.getTop()) + slop);
    }


    void removeTapCallback(View target){
        if(mPendingCheckForTap != null){
            target.removeCallbacks(mPendingCheckForTap);
        }
    }

    void removeLongPressCallback(View target){
        if (mPendingCheckForLongPress != null) {
            target.removeCallbacks(mPendingCheckForLongPress);
        }
    }

    private final static class PerformClick implements Runnable {

        WeakReference<View> target;

        private PerformClick(View target) {
            this.target = new WeakReference<View>(target);
        }

        @Override
        public void run() {
            if(target.get() != null){
                target.get().performClick();
            }
        }
    }


    final class CheckForTap implements Runnable{

        View target;
        float x, y;

        CheckForTap(View target) {
            this.target = target;
        }

        @Override
        public void run() {
            mPrePressed = true;
            setPressed(target, true, x, y);
            checkForLongClick(target, ViewConfiguration.getTapTimeout());
        }
    }

    private final class CheckForLongPress implements Runnable {

        View target;

        private CheckForLongPress(View target) {
            this.target = target;
        }

        @Override
        public void run() {
            if (target.isPressed() && (target.getParent() != null)) {
                if (target.performLongClick()) {
                    mHasPerformedLongPress = true;
                }
            }
        }
    }

    private final class UnsetPressedState implements Runnable {

        View target;

        private UnsetPressedState(View target) {
            this.target = target;
        }

        @Override
        public void run() {
            target.setPressed(false);
        }
    }

}

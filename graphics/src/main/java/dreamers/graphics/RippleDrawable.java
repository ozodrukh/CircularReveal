package dreamers.graphics;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.Arrays;

public class RippleDrawable extends Drawable {
    private static final PorterDuffXfermode DST_IN = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
    private static final PorterDuffXfermode SRC_ATOP = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
    private static final PorterDuffXfermode SRC_OVER = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);

    /**
     * Constant for automatically determining the maximum ripple radius.
     *
     * @see #setMaxRadius(int)
     * @hide
     */
    public static final int RADIUS_AUTO = -1;

    /** The maximum number of ripples supported. */
    private static final int MAX_RIPPLES = 10;

    /** Current ripple effect bounds, used to constrain ripple effects. */
    private final Rect mHotspotBounds = new Rect();

    ColorStateList mColor = ColorStateList.valueOf(Color.MAGENTA);

    int mMaxRadius = RADIUS_AUTO;

    private Drawable mContent;

    /** The masking layer, e.g. the layer with id R.id.mask. */
    private Drawable mMask;

    /** The current background. May be actively animating or pending entry. */
    private RippleBackground mBackground;

    /** Whether we expect to draw a background when visible. */
    private boolean mBackgroundActive;

    /** The current ripple. May be actively animating or pending entry. */
    private Ripple mRipple;

    /** Whether we expect to draw a ripple when visible. */
    private boolean mRippleActive;

    // Hotspot coordinates that are awaiting activation.
    private float mPendingX;
    private float mPendingY;
    private boolean mHasPending;

    /**
     * Lazily-created array of actively animating ripples. Inactive ripples are
     * pruned during draw(). The locations of these will not change.
     */
    private Ripple[] mExitingRipples;
    private int mExitingRipplesCount = 0;

    /** Paint used to control appearance of ripples. */
    private Paint mRipplePaint;

    /** Paint used to control reveal layer masking. */
    private Paint mMaskingPaint;

    /** Target density of the display into which ripples are drawn. */
    private float mDensity = 1.0f;

    /** Whether bounds are being overridden. */
    private boolean mOverrideBounds;

    /**
     * Whether the next draw MUST draw something to canvas. Used to work around
     * a bug in hardware invalidation following a render thread-accelerated
     * animation.
     */
    private boolean mNeedsDraw;

    /**
     * Creates a new ripple drawable with the specified ripple color and
     * optional content and mask drawables.
     *
     * @param color The ripple color
     */
    public RippleDrawable(ColorStateList color) {
        setColor(color);
    }

    public RippleDrawable(ColorStateList color, Drawable content){
        this(color);

        mContent = content;
    }

    @Override
    public void jumpToCurrentState() {
        if(Build.VERSION.SDK_INT > 11) {
            super.jumpToCurrentState();
        }

        boolean needsDraw;

        if (mRipple != null) {
            mRipple.jump();
        }

        if (mBackground != null) {
            mBackground.jump();
        }

        needsDraw = cancelExitingRipples();

        mNeedsDraw = needsDraw;
        invalidateSelf();
    }


    private boolean cancelExitingRipples() {
        final int count = mExitingRipplesCount;
        final Ripple[] ripples = mExitingRipples;
        for (int i = 0; i < count; i++) {
            ripples[i].cancel();
        }

        if (ripples != null) {
            Arrays.fill(ripples, 0, count, null);
        }
        mExitingRipplesCount = 0;

        return false;
    }

    @Override
    public void setAlpha(int alpha) {

    }


    @Override
    public void setColorFilter(ColorFilter cf) {
        //TODO how to implement?
    }

    @Override
    public int getOpacity() {
        // Worst-case scenario.
        return PixelFormat.TRANSLUCENT;
    }


    @Override
    protected boolean onStateChange(int[] stateSet) {
        final boolean changed = super.onStateChange(stateSet);

        boolean enabled = false;
        boolean pressed = false;
        boolean focused = false;

        for (int state : stateSet) {
            if (state == android.R.attr.state_enabled) {
                enabled = true;
            }
            if (state == android.R.attr.state_focused) {
                focused = true;
            }
            if (state == android.R.attr.state_pressed) {
                pressed = true;
            }
        }

        setRippleActive(enabled && pressed);
        setBackgroundActive(focused || (enabled && pressed));

        return changed;
    }

    private void setRippleActive(boolean active) {
        if (mRippleActive != active) {
            mRippleActive = active;
            if (active) {
                tryRippleEnter();
            } else {
                tryRippleExit();
            }
        }
    }

    private void setBackgroundActive(boolean active) {
        if (mBackgroundActive != active) {
            mBackgroundActive = active;
            if (active) {
                tryBackgroundEnter();
            } else {
                tryBackgroundExit();
            }
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        if (!mOverrideBounds) {
            mHotspotBounds.set(bounds);
            onHotspotBoundsChanged();
        }

        invalidateSelf();
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        final boolean changed = super.setVisible(visible, restart);

        if (!visible) {
            clearHotspots();
        } else if (changed) {
            // If we just became visible, ensure the background and ripple
            // visibilities are consistent with their internal states.
            if (mRippleActive) {
                tryRippleEnter();
            }

            if (mBackgroundActive) {
                tryBackgroundEnter();
            }
        }
        return changed;
    }

    public void setHotspot(float x, float y) {
        if (mRipple == null || mBackground == null) {
            mPendingX = x;
            mPendingY = y;
            mHasPending = true;
        }

        if (mRipple != null) {
            mRipple.move(x, y);
        }
    }

    /**
     * Attempts to start an enter animation for the active hotspot. Fails if
     * there are too many animating ripples.
     */
    private void tryRippleEnter() {
        if (mExitingRipplesCount >= MAX_RIPPLES) {
            // This should never happen unless the user is tapping like a maniac
            // or there is a bug that's preventing ripples from being removed.
            return;
        }

        if (mRipple == null) {
            final float x;
            final float y;
            if (mHasPending) {
                mHasPending = false;
                x = mPendingX;
                y = mPendingY;
            } else {
                x = mHotspotBounds.exactCenterX();
                y = mHotspotBounds.exactCenterY();
            }
            mRipple = new Ripple(this, mHotspotBounds, x, y);
        }

        final int color = mColor.getColorForState(getState(), Color.TRANSPARENT);
        mRipple.setup(mMaxRadius, color, mDensity);
        mRipple.enter();
    }

    /**
     * Attempts to start an exit animation for the active hotspot. Fails if
     * there is no active hotspot.
     */
    private void tryRippleExit() {
        if (mRipple != null) {
            if (mExitingRipples == null) {
                mExitingRipples = new Ripple[MAX_RIPPLES];
            }
            mExitingRipples[mExitingRipplesCount++] = mRipple;
            mRipple.exit();
            mRipple = null;
        }
    }

    /**
     * Cancels and removes the active ripple, all exiting ripples, and the
     * background. Nothing will be drawn after this method is called.
     */
    private void clearHotspots() {
        boolean needsDraw = false;

        if (mRipple != null) {
            needsDraw = false;
            mRipple.cancel();
            mRipple = null;
        }

        if (mBackground != null) {
            needsDraw = mBackground.isHardwareAnimating();
            mBackground.cancel();
            mBackground = null;
        }

        needsDraw |= cancelExitingRipples();

        mNeedsDraw = needsDraw;
        invalidateSelf();
    }

    public void setHotspotBounds(int left, int top, int right, int bottom) {
        mOverrideBounds = true;
        mHotspotBounds.set(left, top, right, bottom);

        onHotspotBoundsChanged();
    }

    /** @hide */
    public void getHotspotBounds(Rect outRect) {
        outRect.set(mHotspotBounds);
    }

    /**
     * Notifies all the animating ripples that the hotspot bounds have changed.
     */
    private void onHotspotBoundsChanged() {
        final int count = mExitingRipplesCount;
        final Ripple[] ripples = mExitingRipples;
        for (int i = 0; i < count; i++) {
            ripples[i].onHotspotBoundsChanged();
        }

        if (mRipple != null) {
            mRipple.onHotspotBoundsChanged();
        }

        if (mBackground != null) {
            mBackground.onHotspotBoundsChanged();
        }
    }

    /**
     * Creates an active hotspot at the specified location.
     */
    private void tryBackgroundEnter() {
        if (mBackground == null) {
            mBackground = new RippleBackground(this, mHotspotBounds);
        }

        final int color = mColor.getColorForState(getState(), Color.TRANSPARENT);
        mBackground.setup(mMaxRadius, color, mDensity);
        mBackground.enter();
    }

    private void tryBackgroundExit() {
        if (mBackground != null) {
            // Don't null out the background, we need it to draw!
            mBackground.exit();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        final boolean hasMask = mMask != null;
        final boolean drawNonMaskContent = mContent != null;//TODO if contentDrawable is not null

        final boolean drawMask = hasMask && mMask.getOpacity() != PixelFormat.OPAQUE;
        final Rect bounds = getDirtyBounds();
        final int saveCount = canvas.save(Canvas.CLIP_SAVE_FLAG);
        canvas.clipRect(bounds);

        // If we have content, draw it into a layer first.
        if (drawNonMaskContent) {
            drawContentLayer(canvas, bounds, SRC_OVER);
        }

        // Next, try to draw the ripples (into a layer if necessary). If we need
        // to mask against the underlying content, set the xfermode to SRC_ATOP.
        final PorterDuffXfermode xfermode = (hasMask || !drawNonMaskContent) ? SRC_OVER : SRC_ATOP;

        // If we have a background and a non-opaque mask, draw the masking layer.
        final int backgroundLayer = drawBackgroundLayer(canvas, bounds, xfermode, drawMask);
        if (backgroundLayer >= 0) {
            if (drawMask) {
                drawMaskingLayer(canvas, bounds, DST_IN);
            }
            canvas.restoreToCount(backgroundLayer);
        }

        // If we have ripples and a non-opaque mask, draw the masking layer.
        final int rippleLayer = drawRippleLayer(canvas, bounds, xfermode);
        if (rippleLayer >= 0) {
            if (drawMask) {
                drawMaskingLayer(canvas, bounds, DST_IN);
            }
            canvas.restoreToCount(rippleLayer);
        }

        // If we failed to draw anything and we just canceled animations, at
        // least draw a color so that hardware invalidation works correctly.
        if (mNeedsDraw) {
            canvas.drawColor(Color.TRANSPARENT);

            // Request another draw so we can avoid adding a transparent layer
            // during the next display list refresh.
            invalidateSelf();
        }

        mNeedsDraw = false;

        canvas.restoreToCount(saveCount);
    }

    /**
     * Removes a ripple from the exiting ripple list.
     *
     * @param ripple the ripple to remove
     */
    void removeRipple(Ripple ripple) {
        // Ripple ripple ripple ripple. Ripple ripple.
        final Ripple[] ripples = mExitingRipples;
        final int count = mExitingRipplesCount;
        final int index = getRippleIndex(ripple);
        if (index >= 0) {
            System.arraycopy(ripples, index + 1, ripples, index, count - (index + 1));
            ripples[count - 1] = null;
            mExitingRipplesCount--;

            invalidateSelf();
        }
    }

    private int getRippleIndex(Ripple ripple) {
        final Ripple[] ripples = mExitingRipples;
        final int count = mExitingRipplesCount;
        for (int i = 0; i < count; i++) {
            if (ripples[i] == ripple) {
                return i;
            }
        }
        return -1;
    }

    private int drawContentLayer(Canvas canvas, Rect bounds, PorterDuffXfermode mode) {
        mContent.setBounds(bounds);
        mContent.draw(canvas);
        return -1;
    }

    private int drawBackgroundLayer(
            Canvas canvas, Rect bounds, PorterDuffXfermode mode, boolean drawMask) {
        int saveCount = -1;

        if (mBackground != null && mBackground.shouldDraw()) {
            // TODO: We can avoid saveLayer here if we push the xfermode into
            // the background's render thread animator at exit() time.
            if (drawMask || mode != SRC_OVER) {
                //saveCount = canvas.saveLayer(bounds.left, bounds.top, bounds.right,
                //                        bounds.bottom, getMaskingPaint(mode));
            }

            final float x = mHotspotBounds.exactCenterX();
            final float y = mHotspotBounds.exactCenterY();
            canvas.translate(x, y);
            mBackground.draw(canvas, getRipplePaint());
            canvas.translate(-x, -y);
        }

        return saveCount;
    }

    private int drawRippleLayer(Canvas canvas, Rect bounds, PorterDuffXfermode mode) {
        boolean drewRipples = false;
        int restoreToCount = -1;
        int restoreTranslate = -1;

        // Draw ripples and update the animating ripples array.
        final int count = mExitingRipplesCount;
        final Ripple[] ripples = mExitingRipples;
        for (int i = 0; i <= count; i++) {
            final Ripple ripple;
            if (i < count) {
                ripple = ripples[i];
            } else if (mRipple != null) {
                ripple = mRipple;
            } else {
                continue;
            }

            // If we're masking the ripple layer, make sure we have a layer
            // first. This will merge SRC_OVER (directly) onto the canvas.
            if (restoreToCount < 0) {
                final Paint maskingPaint = getMaskingPaint(mode);
                final int color = mColor.getColorForState(getState(), Color.TRANSPARENT);
                final int alpha = Color.alpha(color);
                maskingPaint.setAlpha(alpha / 2);


                // Translate the canvas to the current hotspot bounds.
                restoreTranslate = canvas.save();
                canvas.translate(mHotspotBounds.exactCenterX(), mHotspotBounds.exactCenterY());
            }

            drewRipples |= ripple.draw(canvas, getRipplePaint());
        }

        // Always restore the translation.
        if (restoreTranslate >= 0) {
            canvas.restoreToCount(restoreTranslate);
        }

        // If we created a layer with no content, merge it immediately.
        if (restoreToCount >= 0 && !drewRipples) {
            canvas.restoreToCount(restoreToCount);
            restoreToCount = -1;
        }

        return restoreToCount;
    }

    private int drawMaskingLayer(Canvas canvas, Rect bounds, PorterDuffXfermode mode) {
        // Ensure that DST_IN blends using the entire layer.
        canvas.drawColor(Color.TRANSPARENT);

        mMask.draw(canvas);

        return -1;
    }

    private Paint getRipplePaint() {
        if (mRipplePaint == null) {
            mRipplePaint = new Paint();
            mRipplePaint.setAntiAlias(true);
        }
        return mRipplePaint;
    }

    private Paint getMaskingPaint(PorterDuffXfermode xfermode) {
        if (mMaskingPaint == null) {
            mMaskingPaint = new Paint();
        }
        mMaskingPaint.setXfermode(xfermode);
        mMaskingPaint.setAlpha(0xFF);
        return mMaskingPaint;
    }


    /**
     * Set the density at which this drawable will be rendered.
     *
     * @param metrics The display metrics for this drawable.
     */
    private void setTargetDensity(DisplayMetrics metrics) {
        if (mDensity != metrics.density) {
            mDensity = metrics.density;
            invalidateSelf();
        }
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    public void setColor(ColorStateList color) {
        mColor = color;
        invalidateSelf();
    }

    @Override
    public Rect getDirtyBounds() {
        return getBounds();
    }


    /**
     * Sets the maximum ripple radius in pixels. The default value of
     * {@link #RADIUS_AUTO} defines the radius as the distance from the center
     * of the drawable bounds (or hotspot bounds, if specified) to a corner.
     *
     * @param maxRadius the maximum ripple radius in pixels or
     *            {@link #RADIUS_AUTO} to automatically determine the maximum
     *            radius based on the bounds
     * @see #getMaxRadius()
     * @see #setHotspotBounds(int, int, int, int)
     * @hide
     */
    public void setMaxRadius(int maxRadius) {
        if (maxRadius != RADIUS_AUTO && maxRadius < 0) {
            throw new IllegalArgumentException("maxRadius must be RADIUS_AUTO or >= 0");
        }

        mMaxRadius = maxRadius;
    }

    /**
     * @return the maximum ripple radius in pixels, or {@link #RADIUS_AUTO} if
     *         the radius is determined automatically
     * @see #setMaxRadius(int)
     * @hide
     */
    public int getMaxRadius() {
        return mMaxRadius;
    }

    /**
     * @deprecated
     */
    public static RippleDrawable createRipple(View target, int color){
        return For(target, color);
    }

    public static RippleDrawable For(View target, int color){
        return makeFor(target, ColorStateList.valueOf(color));
    }

    public static RippleDrawable makeFor(View target, ColorStateList colors){
        return makeFor(target, colors, false);
    }

    public static RippleDrawable makeFor(View target, ColorStateList colors, boolean parentIsScrollContainer){
        RippleDrawable drawable = new RippleDrawable(colors, target.getBackground());

        TouchTracker tracker = new TouchTracker(drawable);
        tracker.setInsideScrollContainer(parentIsScrollContainer);

        setBackground(target, drawable);
        target.setOnTouchListener(tracker);

        return drawable;
    }

    private static void setBackground(View target, Drawable drawable){
        if(Build.VERSION.SDK_INT > 16){
            target.setBackground(drawable);
        }else{
            target.setBackgroundDrawable(drawable);
        }
    }
}
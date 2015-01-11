package io.codetail.circualrevealsample.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import java.util.ArrayList;

import io.codetail.circualrevealsample.R;


// Port of FrameLayout for Android 2.3 Gingerbread
public class FrameLayoutCompat extends ViewGroup{

    static final int DEFAULT_CHILD_GRAVITY = Gravity.TOP | Gravity.START;

    boolean mMeasureAllChildren = false;

    private Drawable mForeground;
    private ColorStateList mForegroundTintList = null;
    private PorterDuff.Mode mForegroundTintMode = null;
    private boolean mHasForegroundTint = false;
    private boolean mHasForegroundTintMode = false;

    private int mForegroundPaddingLeft = 0;

    private int mForegroundPaddingTop = 0;

    private int mForegroundPaddingRight = 0;

    private int mForegroundPaddingBottom = 0;

    private final Rect mSelfBounds = new Rect();
    private final Rect mOverlayBounds = new Rect();

    private int mForegroundGravity = Gravity.FILL;

    protected boolean mForegroundInPadding = true;

    boolean mForegroundBoundsChanged = false;

    private final ArrayList<View> mMatchParentChildren = new ArrayList<View>(1);

    public FrameLayoutCompat(Context context) {
        super(context);
    }

    public FrameLayoutCompat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FrameLayoutCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FrameLayoutCompat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.FrameLayoutCompat, defStyleAttr, defStyleRes);

        mForegroundGravity = a.getInt(
                R.styleable.FrameLayoutCompat_foregroundGravity, mForegroundGravity);

        final Drawable d = a.getDrawable(R.styleable.FrameLayoutCompat_foreground);
        if (d != null) {
            setForeground(d);
        }

        if (a.getBoolean(R.styleable.FrameLayoutCompat_measureAllChildren, false)) {
            setMeasureAllChildren(true);
        }

        if (a.hasValue(R.styleable.FrameLayoutCompat_foregroundTintMode)) {
            mForegroundTintMode = DrawableHelper.parseTintMode(a.getInt(
                    R.styleable.FrameLayoutCompat_foregroundTintMode, -1), mForegroundTintMode);
            mHasForegroundTintMode = true;
        }

        if (a.hasValue(R.styleable.FrameLayoutCompat_foregroundTint)) {
            mForegroundTintList = a.getColorStateList(R.styleable.FrameLayoutCompat_foregroundTint);
            mHasForegroundTint = true;
        }

        mForegroundInPadding = a.getBoolean(R.styleable.FrameLayoutCompat_foregroundInsidePadding, true);

        a.recycle();

        applyForegroundTint();
    }


    /**
     * Describes how the foreground is positioned.
     *
     * @return foreground gravity.
     *
     * @see #setForegroundGravity(int)
     *
     * @attr ref android.R.styleable#FrameLayout_foregroundGravity
     */
    public int getForegroundGravity() {
        return mForegroundGravity;
    }

    /**
     * Describes how the foreground is positioned. Defaults to START and TOP.
     *
     * @param foregroundGravity See {@link android.view.Gravity}
     *
     * @see #getForegroundGravity()
     *
     * @attr ref android.R.styleable#FrameLayout_foregroundGravity
     */
    public void setForegroundGravity(int foregroundGravity) {
        if (mForegroundGravity != foregroundGravity) {
            if ((foregroundGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.START;
            }

            if ((foregroundGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.TOP;
            }

            mForegroundGravity = foregroundGravity;


            if (mForegroundGravity == Gravity.FILL && mForeground != null) {
                Rect padding = new Rect();
                if (mForeground.getPadding(padding)) {
                    mForegroundPaddingLeft = padding.left;
                    mForegroundPaddingTop = padding.top;
                    mForegroundPaddingRight = padding.right;
                    mForegroundPaddingBottom = padding.bottom;
                }
            } else {
                mForegroundPaddingLeft = 0;
                mForegroundPaddingTop = 0;
                mForegroundPaddingRight = 0;
                mForegroundPaddingBottom = 0;
            }

            requestLayout();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (mForeground != null) {
            mForeground.setVisible(visibility == VISIBLE, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || (who == mForeground);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mForeground != null) mForeground.jumpToCurrentState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mForeground != null && mForeground.isStateful()) {
            mForeground.setState(getDrawableState());
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);

        if (mForeground != null) {
            mForeground.setHotspot(x, y);
        }
    }

    /**
     * Returns a set of layout parameters with a width of
     * {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT},
     * and a height of {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT}.
     */
    @Override
    protected FrameLayout.LayoutParams generateDefaultLayoutParams() {
        return new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    /**
     * Supply a Drawable that is to be rendered on top of all of the child
     * views in the frame layout.  Any padding in the Drawable will be taken
     * into account by ensuring that the children are inset to be placed
     * inside of the padding area.
     *
     * @param d The Drawable to be drawn on top of the children.
     *
     * @attr ref android.R.styleable#FrameLayout_foreground
     */
    public void setForeground(Drawable d) {
        if (mForeground != d) {
            if (mForeground != null) {
                mForeground.setCallback(null);
                unscheduleDrawable(mForeground);
            }

            mForeground = d;
            mForegroundPaddingLeft = 0;
            mForegroundPaddingTop = 0;
            mForegroundPaddingRight = 0;
            mForegroundPaddingBottom = 0;

            if (d != null) {
                setWillNotDraw(false);
                d.setCallback(this);
                if (d.isStateful()) {
                    d.setState(getDrawableState());
                }
                applyForegroundTint();
                if (mForegroundGravity == Gravity.FILL) {
                    Rect padding = new Rect();
                    if (d.getPadding(padding)) {
                        mForegroundPaddingLeft = padding.left;
                        mForegroundPaddingTop = padding.top;
                        mForegroundPaddingRight = padding.right;
                        mForegroundPaddingBottom = padding.bottom;
                    }
                }
            }  else {
                setWillNotDraw(true);
            }
            requestLayout();
            invalidate();
        }
    }

    /**
     * Returns the drawable used as the foreground of this FrameLayout. The
     * foreground drawable, if non-null, is always drawn on top of the children.
     *
     * @return A Drawable or null if no foreground was set.
     */
    public Drawable getForeground() {
        return mForeground;
    }

    /**
     * Applies a tint to the foreground drawable. Does not modify the current
     * tint mode, which is {@link android.graphics.PorterDuff.Mode#SRC_IN} by default.
     * <p>
     * Subsequent calls to {@link #setForeground(android.graphics.drawable.Drawable)} will automatically
     * mutate the drawable and apply the specified tint and tint mode using
     * {@link android.graphics.drawable.Drawable#setTintList(android.content.res.ColorStateList)}.
     *
     * @param tint the tint to apply, may be {@code null} to clear tint
     *
     * @attr ref android.R.styleable#FrameLayout_foregroundTint
     * @see #getForegroundTintList()
     * @see android.graphics.drawable.Drawable#setTintList(android.content.res.ColorStateList)
     */
    public void setForegroundTintList(ColorStateList tint) {
        mForegroundTintList = tint;
        mHasForegroundTint = true;

        applyForegroundTint();
    }

    /**
     * @return the tint applied to the foreground drawable
     * @attr ref android.R.styleable#FrameLayout_foregroundTint
     * @see #setForegroundTintList(android.content.res.ColorStateList)
     */
    public ColorStateList getForegroundTintList() {
        return mForegroundTintList;
    }

    /**
     * Specifies the blending mode used to apply the tint specified by
     * {@link #setForegroundTintList(android.content.res.ColorStateList)}} to the foreground drawable.
     * The default mode is {@link android.graphics.PorterDuff.Mode#SRC_IN}.
     *
     * @param tintMode the blending mode used to apply the tint, may be
     *                 {@code null} to clear tint
     * @attr ref android.R.styleable#FrameLayout_foregroundTintMode
     * @see #getForegroundTintMode()
     * @see android.graphics.drawable.Drawable#setTintMode(android.graphics.PorterDuff.Mode)
     */
    public void setForegroundTintMode(PorterDuff.Mode tintMode) {
        mForegroundTintMode = tintMode;
        mHasForegroundTintMode = true;

        applyForegroundTint();
    }

    /**
     * @return the blending mode used to apply the tint to the foreground
     *         drawable
     * @attr ref android.R.styleable#FrameLayout_foregroundTintMode
     * @see #setForegroundTintMode(android.graphics.PorterDuff.Mode)
     */
    public PorterDuff.Mode getForegroundTintMode() {
        return mForegroundTintMode;
    }

    private void applyForegroundTint() {
        if (mForeground != null && (mHasForegroundTint || mHasForegroundTintMode)) {
            mForeground = mForeground.mutate();

            if (mHasForegroundTint) {
                DrawableCompat.setTint(mForeground, mForegroundTintList.
                        getColorForState(getDrawableState(), Color.TRANSPARENT));
            }

            if (mHasForegroundTintMode) {
                DrawableCompat.setTintMode(mForeground, mForegroundTintMode);
            }
        }
    }

    int getPaddingLeftWithForeground() {
        return mForegroundInPadding ? Math.max(getPaddingLeft(), mForegroundPaddingLeft) :
                getPaddingLeft() + mForegroundPaddingLeft;
    }

    int getPaddingRightWithForeground() {
        return mForegroundInPadding ? Math.max(getPaddingRight(), mForegroundPaddingRight) :
                getPaddingRight() + mForegroundPaddingRight;
    }

    private int getPaddingTopWithForeground() {
        return mForegroundInPadding ? Math.max(getPaddingTop(), mForegroundPaddingTop) :
                getPaddingTop() + mForegroundPaddingTop;
    }

    private int getPaddingBottomWithForeground() {
        return mForegroundInPadding ? Math.max(getPaddingBottom(), mForegroundPaddingBottom) :
                getPaddingBottom() + mForegroundPaddingBottom;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                        MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        mMatchParentChildren.clear();

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (mMeasureAllChildren || child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth,
                        child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);

                //combine
                childState = childState | ViewCompat.getMeasuredState(child);

                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT ||
                            lp.height == LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child);
                    }
                }
            }
        }

        // Account for padding too
        maxWidth += getPaddingLeftWithForeground() + getPaddingRightWithForeground();
        maxHeight += getPaddingTopWithForeground() + getPaddingBottomWithForeground();

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        // Check against our foreground's minimum height and width
        final Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
            maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
        }

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec, childState << 16));

        count = mMatchParentChildren.size();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = mMatchParentChildren.get(i);

                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int childWidthMeasureSpec;
                int childHeightMeasureSpec;

                if (lp.width == LayoutParams.MATCH_PARENT) {
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth() -
                                    getPaddingLeftWithForeground() - getPaddingRightWithForeground() -
                                    lp.leftMargin - lp.rightMargin,
                            MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeftWithForeground() + getPaddingRightWithForeground() +
                                    lp.leftMargin + lp.rightMargin,
                            lp.width);
                }

                if (lp.height == LayoutParams.MATCH_PARENT) {
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() -
                                    getPaddingTopWithForeground() - getPaddingBottomWithForeground() -
                                    lp.topMargin - lp.bottomMargin,
                            MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTopWithForeground() + getPaddingBottomWithForeground() +
                                    lp.topMargin + lp.bottomMargin,
                            lp.height);
                }

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    /**
     * Merge two states as returned by {@link #getMeasuredState()}.
     * @param curState The current state as returned from a view or the result
     * of combining multiple views.
     * @param newState The new view state to combine.
     * @return Returns a new integer reflecting the combination of the two
     * states.
     */
    public static int combineMeasuredStates(int curState, int newState) {
        return curState | newState;
    }

    /**
     * Utility to reconcile a desired size and state, with constraints imposed
     * by a MeasureSpec.  Will take the desired size, unless a different size
     * is imposed by the constraints.  The returned value is a compound integer,
     * with the resolved size in the {@link #MEASURED_SIZE_MASK} bits and
     * optionally the bit {@link #MEASURED_STATE_TOO_SMALL} set if the resulting
     * size is smaller than the size the view wants to be.
     *
     * @param size How big the view wants to be
     * @param measureSpec Constraints imposed by the parent
     * @return Size information bit mask as defined by
     * {@link #MEASURED_SIZE_MASK} and {@link #MEASURED_STATE_TOO_SMALL}.
     */
    public static int resolveSizeAndState(int size, int measureSpec, int childMeasuredState) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                if (specSize < size) {
                    result = specSize | 0x01000000;
                } else {
                    result = size;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result | (childMeasuredState&0xff000000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren(left, top, right, bottom, false /* no force left gravity */);
    }

    @SuppressLint("RtlHardcoded")
    void layoutChildren(int left, int top, int right, int bottom,
                        boolean forceLeftGravity) {
        final int count = getChildCount();

        final int parentLeft = getPaddingLeftWithForeground();
        final int parentRight = right - left - getPaddingRightWithForeground();

        final int parentTop = getPaddingTopWithForeground();
        final int parentBottom = bottom - top - getPaddingBottomWithForeground();

        mForegroundBoundsChanged = true;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY;
                }

                final int absoluteGravity;

                final int layoutDirection = ViewCompat.getLayoutDirection(this);
                absoluteGravity = GravityCompat.getAbsoluteGravity(gravity, layoutDirection);

                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                                lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        if (!forceLeftGravity) {
                            childLeft = parentRight - width - lp.rightMargin;
                            break;
                        }
                    case Gravity.LEFT:
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                }

                switch (verticalGravity) {
                    case Gravity.TOP:
                        childTop = parentTop + lp.topMargin;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                                lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = parentBottom - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                }

                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mForegroundBoundsChanged = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        if (mForeground != null) {
            final Drawable foreground = mForeground;

            if (mForegroundBoundsChanged) {
                mForegroundBoundsChanged = false;
                final Rect selfBounds = mSelfBounds;
                final Rect overlayBounds = mOverlayBounds;

                final int w = getRight() - getLeft();
                final int h = getBottom() - getTop();

                if (mForegroundInPadding) {
                    selfBounds.set(0, 0, w, h);
                } else {
                    selfBounds.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
                }

                final int layoutDirection = ViewCompat.getLayoutDirection(this);
                GravityCompat.apply(mForegroundGravity, foreground.getIntrinsicWidth(),
                        foreground.getIntrinsicHeight(), selfBounds, overlayBounds,
                        layoutDirection);


                foreground.setBounds(overlayBounds);
            }

            foreground.draw(canvas);
        }
    }


    /**
     * Sets whether to consider all children, or just those in
     * the VISIBLE or INVISIBLE state, when measuring. Defaults to false.
     *
     * @param measureAll true to consider children marked GONE, false otherwise.
     * Default value is false.
     *
     * @attr ref android.R.styleable#FrameLayout_measureAllChildren
     */
    public void setMeasureAllChildren(boolean measureAll) {
        mMeasureAllChildren = measureAll;
    }

    /**
     * Determines whether all children, or just those in the VISIBLE or
     * INVISIBLE state, are considered when measuring.
     *
     * @return Whether all children are considered when measuring.
     *
     * @deprecated This method is deprecated in favor of
     * {@link #getMeasureAllChildren() getMeasureAllChildren()}, which was
     * renamed for consistency with
     * {@link #setMeasureAllChildren(boolean) setMeasureAllChildren()}.
     */
    @Deprecated
    public boolean getConsiderGoneChildrenWhenMeasuring() {
        return getMeasureAllChildren();
    }

    /**
     * Determines whether all children, or just those in the VISIBLE or
     * INVISIBLE state, are considered when measuring.
     *
     * @return Whether all children are considered when measuring.
     */
    public boolean getMeasureAllChildren() {
        return mMeasureAllChildren;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FrameLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p != null && p instanceof FrameLayout.LayoutParams;
    }

    @Override
    protected FrameLayout.LayoutParams generateLayoutParams(LayoutParams p) {
        return new FrameLayout.LayoutParams(p);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(FrameLayout.class.getName());
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(FrameLayout.class.getName());
    }

}

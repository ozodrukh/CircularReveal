package io.codetail.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import io.codetail.animation.RevealViewGroup;
import io.codetail.animation.ViewRevealManager;

public class RevealFrameLayout extends FrameLayout implements RevealViewGroup {
  private ViewRevealManager manager;

  public RevealFrameLayout(Context context) {
    this(context, null);
  }

  public RevealFrameLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RevealFrameLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    // https://developer.android.com/guide/topics/graphics/hardware-accel.html#unsupported
    // in theory the first part of this if could be omitted, but I'll leave it there to make the version range clear
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    manager = new ViewRevealManager();
  }

  @Override protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
    try {
      canvas.save();
      return manager.transform(canvas, child)
          & super.drawChild(canvas, child, drawingTime);
    } finally {
      canvas.restore();
    }
  }

  public void setViewRevealManager(ViewRevealManager manager) {
    if (manager == null) {
      throw new NullPointerException("ViewRevealManager is null");
    }

    this.manager = manager;
  }

  @Override public ViewRevealManager getViewRevealManager() {
    return manager;
  }
}

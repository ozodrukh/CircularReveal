package io.codetail.circualrevealsample;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

public class CardViewPlus extends CardView {

    private int mColor;

    public CardViewPlus(Context context) {
        this(context, null);
    }

    public CardViewPlus(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardViewPlus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setCardBackgroundColor(int color) {
        super.setCardBackgroundColor(color);
        mColor = color;
    }

    public int getCardBackgroundColor() {
        return mColor;
    }
}

package io.codetail.circualrevealsample;

import android.content.Context;
import android.support.animation.SpringForce;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import io.codetail.animation.RevealViewGroup;
import io.codetail.animation.SpringViewAnimatorManager;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * created at 3/16/17
 *
 * @author Ozodrukh
 * @version 1.0
 */
public class SpringSettingsBottomDialog extends LinearLayout {
  private boolean springManagerAdded = false;
  private boolean switchAdded = false;

  private SeekBar stiffnessView;
  private SeekBar dampingView;

  public SpringSettingsBottomDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
    final int padding = dp(16);

    setOrientation(VERTICAL);
    setPadding(padding, padding, padding, padding);
  }

  public void setAnimatorManager(final SpringViewAnimatorManager animatorManager) {
    if (springManagerAdded) {
      // already inflated progress bars
      return;
    }

    springManagerAdded = true;

    final int stiffnessVal =
        (int) ((animatorManager.getStiffness() / SpringForce.STIFFNESS_HIGH) * 100f);

    final int dampingVal =
        (int) (animatorManager.getDampingRatio() / SpringForce.DAMPING_RATIO_NO_BOUNCY * 100f);

    stiffnessView =
        createConfigurationView("Stiffness", stiffnessVal, new OnProgressChangeListener() {
          @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            animatorManager.setStiffness(3000 * (progress / 100f));
          }
        });

    dampingView =
        createConfigurationView("Damping Ratio", dampingVal, new OnProgressChangeListener() {
          @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            animatorManager.setDampingRatio(progress / 100f);
          }
        });
  }

  public void addSwitch(String label, boolean defaultState,
      final CompoundButton.OnCheckedChangeListener listener) {
    if (switchAdded) {
      return;
    }

    switchAdded = true;

    final SwitchCompat switchView = new SwitchCompat(getContext());
    switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        listener.onCheckedChanged(buttonView, isChecked);

        if (springManagerAdded) {
          stiffnessView.setEnabled(isChecked);
          dampingView.setEnabled(isChecked);
        }
      }
    });
    switchView.setChecked(defaultState);
    switchView.setText(label);

    addView(switchView, createMarginLayoutParams(MATCH_PARENT, WRAP_CONTENT, 0, 0, 0, dp(16)));

    if (springManagerAdded) {
      stiffnessView.setEnabled(defaultState);
      dampingView.setEnabled(defaultState);
    }
  }

  private SeekBar createConfigurationView(CharSequence label, int defaultVal,
      SeekBar.OnSeekBarChangeListener changeListener) {

    final TextView labelView =
        new AppCompatTextView(getContext(), null, R.style.TextAppearance_AppCompat_Caption);
    labelView.setText(label);
    labelView.setLayoutParams(createMarginLayoutParams(MATCH_PARENT, WRAP_CONTENT, 0, 0, 0, dp(8)));

    final SeekBar seekBar = new SeekBar(getContext());
    seekBar.setProgress(defaultVal);
    seekBar.setMax(100);
    seekBar.setOnSeekBarChangeListener(changeListener);

    seekBar.setLayoutParams(createMarginLayoutParams(MATCH_PARENT, WRAP_CONTENT, 0, 0, 0, dp(16)));

    addView(labelView);
    addView(seekBar);
    return seekBar;
  }

  private int dp(float px) {
    return (int) (getContext().getResources().getDisplayMetrics().density * px);
  }

  private static MarginLayoutParams createMarginLayoutParams(int w, int h, int l, int t, int r,
      int b) {
    MarginLayoutParams lp = new MarginLayoutParams(w, h);
    lp.leftMargin = l;
    lp.topMargin = t;
    lp.rightMargin = r;
    lp.bottomMargin = b;
    return lp;
  }

  private static abstract class OnProgressChangeListener
      implements SeekBar.OnSeekBarChangeListener {

    @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override public void onStopTrackingTouch(SeekBar seekBar) {

    }
  }
}

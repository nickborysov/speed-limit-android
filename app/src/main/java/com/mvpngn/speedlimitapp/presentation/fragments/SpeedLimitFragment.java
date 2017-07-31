package com.mvpngn.speedlimitapp.presentation.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mvpngn.speedlimitapp.R;

import java.util.Locale;

import static com.mvpngn.speedlimitapp.SpeedLimitApp.DEFAULT_RADIUS;

abstract public class SpeedLimitFragment extends Fragment {

    protected TextView mSpeedValueTextView;
    protected TextView mSpeedDimensionTextView;
    protected EditText mOsmRadiusEditText;
    protected Double mLatitude;
    protected Double mLongitude;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected TextView mWayIdTextView;
    protected TextView mUpdateTimeTextView;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        mOsmRadiusEditText.setText(String.format(Locale.US, "%.0f", DEFAULT_RADIUS));
        hideKeyboard();
    }

    protected abstract void setupViews(View view);

    public void hideKeyboard() {
        if (getView() != null) {
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    public boolean setSpeedValue(@Nullable String value) {
        if (value == null) {
            setSpeedValueNotDetected();
            return false;
        }
        mSpeedValueTextView.setText(value);
        mSpeedDimensionTextView.setVisibility(View.VISIBLE);
        return true;
    }

    public void setSpeedValueNotDetected() {
        String text = getString(R.string.not_detected_text);
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new RelativeSizeSpan(0.25f), 0, text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(),
                R.color.not_detected_text_color)), 0, text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mSpeedValueTextView.setText(spannableString);
        mSpeedDimensionTextView.setVisibility(View.INVISIBLE);
    }

    public void setSpeedValueGpsActivation() {
        String text = getString(R.string.gps_activation_text);
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new RelativeSizeSpan(0.25f), 0, text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(),
                R.color.gps_activation_text_color)), 0, text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mSpeedValueTextView.setText(spannableString);
        mSpeedDimensionTextView.setVisibility(View.INVISIBLE);
    }

    protected void setSpeedValueWithInfo(
            @Nullable String value,
            @Nullable String nodeId) {
        if (setSpeedValue(value)) {
            mWayIdTextView.setText(nodeId);
        }
    }

    protected void setSpeedValueWithInfo(
            @Nullable String value,
            @Nullable String nodeId,
            @Nullable String updateTime) {
        if (setSpeedValue(value)) {
            mWayIdTextView.setText(nodeId);
            mUpdateTimeTextView.setText(updateTime);
        }
    }


    public float getOsmRadius() {
        float radius = DEFAULT_RADIUS;
        try {
            radius = Float.parseFloat(mOsmRadiusEditText.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), R.string.wrong_input_radius, Toast.LENGTH_SHORT).show();
        }
        return radius;
    }

    public void setLatLng(Double latitude, Double longitude) {
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        mLatitudeTextView.setText(String.format(Locale.US, "%.14f", mLatitude));
        mLongitudeTextView.setText(String.format(Locale.US, "%.14f", mLongitude));
    }

    public double getLat() {
        return mLatitude != null ? mLatitude : 0d;
    }

    public double getLon() {
        return mLongitude != null ? mLongitude : 0d;
    }
}

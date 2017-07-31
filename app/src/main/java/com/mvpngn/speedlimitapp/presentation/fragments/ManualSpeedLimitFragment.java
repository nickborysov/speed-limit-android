package com.mvpngn.speedlimitapp.presentation.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mvpngn.speedlimitapp.R;

public class ManualSpeedLimitFragment extends SpeedLimitFragment {

    public static final String TAG = "ManualSpeedLimitFragment";

    private View.OnClickListener mOnCheckButtonClickListener;

    public ManualSpeedLimitFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manual_speed_limit, container, false);
    }

    @Override
    protected void setupViews(View view) {
        mSpeedValueTextView = view.findViewById(R.id.manual_speed_value_text_view);
        mSpeedDimensionTextView = view.findViewById(R.id.manual_speed_dimension_text_view);
        mLatitudeTextView = view.findViewById(R.id.manual_speed_lat_edit_text_view);
        mLongitudeTextView = view.findViewById(R.id.manual_speed_lon_edit_text_view);
        mOsmRadiusEditText = view.findViewById(R.id.manual_speed_osm_radius_edit_text);
        mWayIdTextView = view.findViewById(R.id.manual_speed_way_id_text_view);
        mWayNameTextView = view.findViewById(R.id.manual_speed_way_name_text_view);
        Button checkButton = view.findViewById(R.id.manual_check_button);

        if (mOnCheckButtonClickListener != null) {
            checkButton.setOnClickListener(mOnCheckButtonClickListener);
        }

        checkButton.requestFocus();
        setSpeedValueNotDetected();

        setLatLng(49.9896234d, 36.250744d);
    }

    public void setOnCheckButtonClickListener(View.OnClickListener onCheckButtonClickListener) {
        this.mOnCheckButtonClickListener = onCheckButtonClickListener;
    }

    @Override
    public double getLat() {
        try {
            mLatitude = Double.parseDouble(mLatitudeTextView.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), R.string.wrong_input_lat, Toast.LENGTH_SHORT).show();
        }
        return super.getLat();
    }

    @Override
    public double getLon() {
        try {
            mLongitude = Double.parseDouble(mLongitudeTextView.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), R.string.wrong_input_lon, Toast.LENGTH_SHORT).show();
        }
        return super.getLon();
    }

    @Override
    public void setSpeedValueWithInfo(@Nullable String value, @Nullable String wayId, @Nullable String wayName) {
        super.setSpeedValueWithInfo(value, wayId, wayName);
    }
}

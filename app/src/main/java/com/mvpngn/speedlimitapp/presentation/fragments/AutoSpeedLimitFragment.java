package com.mvpngn.speedlimitapp.presentation.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mvpngn.speedlimitapp.R;

public class AutoSpeedLimitFragment extends SpeedLimitFragment {

    public static final String TAG = "AutoSpeedLimitFragment";

    private Button mUpdateRadiusButton;

    private View.OnClickListener mOnUpdateRadiusListener;

    public AutoSpeedLimitFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auto_speed_limit, container, false);
    }

    @Override
    protected void setupViews(View view) {
        mSpeedValueTextView = view.findViewById(R.id.auto_speed_value_text_view);
        mSpeedDimensionTextView = view.findViewById(R.id.auto_speed_dimension_text_view);
        mLatitudeTextView = view.findViewById(R.id.auto_speed_lat_text_view);
        mLongitudeTextView = view.findViewById(R.id.auto_speed_lon_text_view);
        mOsmRadiusEditText = view.findViewById(R.id.auto_speed_osm_radius_edit_text);
        mUpdateRadiusButton = view.findViewById(R.id.auto_speed_update_radius);
        mWayIdTextView = view.findViewById(R.id.auto_speed_way_id_text_view);
        mWayNameTextView = view.findViewById(R.id.auto_speed_way_name_text_view);
        mUpdateTimeTextView = view.findViewById(R.id.auto_speed_updated_text_view);
        if (mOnUpdateRadiusListener != null) {
            mUpdateRadiusButton.setOnClickListener(mOnUpdateRadiusListener);
        }
        mUpdateRadiusButton.requestFocus();
        setSpeedValueGpsActivation();
    }

    public void setOnUpdateRadiusListener(View.OnClickListener listener) {
        mOnUpdateRadiusListener = listener;
    }

    @Override
    public void setSpeedValueWithInfo(@Nullable String value, @Nullable String wayId, @Nullable String wayName, @Nullable String updateTime) {
        super.setSpeedValueWithInfo(value, wayId, wayName, updateTime);
    }
}

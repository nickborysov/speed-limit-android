package com.mvpngn.speedlimitapp.presentation;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.mvpngn.speedlimitapp.R;
import com.mvpngn.speedlimitapp.SpeedLimitApp;
import com.mvpngn.speedlimitapp.data.OverpassDataSource;
import com.mvpngn.speedlimitapp.presentation.fragments.AutoSpeedLimitFragment;
import com.mvpngn.speedlimitapp.presentation.fragments.ManualSpeedLimitFragment;
import com.mvpngn.speedlimitapp.utils.SystemServicesHelper;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SystemServicesHelper mSystemServicesHelper;
    private AutoSpeedLimitFragment mAutoSpeedLimitFragment;
    private ManualSpeedLimitFragment mManualSpeedLimitFragment;
    private OverpassDataSource mOverpassDataSource;

    TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(SpeedLimitApp.APP_NAME, "Start Application");
        setContentView(R.layout.activity_main);
        mSystemServicesHelper = new SystemServicesHelper(this, mLocationListener);
        setupFragments();
        setupViews();

        mOverpassDataSource = new OverpassDataSource();
        mOverpassDataSource.setOnMaxSpeedDetectedListener(
                new OverpassDataSource.OnMaxSpeedDetectedListener() {
                    @Override
                    public void maxSpeedDetected(@Nullable String speed,
                                                 @Nullable String nodeId,
                                                 @Nullable String wayId,
                                                 @Nullable String wayName,
                                                 float distance) {
                        switch (mTabLayout.getSelectedTabPosition()) {
                            case 0:
                                mAutoSpeedLimitFragment.setSpeedValueWithInfo(
                                        speed,
                                        wayId,
                                        wayName,
                                        DateFormat.getTimeInstance()
                                                .format(Calendar.getInstance().getTime()));
                                break;
                            case 1:
                                mManualSpeedLimitFragment.setSpeedValueWithInfo(
                                        speed,
                                        wayId,
                                        wayName);
                                break;
                        }
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSystemServicesHelper.trySetLocationUpdateListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSystemServicesHelper.removeLocationUpdateListener();
    }

    private void setupViews() {
        setAutoSpeedLimitFragment();

        mTabLayout = (TabLayout) findViewById(R.id.main_navigation_tabs);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    default:
                    case 0:
                        setAutoSpeedLimitFragment();
                        break;
                    case 1:
                        setManualSpeedLimitFragment();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mTabLayout.getTabAt(0).select();
    }

    private void setupFragments() {
        mAutoSpeedLimitFragment = new AutoSpeedLimitFragment();
        mAutoSpeedLimitFragment.setOnUpdateRadiusListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAutoSpeedLimitFragment.hideKeyboard();
                        ((ViewGroup)view.getParent()).requestFocus();
                        mAutoSpeedLimitFragment.hideKeyboard();
                        mOverpassDataSource.setRadius(mAutoSpeedLimitFragment.getOsmRadius());
                    }
                }
        );
        mManualSpeedLimitFragment = new ManualSpeedLimitFragment();
        mManualSpeedLimitFragment.setOnCheckButtonClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mManualSpeedLimitFragment.hideKeyboard();
                        ((ViewGroup)view.getParent()).requestFocus();
                        mManualSpeedLimitFragment.clearInfo();
                        if (new SystemServicesHelper(MainActivity.this).checkNetwork()) {
                            mOverpassDataSource.setRadius(mManualSpeedLimitFragment.getOsmRadius());
                            mOverpassDataSource.searchNearestMaxSpeed(
                                    mManualSpeedLimitFragment.getLat(),
                                    mManualSpeedLimitFragment.getLon());
                        }
                    }
                }
        );
    }

    private void setAutoSpeedLimitFragment() {
        replaceFragment(mAutoSpeedLimitFragment, R.id.main_content, false, AutoSpeedLimitFragment.TAG);
        mSystemServicesHelper.trySetLocationUpdateListener();
    }

    private void setManualSpeedLimitFragment() {
        replaceFragment(mManualSpeedLimitFragment, R.id.main_content, false, ManualSpeedLimitFragment.TAG);
        mSystemServicesHelper.removeLocationUpdateListener();
    }

    private void replaceFragment(Fragment fragment, int containerId, boolean isAddToBackStack, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(containerId, fragment, tag);
        if (isAddToBackStack)
            fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mAutoSpeedLimitFragment != null &&
                    mAutoSpeedLimitFragment.getTag().equals(AutoSpeedLimitFragment.TAG)) {
                StringBuilder satellitesText = new StringBuilder();
                if (location.getExtras() != null) {
                    satellitesText.append(location.getExtras().getInt("satellites"));
                    if (location.hasAccuracy()) {
                        satellitesText.append(" | Accuracy: ")
                                .append(String.format(Locale.US, "%.2f", location.getAccuracy()))
                                .append(" m");
                    }
                    mAutoSpeedLimitFragment.setSatellites(satellitesText.toString());
                }
                Log.d(SpeedLimitApp.APP_NAME,
                        "Current location: " + location.getLatitude() + " " + location.getLongitude());
                mAutoSpeedLimitFragment.setLatLng(location.getLatitude(), location.getLongitude());
                mOverpassDataSource.setRadius(mAutoSpeedLimitFragment.getOsmRadius());
                mOverpassDataSource.searchNearestMaxSpeed(location.getLatitude(), location.getLongitude());
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
}

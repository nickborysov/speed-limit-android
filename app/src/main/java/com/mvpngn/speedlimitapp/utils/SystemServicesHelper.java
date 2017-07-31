package com.mvpngn.speedlimitapp.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.mvpngn.speedlimitapp.utils.kalman_filter.KalmanLocationManager;

import static com.mvpngn.speedlimitapp.SpeedLimitApp.DEFAULT_LOCATION_RADIUS;
import static com.mvpngn.speedlimitapp.SpeedLimitApp.DEFAULT_TIMEOUT;

public class SystemServicesHelper {

    private Context mContext;
    private OpenSettingsDialogs mOpenSettingsDialogs;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private boolean mIsListenerAdded;
    private KalmanLocationManager mKalmanLocationManager;

    public SystemServicesHelper(Context context) {
        this(context, null);
    }

    public SystemServicesHelper(Context context, @Nullable LocationListener listener) {
        this.mContext = context;
        this.mLocationListener = listener;
        this.mOpenSettingsDialogs = new OpenSettingsDialogs(mContext);
        this.mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        this.mKalmanLocationManager = new KalmanLocationManager(mContext);
        this.mIsListenerAdded = false;
    }

    public void removeLocationUpdateListener() {
        if (mIsListenerAdded) {
//            mLocationManager.removeUpdates(mLocationListener);
            mKalmanLocationManager.removeUpdates(mLocationListener);
            mIsListenerAdded = false;
        }
    }

    private void setLocationUpdateListener(long timeout, float minDistance) throws SecurityException {
//        mLocationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER,
//                timeout,
//                minDistance,
//                mLocationListener);
        mKalmanLocationManager.requestLocationUpdates(
                KalmanLocationManager.UseProvider.GPS,
                timeout,
                timeout,
                timeout,
                mLocationListener,
                true);
        mIsListenerAdded = true;
    }

    public boolean trySetLocationUpdateListener() {
        return trySetLocationUpdateListener(DEFAULT_TIMEOUT, DEFAULT_LOCATION_RADIUS);
    }

    public boolean trySetLocationUpdateListener(long timeout, float minDistance) {
        removeLocationUpdateListener();
        if (checkServicesEnabled() && mLocationListener != null) {
            setLocationUpdateListener(timeout, minDistance);
            return true;
        }
        return false;
    }

    public boolean checkServicesEnabled() {
        return checkLocationPermission() && checkNetwork() && checkLocationServices();
    }

    public boolean checkNetwork() {
        if (!isNetworkEnabled()) {
            mOpenSettingsDialogs.networkDisabledDialog().show();
            return false;
        }
        return true;
    }

    private boolean checkLocationServices() {
        if (!isLocationServicesEnabled()) {
            mOpenSettingsDialogs.gpsDisabledDialog().show();
            return false;
        }
        return true;
    }

    private boolean checkLocationPermission() {
        if (!isLocationPermissionGranted()) {
            mOpenSettingsDialogs.locationPermissionDialog().show();
            return false;
        }
        return true;
    }

    private boolean isLocationPermissionGranted() {
        return (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean isLocationServicesEnabled() {
        boolean isGpsEnabled = false;
        try {
            isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        return isGpsEnabled;
    }

    private boolean isNetworkEnabled() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}

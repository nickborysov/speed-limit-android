package com.mvpngn.speedlimitapp.data;

import android.support.annotation.Nullable;
import android.util.Log;

import com.mvpngn.speedlimitapp.SpeedLimitApp;
import com.mvpngn.speedlimitapp.utils.LatLngHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class OverpassDataSource {

    private List<OverpassQueryResult.Element> mNodes;
    private List<OverpassQueryResult.Element> mWays;

    private LatLngBounds mLatLngBounds;
    private float mRadius;

    private OnMaxSpeedDetectedListener mOnMaxSpeedDetectedListener;

    private double mLatitude;
    private double mLongitude;

    public OverpassDataSource() {
        setDefaultsLatLng();
        this.mNodes = new ArrayList<>();
        this.mWays = new ArrayList<>();
    }

    private void setDefaultsLatLng() {
        this.mLatitude = 0;
        this.mLongitude = 0;
        this.mRadius = 0;
        this.mLatLngBounds = LatLngHelper.getBoundsByPosition(mLatitude, mLongitude, mRadius);
    }

    public void setLatLngRadius(double latitude, double longitude, float radius) {
        if (this.mRadius != radius || this.mLatitude != latitude || this.mLongitude != longitude) {
            this.mLatitude = latitude;
            this.mLongitude = longitude;
            this.mRadius = radius;
            if (this.mRadius != 0 && this.mLatitude != 0 && this.mLongitude != 0) {
                reloadData();
            } else {
                this.mLatLngBounds = LatLngHelper.getBoundsByPosition(mLatitude, mLongitude, mRadius);
            }

        }
    }

    public void setRadius(float radius) {
        setLatLngRadius(mLatitude, mLongitude, radius);
    }

    public void setOnMaxSpeedDetectedListener(OnMaxSpeedDetectedListener onMaxSpeedDetectedListener) {
        this.mOnMaxSpeedDetectedListener = onMaxSpeedDetectedListener;
    }

    public void searchNearestMaxSpeed(double latitude, double longitude) {
        if (mLatLngBounds.contains(new LatLng(latitude, longitude))) {
            detectNearestMaxSpeed(latitude, longitude);
        } else {
            mLatitude = latitude;
            mLongitude = longitude;
            reloadData();
        }
    }

    private void detectNearestMaxSpeed(double latitude, double longitude) {
        OverpassQueryResult.Element nearestNode = null;
        float nearestElementDistance = Float.MAX_VALUE;
        for (OverpassQueryResult.Element element : mNodes) {
            float distance = LatLngHelper.getDistanceBetweenPoints(
                    latitude, longitude, element.lat, element.lon);
            if (nearestElementDistance > distance) {
                nearestElementDistance = distance;
                nearestNode = element;
            }
        }
        if (nearestNode != null) {
            OverpassQueryResult.Element nearestWay = detectWayById(nearestNode.id);
            if (nearestWay != null && nearestWay.tags != null && mOnMaxSpeedDetectedListener != null) {
                Log.i(SpeedLimitApp.APP_NAME,
                        "NodeId = " + nearestNode.id + "; WayId = " + nearestWay.id
                                + "; speedLimitValue = " + nearestWay.tags.maxspeed+
                                "; meters = " + nearestElementDistance);
                Integer value = getSpeedWithoutUnits(nearestWay.tags.maxspeed);
                mOnMaxSpeedDetectedListener.maxSpeedDetected(
                        value != null ? value.toString() : null,
                        nearestNode.id,
                        nearestWay.id,
                        nearestElementDistance);
            }
        }
    }

    private Integer getSpeedWithoutUnits(String speed) {
        Integer speedInt;
        try {
            speedInt = Integer.parseInt(speed.contains(" ")
                    ? speed.substring(0, speed.indexOf(' ')) : speed);
        } catch (Exception e) {
            return null;
        }
        return speedInt;
    }

    @Nullable
    private OverpassQueryResult.Element detectWayById(String id) {
        for (OverpassQueryResult.Element element : mWays) {
            for (String node : element.nodes) {
                if (node.compareTo(id) == 0 && hasMaxSpeed(element)) {
                    return element;
                }
            }
        }
        return null;
    }

    private boolean hasMaxSpeed(OverpassQueryResult.Element element) {
        return element != null && element.tags != null && element.tags.maxspeed != null;
    }

    private void reloadData() {
        mLatLngBounds = LatLngHelper.getBoundsByPosition(
                mLatitude, mLongitude, mRadius);
        OverpassServiceRequest request = new OverpassServiceRequest(mLatLngBounds);
        request.resumeWithCompletionHandler(new OverpassServiceRequest.CompletionHandler() {
            @Override
            public void completionHandler(OverpassQueryResult result) {
                if (result.elements != null && result.elements.size() > 0) {
                    mNodes.clear();
                    mWays.clear();
                    for (OverpassQueryResult.Element element : result.elements) {
                        switch (element.type) {
                            case "node":
                                mNodes.add(element);
                                break;
                            case "way":
                                mWays.add(element);
                                break;
                        }
                    }
                    Log.d(SpeedLimitApp.APP_NAME,
                            "Loaded " + result.elements.size() + " elements ("
                                    + mNodes.size() + "; " + mWays.size() + ")");
                    detectNearestMaxSpeed(mLatitude, mLongitude);
                } else {
                    completionHandlerWithError("Elements is empty");
                }
            }

            @Override
            public void completionHandlerWithError(String error) {
                Log.d(SpeedLimitApp.APP_NAME,
                        "Loaded with error: " + error);
                setDefaultsLatLng();
                if (mOnMaxSpeedDetectedListener != null) {
                    mOnMaxSpeedDetectedListener.maxSpeedDetected(null, null, null, 0);
                }
            }
        });
    }

    public interface OnMaxSpeedDetectedListener {
        void maxSpeedDetected(@Nullable String speed,
                              @Nullable String nodeId,
                              @Nullable String wayId,
                              float distance);
    }

}

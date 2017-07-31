package com.mvpngn.speedlimitapp.data;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mvpngn.speedlimitapp.SpeedLimitApp;
import com.mvpngn.speedlimitapp.utils.LatLngHelper;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.mvpngn.speedlimitapp.SpeedLimitApp.NODE_MAX_DISTANCE;

public class OverpassDataSource {

    private Map<String, OverpassQueryResult.Element> mNodes;
    private Map<String, OverpassQueryResult.Element> mWays;

    private LatLngBounds mLatLngBounds;
    private float mRadius;

    private OnMaxSpeedDetectedListener mOnMaxSpeedDetectedListener;

    private double mLatitude;
    private double mLongitude;

    public OverpassDataSource() {
        setDefaultsLatLng();
        this.mNodes = new LinkedHashMap<>();
        this.mWays = new LinkedHashMap<>();
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
            detectNearestMaxSpeedByTwoPoints(latitude, longitude);
        } else {
            mLatitude = latitude;
            mLongitude = longitude;
            reloadData();
        }
    }

    private void detectNearestMaxSpeed(double latitude, double longitude) {
        OverpassQueryResult.Element nearestNode = null;
        float nearestElementDistance = Float.MAX_VALUE;
        for (Map.Entry<String, OverpassQueryResult.Element> element : mNodes.entrySet()) {
            float distance = LatLngHelper.getDistanceBetweenPoints(
                    latitude, longitude, element.getValue().lat, element.getValue().lon);
            if (nearestElementDistance > distance) {
                nearestElementDistance = distance;
                nearestNode = element.getValue();
            }
        }
        if (nearestNode != null) {
            OverpassQueryResult.Element nearestWay = detectWayById(nearestNode.id);
            doWithNearestWay(nearestWay, nearestElementDistance);
        }
    }

    private void doWithNearestWay(OverpassQueryResult.Element way, Float distance) {
        if (way != null && way.tags != null && mOnMaxSpeedDetectedListener != null) {
            Log.i(SpeedLimitApp.APP_NAME,
                    "NodeId = " + way.id + "; WayId = " + way.id
                            + "; speedLimitValue = " + way.tags.maxspeed +
                            "; meters = " + distance);
            Integer value = getSpeedWithoutUnits(way.tags.maxspeed);

            mOnMaxSpeedDetectedListener.maxSpeedDetected(
                    value != null && distance < NODE_MAX_DISTANCE ? value.toString() : null,
                    way.id,
                    way.id,
                    way.tags.name,
                    distance);
        }
    }

    private void detectNearestMaxSpeedByTwoPoints(double latitude, double longitude) {
        AbstractMap.SimpleEntry<OverpassQueryResult.Element, Float> wayWithDistance =
                getNearestWay(latitude, longitude);
        if (wayWithDistance != null) {
            doWithNearestWay(wayWithDistance.getKey(), wayWithDistance.getValue());
        } else {
            doWithNearestWay(null, null);
        }
    }

    private AbstractMap.SimpleEntry<OverpassQueryResult.Element, Float> getNearestWay(
            double latitude,
            double longitude) {

        // Get SortedMap of distance to latlng with ways
        SortedMap<Float, OverpassQueryResult.Element> waySortedMap = new TreeMap<>();
        for (Map.Entry<String, OverpassQueryResult.Element> element : mNodes.entrySet()) {
            float distance = LatLngHelper.getDistanceBetweenPoints(
                    latitude, longitude, element.getValue().lat, element.getValue().lon);
            int i = 0;
            for (OverpassQueryResult.Element way : element.getValue().ways) {
                waySortedMap.put(distance + 0.000001f * i++, way);
            }
        }
        for (Float distanceOne : waySortedMap.keySet()) {
            for (Float distanceTwo : waySortedMap.keySet()) {
                if (waySortedMap.get(distanceOne).equals(waySortedMap.get(distanceTwo))) {
                    return new AbstractMap.SimpleEntry<>(waySortedMap.get(distanceOne), distanceOne);
                }
            }
        }
        return null;
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

    private OverpassQueryResult.Element detectWayById(String nodeId) {
        for (Map.Entry<String, OverpassQueryResult.Element> element : mWays.entrySet()) {
            for (String node : element.getValue().nodes) {
                if (node.compareTo(nodeId) == 0) {
                    return element.getValue();
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
                mNodes.clear();
                mWays.clear();
                if (result.elements != null && result.elements.size() > 0) {
                    for (OverpassQueryResult.Element element : result.elements) {
                        switch (element.type) {
                            case "node":
                                mNodes.put(element.id, element);
                                break;
                            case "way":
                                mWays.put(element.id, element);
                                break;
                        }
                    }

                    for (Map.Entry<String, OverpassQueryResult.Element> element : mNodes.entrySet()) {
                        if (element.getValue().ways == null) {
                            element.getValue().ways = new ArrayList<>();
                        }
                        element.getValue().ways.add(detectWayById(element.getKey()));
                    }

                    Log.d(SpeedLimitApp.APP_NAME,
                            "Loaded " + result.elements.size() + " elements ("
                                    + mNodes.size() + "; " + mWays.size() + ")");

                    detectNearestMaxSpeedByTwoPoints(mLatitude, mLongitude);
                } else {
                    completionHandlerWithError("Elements is empty");
                }
            }

            @Override
            public void completionHandlerWithError(String error) {
                mNodes.clear();
                mWays.clear();
                Log.d(SpeedLimitApp.APP_NAME,
                        "Loaded with error: " + error);
                setDefaultsLatLng();
                if (mOnMaxSpeedDetectedListener != null) {
                    mOnMaxSpeedDetectedListener.maxSpeedDetected(null, null, null, null, 0);
                }
            }
        });
    }

    public interface OnMaxSpeedDetectedListener {
        void maxSpeedDetected(@Nullable String speed,
                              @Nullable String nodeId,
                              @Nullable String wayId,
                              @Nullable String wayName,
                              float distance);
    }

}

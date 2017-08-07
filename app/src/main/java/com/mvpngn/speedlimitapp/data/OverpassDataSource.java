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
            OverpassQueryResult.Element nearestWay = detectWayByNodeId(nearestNode.id);
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

        SortedMap<Float, OverpassQueryResult.Element> nodeSortedMap =
                getNodeSortedMapByDistanceToPoint(latitude, longitude);
        SortedMap<Float, OverpassQueryResult.Element> waySortedMap =
                getWaySortedMap(nodeSortedMap, latitude, longitude, SpeedLimitApp.WAY_MAX_COUNT);
        return waySortedMap.size() > 0 ? new AbstractMap.SimpleEntry<>(waySortedMap
                .get(waySortedMap.firstKey()), waySortedMap.firstKey()) : null;
    }

    private SortedMap<Float, OverpassQueryResult.Element> getWaySortedMap(
            SortedMap<Float, OverpassQueryResult.Element> nodeSortedMap,
            double latitude,
            double longitude,
            int count) {

        SortedMap<Float, OverpassQueryResult.Element> waySortedMap = new TreeMap<>();

        for (Map.Entry<Float, OverpassQueryResult.Element> entry : nodeSortedMap.entrySet()) {
            float distance = Float.MAX_VALUE;
            String currentWayId = null;
            for (Map.Entry<Float, OverpassQueryResult.Element> subEntry : nodeSortedMap.entrySet()) {
                for (String wayId : entry.getValue().wayIds) {
                    if (mWays.get(wayId).nodes.contains((subEntry.getValue().id)) &&
                            subEntry.getKey() < distance && !entry.getKey().equals(subEntry.getKey())) {
                        distance = subEntry.getKey();
                        currentWayId = wayId;
                    }
                }
            }
            if (currentWayId != null) {
                float wayDistance = LatLngHelper.getDistanceToLine(
                        latitude,
                        longitude,
                        entry.getValue().lat,
                        entry.getValue().lon,
                        nodeSortedMap.get(distance).lat,
                        nodeSortedMap.get(distance).lon);
                waySortedMap.put(wayDistance, mWays.get(currentWayId));
            }
            count--;
            if (count <= 0) {
                return waySortedMap;
            }
        }
        return waySortedMap;
    }

    private SortedMap<Float, OverpassQueryResult.Element> getNodeSortedMapByDistanceToPoint(
            double latitude,
            double longitude) {

        SortedMap<Float, OverpassQueryResult.Element> sortedMap = new TreeMap<>();
        for (Map.Entry<String, OverpassQueryResult.Element> element : mNodes.entrySet()) {
            float distance = LatLngHelper.getDistanceBetweenPoints(
                    latitude, longitude, element.getValue().lat, element.getValue().lon);
            sortedMap.put(distance, element.getValue());
        }
        return sortedMap;
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

    private OverpassQueryResult.Element detectWayByNodeId(String nodeId) {
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
                        if (element.getValue().wayIds == null) {
                            element.getValue().wayIds = new ArrayList<>();
                        }
                        OverpassQueryResult.Element way = detectWayByNodeId(element.getKey());
                        if (way != null) {
                            element.getValue().wayIds.add(way.id);
                        }
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

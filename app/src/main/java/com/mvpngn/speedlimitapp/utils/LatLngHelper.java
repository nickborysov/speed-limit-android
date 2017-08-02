package com.mvpngn.speedlimitapp.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class LatLngHelper {

    public static final double WGS84_RADIUS = 6370997.0;
    public static final double EARTH_CIRCUM_FENCE = 2 * WGS84_RADIUS * Math.PI;
    public static final double DEGREES_PER_METER_FOR_LAT = EARTH_CIRCUM_FENCE / 360.0;

    public static LatLngBounds getBoundsByPosition(
            double latitude,
            double longitude,
            float meters) {
        double shrinkFactor = Math.cos((latitude * Math.PI / 180));
        double degreesPerMeterForLon = DEGREES_PER_METER_FOR_LAT * shrinkFactor;
        double southWestLat = latitude - meters / 2 * (1 / DEGREES_PER_METER_FOR_LAT);
        double southWestLng = longitude - meters / 2 * (1 / degreesPerMeterForLon);
        double northEastLat = latitude + meters / 2 * (1 / DEGREES_PER_METER_FOR_LAT);
        double northEastLng = longitude + meters / 2 * (1 / degreesPerMeterForLon);
        return new LatLngBounds(new LatLng(southWestLat, southWestLng),
                new LatLng(northEastLat, northEastLng));
    }

    public static float getDistanceBetweenPoints(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2) {
        double shrinkFactor = Math.cos((latitude1 * Math.PI / 180));
        double degreesPerMeterForLon = DEGREES_PER_METER_FOR_LAT * shrinkFactor;
        double metersLat = (latitude1 - latitude2) * DEGREES_PER_METER_FOR_LAT * 2;
        double metersLng = (longitude1 - longitude2) * degreesPerMeterForLon * 2;
        double meters = Math.sqrt(Math.pow(metersLat, 2) + Math.pow(metersLng, 2));
        return (float) meters;
    }

    public static float getDistanceToLine(
            double currentLat,
            double currentLng,
            double pointLat1,
            double pointLng1,
            double pointLat2,
            double pointLng2) {

        if (pointLat1 == pointLat2 && pointLng1 == pointLng2) {
            return 0f;
        }
        double meters = ((pointLng1 - pointLng2) * currentLat + (pointLat2 - pointLat1) * currentLng +
                (pointLat1 * pointLng2 - pointLat2 * pointLng1)) /
                Math.sqrt(Math.pow(pointLat2 - pointLat1, 2) + Math.pow(pointLng2 - pointLng1, 2));

        return (float) Math.abs(meters);

    }
}

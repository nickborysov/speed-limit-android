package com.mvpngn.speedlimitapp.data;

import android.util.Log;

import com.mvpngn.speedlimitapp.SpeedLimitApp;
import com.mvpngn.speedlimitapp.utils.LatLngHelper;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OverpassServiceRequest {

    LatLngBounds mLatLngBounds;

    public OverpassServiceRequest(double latitude, double longitude, float radius) {
        mLatLngBounds = LatLngHelper.getBoundsByPosition(latitude, longitude, radius);
    }

    public OverpassServiceRequest(LatLngBounds latLngBounds) {
        this.mLatLngBounds = latLngBounds;
    }

    public void resumeWithCompletionHandler(CompletionHandler handler) {
        final CompletionHandler completionHandler = handler;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://overpass-api.de")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        String query = getQueryString(mLatLngBounds);
        Log.d(SpeedLimitApp.APP_NAME, "http://overpass-api.de/api/interpreter?data=" + query);
        OverpassService service = retrofit.create(OverpassService.class);
        service.interpreter(query).enqueue(
                new Callback<OverpassQueryResult>() {
                    @Override
                    public void onResponse(Call<OverpassQueryResult> call, Response<OverpassQueryResult> response) {
                        if (response != null && response.body() != null) {
                            completionHandler.completionHandler(response.body());
                        } else {
                            onFailure(call, new Throwable("Response empty"));
                        }
                    }

                    @Override
                    public void onFailure(Call<OverpassQueryResult> call, Throwable t) {
                        completionHandler.completionHandlerWithError(t.getMessage());
                    }
                });
    }

    private String getQueryString(final LatLngBounds bounds) {
        StringBuilder query = new StringBuilder();
        StringBuilder coordinates = new StringBuilder()
                .append("(")
                .append(String.format(Locale.US, "%.14f", bounds.southwest.latitude)).append(",")
                .append(String.format(Locale.US, "%.14f", bounds.southwest.longitude)).append(",")
                .append(String.format(Locale.US, "%.14f", bounds.northeast.latitude)).append(",")
                .append(String.format(Locale.US, "%.14f", bounds.northeast.longitude)).append(");");

        query.append("[out:json]")
                .append("[timeout:25]")
                .append("; (")
                .append("node[\"maxspeed\"]")
                .append(coordinates)
                .append("way[\"maxspeed\"]")
                .append(coordinates)
                .append("relation[\"maxspeed\"]")
                .append(coordinates)
                .append("); out; >; out skel qt;");
        return query.toString();
    }

    public interface CompletionHandler {
        void completionHandler(OverpassQueryResult result);

        void completionHandlerWithError(String error);
    }


}
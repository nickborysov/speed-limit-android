package com.mvpngn.speedlimitapp.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.mvpngn.speedlimitapp.R;

public class OpenSettingsDialogs {

    final Context mContext;

    public OpenSettingsDialogs(@NonNull Context context) {
        this.mContext = context;
    }

    public AlertDialog.Builder gpsDisabledDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setMessage(mContext.getString(R.string.location_not_enabled));
        dialogBuilder.setPositiveButton(mContext.getResources().getString(R.string.enable),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myAppSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(myAppSettings);
                    }
                });
        dialogBuilder.setNegativeButton(mContext.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        return dialogBuilder;
    }

    public AlertDialog.Builder networkDisabledDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setMessage(mContext.getString(R.string.network_not_enabled));
        dialogBuilder.setPositiveButton(mContext.getResources().getString(R.string.enable),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myAppSettings = new Intent(Settings.ACTION_SETTINGS);
                        mContext.startActivity(myAppSettings);
                    }
                });
        dialogBuilder.setNegativeButton(mContext.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        return dialogBuilder;
    }

    public AlertDialog.Builder locationPermissionDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setMessage(mContext.getString(R.string.location_permission_not_granted));
        dialogBuilder.setPositiveButton(mContext.getResources().getString(R.string.enable),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + mContext.getPackageName()));
                        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(myAppSettings);
                    }
                });
        dialogBuilder.setNegativeButton(mContext.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        return dialogBuilder;
    }
}

/*
According to apache license

This is fork of christocracy cordova-plugin-background-geolocation plugin
https://github.com/christocracy/cordova-plugin-background-geolocation

This is a new class
*/

package com.marianhello.bgloc.provider;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.marianhello.bgloc.Config;
import com.tenforwardconsulting.bgloc.DistanceFilterLocationProvider;

import java.lang.IllegalArgumentException;

/**
 * LocationProviderFactory
 */
public class LocationProviderFactory {

    private Context mContext;

    public LocationProviderFactory(Context context) {
        this.mContext = context;
    };

    public LocationProvider getInstance (Integer locationProvider) {
        LocationProvider provider;
        Integer effectiveProvider = locationProvider;
        
        // PATCH: If FUSED_PROVIDER is requested but ACTIVITY_RECOGNITION permission is not granted,
        // fallback to DISTANCE_FILTER_PROVIDER (which doesn't require ACTIVITY_RECOGNITION)
        if (locationProvider == Config.FUSED_PROVIDER) {
            boolean hasActivityRecognitionPermission = false;
            
            // Check for ACTIVITY_RECOGNITION permission on Android 10+ (API 29+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hasActivityRecognitionPermission = ContextCompat.checkSelfPermission(
                    mContext, 
                    android.Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED;
                
                if (!hasActivityRecognitionPermission) {
                    Log.w("LocationProviderFactory", 
                        "FUSED_PROVIDER requested but ACTIVITY_RECOGNITION permission not granted. " +
                        "Falling back to DISTANCE_FILTER_PROVIDER");
                    effectiveProvider = Config.DISTANCE_FILTER_PROVIDER;
                } else {
                    Log.i("LocationProviderFactory", 
                        "FUSED_PROVIDER requested with ACTIVITY_RECOGNITION permission granted");
                }
            } else {
                // On Android 9 and below, ACTIVITY_RECOGNITION is not required
                Log.i("LocationProviderFactory", 
                    "FUSED_PROVIDER requested on Android < 10 (no ACTIVITY_RECOGNITION required)");
            }
        }
        
        switch (effectiveProvider) {
            case Config.DISTANCE_FILTER_PROVIDER:
                provider = new DistanceFilterLocationProvider(mContext);
                break;
            case Config.ACTIVITY_PROVIDER:
                provider = new ActivityRecognitionLocationProvider(mContext);
                break;
            case Config.RAW_PROVIDER:
                provider = new RawLocationProvider(mContext);
                break;
            case Config.FUSED_PROVIDER:
                provider = new FusedLocationProvider(mContext);
                break;
            default:
                throw new IllegalArgumentException("Provider not found");
        }

        return provider;
    }
}

package com.melin.jonas.LocationRecorder.Service;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

/**
 * Created by jonas on 2016-02-29.
 *
 * subscriber for android position callbacks...
 */
public class MyLocationListener implements android.location.LocationListener{

    private Boolean initialized = false;
    private PositionManager positionManager;
    private static MyLocationListener myself;
    private LocationManager locationManager = (LocationManager)LocationRecorderService.getInstance().getSystemService(Context.LOCATION_SERVICE);

    /**
     * Singleton
     * @return
     * @throws Exception
     */
    public static MyLocationListener getInstance() throws Exception{
        if(myself == null){
            myself = new MyLocationListener();
        }
        return myself;
    }

    /**
     * private constructor...
     * @throws Exception
     */
    private MyLocationListener() throws Exception {

        positionManager = PositionManager.getInstance();
        String retval = init();

        if(retval.compareTo("") != 0){
            throw new Exception(retval);
        }
    }

    /**
     * Init positioning and request callbacks with position updates..
     * @return
     */
    private synchronized String init(){

        if (initialized)
            return "";

        //int permissionCheck = ContextCompat.checkSelfPermission(thisActivity,
        //        Manifest.permission.WRITE_CALENDAR);

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, SupportAndDefinitions.posUpdateInterval, 0, this);
        }catch (SecurityException e){
            return("Permission denied: locationManager.requestLocationUpdates");
        }
        initialized = true;
        return "";
    }

    /**
     * check if the phone has enabled its positioning
     * @return true if on, false else
     */

    public boolean checkPositioningEnabled(){
        // Note that we wannto default to report all OK, as we want an explicit tell back
        // from locationManager. TIf we dont even have access to location we wantto solve that first
        // E.g. we cannot know if locationing is enabled. on exception report all OK
        boolean positioningEnabled = true;

        try {
            positioningEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(positioningEnabled == false){
                MyLogger myLogger = MyLogger.getInstance();
                myLogger.logStatus("PLEASE ENABLE POSITIONING!!!","");
            }
        } catch(Exception ex) {
        }

        return positioningEnabled;
    }

    /**
     * Called when location is changed. Send position to position manager...
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        positionManager.handlePosition(location.getLatitude(), location.getLongitude(), location.getAccuracy());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}

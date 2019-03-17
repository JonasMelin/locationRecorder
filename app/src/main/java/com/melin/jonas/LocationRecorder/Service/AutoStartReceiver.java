package com.melin.jonas.LocationRecorder.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by jonas on 2016-05-05.
 *
 * Callback receiver of the system boot event. Will start the service in case of boot.
 */
public class AutoStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(LocationRecorderService.getInstance() == null) {
            Intent myIntent = new Intent(context, LocationRecorderService.class);
            context.startService(myIntent);
        }
    }
}


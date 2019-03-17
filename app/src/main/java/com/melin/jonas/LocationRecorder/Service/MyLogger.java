package com.melin.jonas.LocationRecorder.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.melin.jonas.LocationRecorder.MainActivity;
import com.melin.jonas.LocationTracker.R;

/**
 * Created by jonas on 2016-03-19.
 *
 * Logging to std.out for debug and to user notifications in android drop down menu
 */
public class MyLogger {

    private static MyLogger myself = null;

    /**
     * Singleton
     * @return
     */
    public static MyLogger getInstance(){

        if(myself == null){
            myself = new MyLogger();
        }
        return myself;
    }

    /**
     * private constructor
     */
    private MyLogger(){

    }

    /**
     * Logs to std out if DEBUG is enabled...
     * @param text
     */
    public static void sysout(String text){
        if(SupportAndDefinitions.DEBUG)
            System.out.println(text);
    }

    /**
     * Logs text to android notification bar.
     * @param mainText main text to be displayed
     * @param extraText extra text to be put in bottom right corner of notification
     */
    public synchronized void logStatus(String mainText, String extraText){
        logStatus(mainText, extraText, 0xfaf1af);
    }
    public synchronized void logStatus(String mainText, String extraText, int color ){
        LocationRecorderService service = LocationRecorderService.getInstance();

        if(service == null)
            return;

        Intent notificationIntent = new Intent(service, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(service, 0,
                notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(service);
        builder.setSmallIcon(R.mipmap.ic_launcher2);
        builder.setColor(color);
        builder.setLargeIcon(BitmapFactory.decodeResource(service.getResources(), R.mipmap.ic_launcher2));

        builder.setContentTitle("Location Recorder");
        builder.setContentText(mainText);

        builder.setContentIntent(pendingIntent);

        if(extraText != "") {
            builder.setContentInfo(extraText);
        }
        Notification notification = builder.build();
        service.startForeground(2819, notification);
        MyLogger.sysout("--Status: " + mainText);
    }
}

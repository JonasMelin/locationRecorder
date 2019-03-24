package com.melin.jonas.LocationRecorder.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import com.melin.jonas.LocationRecorder.MainActivity;
import com.melin.jonas.LocationTracker.R;

/**
 * Created by jonas on 2016-03-19.
 *
 * Logging to std.out for debug and to user notifications in android drop down menu
 */
public class MyLogger {

    private static MyLogger myself = null;
    private static NotificationChannel myNotificationChannel = null;
    private static NotificationCompat.Builder builder = null;
    NotificationManagerCompat notificationManager = null;
    private static final String channelId = "aASDGYR542";
    private int notificationId = 1;
    private boolean startedForeground = false;
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

    /**
     * Logs text to android notification bar.
     * @param mainText main text to be displayed
     * @param extraText extra text to be put in bottom right corner of notification
     * @param color ...
     */
    public synchronized void logStatus(String mainText, String extraText, int color ){
        sysout("NOTIFICATION: "+ mainText + " " + extraText );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            logStatusNew(mainText, extraText, color);
        }else{
            logStatusOld(mainText, extraText, color);
        }
    }

    /**
     * Logs text to android notification for systems < VERSION_CODES.O
     * @param mainText
     * @param extraText
     * @param color
     */
    private synchronized void logStatusOld(String mainText, String extraText, int color ){
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

    /**
     * Logs text to android notification for systems >= VERSION_CODES.O
     * @param mainText
     * @param extraText
     * @param color
     */
    private synchronized void logStatusNew(String mainText, String extraText, int color ) {

        createNotificationChannel();
        LocationRecorderService context = LocationRecorderService.getInstance();

        if (context == null)
            return;

        if (notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(context);
        }

        if (builder == null){
            sysout("CREATING NEW NotificationCompat.Builder");
            builder = new NotificationCompat.Builder(context, channelId);
            builder.setOngoing(true);
            builder.setSmallIcon(R.mipmap.ic_launcher2);
            builder.setOnlyAlertOnce(true);
        }

        notificationManager.cancelAll();
        if(mainText != ""){builder.setContentText(mainText);}
        if(extraText != ""){builder.setSubText(extraText);}
        Notification notification = builder.build();

        if(!startedForeground){
            sysout("startForeground notification... (shall only happen once!!)");
            startedForeground = true;
            // Need to register the notification in order for the OS not to put us down...
            // The user must see us as we are defined as a foreground service.
            context.startForeground(notificationId, notification);
        }
        notificationManager.notify(notificationId, notification);
    }

    /**
     * Creates the notificatino channel
     *
     */
    private void createNotificationChannel() {

        if (myNotificationChannel == null) {
            LocationRecorderService context = LocationRecorderService.getInstance();

            if (context == null)
                return;

            sysout("CREATING CHANNEL");
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            CharSequence name = "Recording status";
            String description = "Location recorder notification channel";
            int importance = NotificationManager.IMPORTANCE_LOW;
            myNotificationChannel = new NotificationChannel(channelId, name, importance);
            myNotificationChannel.setDescription(description);
            myNotificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(myNotificationChannel);
            sysout("DONE CREATING CHANNEL");
        }
    }
}

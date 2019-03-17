package com.melin.jonas.LocationRecorder.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.melin.jonas.LocationRecorder.MainActivity;

/**
 * The implementation of the Android Service
 */
public class LocationRecorderService extends Service {

    private PositionRecordListHandler positionRecordList;
    private MyLogger logger = MyLogger.getInstance();
    private static LocationRecorderService myself;

    public LocationRecorderService() {
        myself = this;
        positionRecordList = PositionRecordListHandler.getInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * get instance. No singleton though, but started by the OS.
     * Note: Don't start two of this service, as return value will
     * point to last started only...
     *
     * @return
     */
    public static LocationRecorderService getInstance(){
        return myself;
    }

    /**
     * Returns the report log as a string, ready to print in GUI
     * @return
     */
    public String getReportLogs(){
        return positionRecordList.toString();
    }

    /**
     * Clear the log file. If clearAll=true, then also clear all known positions...
     * If clearLast == 0, then all logs will be cleared
     * If clearLast > 0, then clearLast number of logs will be cleared
     * Still, if clearAll=true, everything will be cleared...
     * @param clearAll
     */
    public void clearLogs(boolean clearAll, int clearLast){
        MyLogger.sysout("PERFORMING CLEAR LOGS!!!");
        positionRecordList.clearList(clearAll, clearLast);
    }

    /**
     * As the function suggests. Clears positions logs that the user did not give a name.
     */
    public void clearUnnamed(){
        positionRecordList.clearUnnamed(true);
    }

    /**
     * Renames all positions record logs
     */
    public void rename(String oldName, String newName){
        MyLogger.sysout("Renaming from " + oldName + " to " + newName);
        positionRecordList.renamePositions(oldName, newName);
    }

    /**
     *
     */
    @Override
    public void onCreate() {
        super.onCreate();

        positionRecordList.init();
        this.logger.logStatus("Initilalizing positioning...", "");
    }

    /**
     *
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        myself = null;
    }

    /**
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int retval =  super.onStartCommand(intent, flags, startId);

        try {
            MyLocationListener.getInstance();
        }catch (Exception ex){
            MainActivity l = MainActivity.getInstance();
            if (l != null){
                l.logReportGui("Problem with positioning. Did you give me permission to use it?\n\n"+ex.toString());
                stopSelf();
            }
        }
        return retval;
    }

    /**
     * return user params
     * @return
     */
    public UserParamValues getUserParams(){
        return UserParamValuesHandler.getInstance().getUserParams();
    }

    /**
     * return user params
     * @return
     */
    public void setUserParams(UserParamValues params){
        UserParamValuesHandler.getInstance().setUserParams(params);
    }

    /**
     *
     * @return if location services are enabled in the phone
     */
    public boolean checkPositioningEnabled(){

        try{

            return MyLocationListener.getInstance().checkPositioningEnabled();
        }catch (Exception ex){
            // If we don't have this instance, it might be because e.g. we are not even
            // allowed to use positioning, we are starting up and waiting for the user to give
            // it to us. The service will terminate anyways. Dont bother try to through
            // Even more dialoges and stuff to the user. We have bigger problems. Report OK.
            return true;
        }
    }
}

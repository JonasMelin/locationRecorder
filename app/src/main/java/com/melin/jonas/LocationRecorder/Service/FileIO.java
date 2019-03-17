package com.melin.jonas.LocationRecorder.Service;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

/**
 * Created by Jonas on 2017-05-02.
 *
 * Handles file writing to/from disk...
 */

public class FileIO {

    private int writeCurrentToDiskCount = 0;

    /**
     * Writes all position records to disk.
     * Note: Dont forget to clean possible current recordings at the same time,
     * else they will show up as "aborted during next app start"
     */
    public synchronized void writeToDisk(LinkedList<PositionRecord> locationLog){

        LocationRecorderService myService = LocationRecorderService.getInstance();

        if (myService == null){
            MyLogger.sysout("COULD NOT GET HANDLE TO service!!!");
            return;
        }

        try {

            FileOutputStream outputStream = myService.openFileOutput("positionLog2.obj", Context.MODE_PRIVATE);
            ObjectOutputStream oo = new ObjectOutputStream(outputStream);
            oo.writeObject(locationLog);
            oo.close();

        }catch (Exception ex){
            MyLogger.sysout(ex.toString());
            MyLogger.sysout("Failed serialize locactionLog and write it to disk");
        }
    }


    /**
     * Writes the current, ongoing recording to disk. Good to have if
     * the app abnormally terminates.
     * Note: It must be tagged storePosWhenLeft "ongoing=true" to be useful during
     * next app start up.
     * @param current
     */
    public void writeCurrentToDisk(PositionRecord current){

        // Some logic to avoid writing to much to disk..
        if(writeCurrentToDiskCount == 0){
            //now do the actul disk writing...
            writeCurrentToDiskCount++;
        }else if (writeCurrentToDiskCount < 10){
            writeCurrentToDiskCount++;
            return;
        }else{
            writeCurrentToDiskCount = 0;
            return;
        }

        LocationRecorderService myService = LocationRecorderService.getInstance();

        if (myService == null){
            MyLogger.sysout("COULD NOT GET HANDLE TO service!!! OK during unit testing.");
            return;
        }

        if(current == null) {
            return;
        }

        try {
            FileOutputStream outputStream = myService.openFileOutput("aborted2.obj", Context.MODE_PRIVATE);
            ObjectOutputStream oo = new ObjectOutputStream(outputStream);
            oo.writeObject(current);
            oo.close();
        }catch (Exception ex){
            MyLogger.sysout(ex.toString());
            MyLogger.sysout("Failed to serialize current and write it to disk (new format)");
        }
    }

    /**
     * Read all data from disk during start and load it to ram...
     */
    public synchronized LinkedList<PositionRecord> readFromDisk() throws Exception{

        LinkedList<PositionRecord> locationLog = new LinkedList<>();

        try{
            LocationRecorderService myService = LocationRecorderService.getInstance();

            if (myService == null){
                MyLogger.sysout("COULD NOT GET HANDLE TO service!!! OK during unit testing..");
                return locationLog;
            }

            FileInputStream inputStream = myService.openFileInput("positionLog2.obj");
            ObjectInputStream oi = new ObjectInputStream(inputStream);
            locationLog = (LinkedList<PositionRecord>)oi.readObject();
            MyLogger.sysout("SUCCESSFULLY READ LOGS FROM DISK (new format)");
            oi.close();

            try{
                inputStream = myService.openFileInput("aborted2.obj");
                oi = new ObjectInputStream(inputStream);
                PositionRecord aborted = (PositionRecord)oi.readObject();
                MyLogger.sysout("SUCCESSFULLY READ ABORTED FROM DISK (new format)");
                oi.close();

                if(aborted.isInProgress()){
                    //We found an ongoing recording on disk. That means we got an abnormal
                    // app termination...
                    aborted.setSpare1(true);
                    aborted.setInProgress(false);
                    // Reset the aborted record on disk. not to be read again...
                    writeCurrentToDisk(aborted);
                    locationLog.add(aborted);
                    //Store entire log to disk
                    writeToDisk(locationLog);
                }
            }catch (Exception ex){
                MyLogger.sysout("No aborted recording was found... (new format)");
            }
        }catch (Exception ex){
            MyLogger.sysout(ex.toString());
            MyLogger.sysout("Failed de-serialize locactionLog and read it from disk (new format)");
            //locationLog = new LinkedList<>();
            MyLogger.sysout("FAILED READING DISK (new format).");
            throw new Exception(ex);
        }
        return locationLog;
    }

    /**
     * return user params from disk. If failed reading, return default params
     * @return
     */
    public UserParamValues readParamsFromDisk(){
        LocationRecorderService myService = LocationRecorderService.getInstance();
        UserParamValues params = new UserParamValues();
        try{


            if (myService == null){
                MyLogger.sysout("COULD NOT GET HANDLE TO service!!! OK during unit testing..");
                return params;
            }

            FileInputStream inputStream = myService.openFileInput("params2.obj");
            ObjectInputStream oi = new ObjectInputStream(inputStream);
            params = (UserParamValues)oi.readObject();
            MyLogger.sysout("SUCCESSFULLY READ PARAMS FROM DISK " + params.getMAX_LOG_SIZE());
            oi.close();

        }catch (Exception ex){
            MyLogger.sysout(ex.toString());
            MyLogger.sysout("Failed de-serialize params and read it from disk");
        }
        return params;
    }

    /**
     * writes user params to disk
     * @param params
     */
    public void writeParamsToDisk(UserParamValues params){
        LocationRecorderService myService = LocationRecorderService.getInstance();

        if (myService == null){
            MyLogger.sysout("COULD NOT GET HANDLE TO service!!! OK during unit testing.");
            return;
        }

        if(params == null) {
            return;
        }

        try {
            FileOutputStream outputStream = myService.openFileOutput("params2.obj", Context.MODE_PRIVATE);
            ObjectOutputStream oo = new ObjectOutputStream(outputStream);
            oo.writeObject(params);
            oo.close();
            MyLogger.sysout("SUCCESSFULLY WROTE PARAMS TO DISK " + params.getMAX_LOG_SIZE());
        }catch (Exception ex){
            MyLogger.sysout(ex.toString());
            MyLogger.sysout("Failed to serialize params and write it to disk");
        }
    }
}

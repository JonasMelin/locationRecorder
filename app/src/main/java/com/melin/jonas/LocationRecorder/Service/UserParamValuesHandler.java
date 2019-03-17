package com.melin.jonas.LocationRecorder.Service;

/**
 * Created by Jonas on 2017-05-06.
 *
 * Gets user parameters from disk and stores new ones to disk
 */

public class UserParamValuesHandler {

    private UserParamValues userParamValues = new UserParamValues();
    private FileIO fileIO = new FileIO();
    private static UserParamValuesHandler myself = null;

    public static UserParamValuesHandler getInstance(){
        if(myself == null){
            myself = new UserParamValuesHandler();
        }
        return myself;
    }

    private UserParamValuesHandler(){
        userParamValues = fileIO.readParamsFromDisk();
    }

    public UserParamValues getUserParams(){
        return userParamValues;
    }

    public void setUserParams(UserParamValues params){
        userParamValues = params;
        fileIO.writeParamsToDisk(userParamValues);
    }

    public void restoreFactoryDefault(){
        userParamValues = new UserParamValues();
        fileIO.writeParamsToDisk(userParamValues);
    }
}

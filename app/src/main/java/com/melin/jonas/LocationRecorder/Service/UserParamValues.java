package com.melin.jonas.LocationRecorder.Service;

import java.io.Serializable;

/**
 * Created by Jonas on 2017-05-06.
 *
 * User parameters. Use the UserParamValuesHandler class to get an instance of this class
 * and to persist it to disk.
 */

public class UserParamValues implements Serializable {

    private int MAX_LOG_SIZE = 300;

    public UserParamValues(){

    }

    public int getMAX_LOG_SIZE() {
        return MAX_LOG_SIZE;
    }

    public void setMAX_LOG_SIZE(int MAX_LOG_SIZE) {
        this.MAX_LOG_SIZE = MAX_LOG_SIZE;
        PositionRecordListHandler.getInstance().autoDeleteLogs(this.MAX_LOG_SIZE);
    }
}

package com.melin.jonas.LocationRecorder.Service;

import android.location.Location;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by jonas on 2016-03-19.
 * A record containing all information about a recorded location....
 */
public class PositionRecord implements Serializable{

    private String recordVersion = "1.0";
    private double lat = 0;
    private double lon = 0;
    private Date arrivalDate;
    private Date leaveDate;
    private String name = "";
    private int minutesInPos = -1;
    private boolean hasName = false;
    private boolean clearedAndInvalid = false; // true - Used only to remember the name of this position
    private boolean nameSetByUser = false;
    private boolean inProgress = true;
    private float accuracy = 0.0f;

    private boolean spare1 = false;  // Indicates aborted recording...
    private int spare2 = 0;
    private float spare3 = 0.0f;
    private double spare4 = 0.0d;
    private boolean spare5 = false;
    private String spare6 = "";

    public PositionRecord(double latitude, double longitude, float accuracy) {
        lat = latitude;
        lon = longitude;
        setAccuracy(accuracy);
        this.arrivalDate = new Date();
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public Location getLocation() {
        Location location = new Location("name");
        location.setLatitude(lat);
        location.setLongitude(lon);
        return location;
    }

    public void updatePosition(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name, boolean nameSetByUser) {
        this.name = name;
        hasName = true;
        this.nameSetByUser = nameSetByUser;
    }

    public Date getLeaveDate() {
        return leaveDate;
    }

    public void setLeaveDate(Date leaveDate) {
        this.leaveDate = leaveDate;
    }

    public int getMinutesInPos() {
        return minutesInPos;
    }

    public void setMinutesInPos(int minutesInPos) {
        this.minutesInPos = minutesInPos;
    }

    public boolean hasName() {
        return hasName;
}

    public boolean isClearedAndInvalid() {
        return clearedAndInvalid;
    }

    public void setClearedAndInvalid(boolean clearedAndInvalid) {
        this.clearedAndInvalid = clearedAndInvalid;
    }

    public boolean isNameSetByUser() {
        return nameSetByUser;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {

        float maxAllowedAccuracy = SupportAndDefinitions.thresholdMovedHigher / SupportAndDefinitions.filterEffectAccuracy;

        if(accuracy > maxAllowedAccuracy) {
            this.accuracy = maxAllowedAccuracy;
            return;
        }

        this.accuracy = accuracy;
    }

    public boolean isSpare1() {
        return spare1;
    }

    public void setSpare1(boolean spare1) {
        this.spare1 = spare1;
    }

    public int getSpare2() {
        return spare2;
    }

    public void setSpare2(int spare2) {
        this.spare2 = spare2;
    }

    public float getSpare3() {
        return spare3;
    }

    public void setSpare3(float spare3) {
        this.spare3 = spare3;
    }

    public double getSpare4() {
        return spare4;
    }

    public void setSpare4(double spare4) {
        this.spare4 = spare4;
    }

    public boolean isSpare5() {
        return spare5;
    }

    public void setSpare5(boolean spare5) {
        this.spare5 = spare5;
    }

    public String getSpare6() {
        return spare6;
    }

    public void setSpare6(String spare6) {
        this.spare6 = spare6;
    }

    public String getRecordVersion() {
        return recordVersion;
    }

    public void setRecordVersion(String recordVersion) {
        this.recordVersion = recordVersion;
    }
}

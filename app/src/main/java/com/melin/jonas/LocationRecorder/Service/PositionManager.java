package com.melin.jonas.LocationRecorder.Service;

import java.util.Date;

/**
 * Created by jonas on 2016-03-06.
 *
 * The state machine that handles the new incoming positions... The "brain" of the program...
 */
public class PositionManager {

    private PositionRecordListHandler positionRecordList = PositionRecordListHandler.getInstance();
    private MyLogger logger = MyLogger.getInstance();
    private PositionRecord oldPos = null;
    private PositionRecord storedNewPos = null;
    //private DecimalFormat formatter = new DecimalFormat("#.##");
    public enum STATES {STARTING, MOVING, STOPPED, STABLE, LEAVING_STABLE};

    private STATES STATE = STATES.STARTING;
    private static PositionManager myself;
    private int leaveStableHysteresCount = 0;
    private int stoppedCounter = 0;


    /**
     *
     * @param
     */
    private PositionManager() {

    }

    /**
     * singleton
     * @return
     */
    public static PositionManager getInstance(){
        if(myself == null)
            myself = new PositionManager();
        return myself;
    }


    /**
     * Handle new position from OS.. State machine...
     * @param latitude
     * @param longitude
     * @param accuracy
     */
    public synchronized void handlePosition(double latitude, double longitude, float accuracy) {

        PositionRecord newPos = new PositionRecord(latitude, longitude, accuracy);

        if (oldPos == null) {
            oldPos = newPos;
            return;
        }

        updatePositionTimes(oldPos, newPos);
        positionRecordList.setGetNameForPosition(newPos);

        MyLogger.sysout("STATE " + STATE.toString());

        switch (STATE){
            case STARTING:

                if(leftLastArea(newPos, oldPos)){
                    STATE = STATES.MOVING;
                    logger.logStatus("Moving...", newPos.getName());
                    oldPos = newPos;
                }else{
                    STATE = STATES.STOPPED;
                    logger.logStatus("Moving... (slow) ", newPos.getName());
                }
                break;
            case MOVING:
                if(leftLastArea(newPos, oldPos)){
                    logger.logStatus("Moving...", newPos.getName());
                    oldPos = newPos;
                }else{
                    STATE = STATES.STOPPED;
                    logger.logStatus("Moving... (slow) ", newPos.getName());
                }
                break;
            case STOPPED:
                if(leftLastArea(newPos, oldPos)){
                    stoppedCounter = 0;
                    STATE = STATES.MOVING;
                    logger.logStatus("Moving...", newPos.getName());
                    oldPos = newPos;
                }else{
                    if(timeDiffLargerThanThresh(oldPos.getArrivalDate(), newPos.getArrivalDate())){
                        stoppedCounter = 0;
                        STATE = STATES.STABLE;
                        logger.logStatus("Recording since " + SupportAndDefinitions.minutesToHoursAndMinutes(oldPos.getMinutesInPos()) + "...", newPos.getName(),0x860a26);
                    }else {
                        if(stoppedCounter >=2) {
                            if(newPos.hasName() && newPos.isNameSetByUser()){
                                // If we entered a previous, named, known position, start recording more rapidly...
                                stoppedCounter = 0;
                                STATE = STATES.STABLE;
                                logger.logStatus("Recording since " + SupportAndDefinitions.minutesToHoursAndMinutes(oldPos.getMinutesInPos()) + "...", newPos.getName(), 0x860a26);
                            }else{
                                logger.logStatus("Not moving since "+ SupportAndDefinitions.minutesToHoursAndMinutes(oldPos.getMinutesInPos()), newPos.getName());
                            }
                        }else{
                            logger.logStatus("Moving... (very slow)", newPos.getName());
                            stoppedCounter++;
                        }
                    }
                }
                break;
            case STABLE:
                if(leftLastArea(newPos, oldPos)){
                    if(areasHasSameSetName(newPos, oldPos)){
                        // Changed position, but new position has same name. Keep recording...
                        // Update the oldPos with new position data accordning to the newly measured position...
                        storeKnownPos(oldPos);
                        oldPos.updatePosition(newPos.getLocation().getLatitude(), newPos.getLocation().getLongitude());
                        logger.logStatus("(*)Recording since " + SupportAndDefinitions.minutesToHoursAndMinutes(oldPos.getMinutesInPos()) + "...", oldPos.getName(), 0x860a26);

                    }else {
                        STATE = STATES.LEAVING_STABLE;
                        logger.logStatus("Pending leave " + oldPos.getName() + " after " + SupportAndDefinitions.minutesToHoursAndMinutes(oldPos.getMinutesInPos()), "");
                        storedNewPos = newPos;
                    }
                }else{
                    if(newPos.hasName()){
                        oldPos.setName(newPos.getName(), newPos.isNameSetByUser());
                    }
                    if(!oldPos.hasName()){
                        oldPos.setName(positionRecordList.getNextName(), false);
                    }
                    SupportAndDefinitions.lowPassFilter(oldPos, newPos);
                    logger.logStatus("Recording since " + SupportAndDefinitions.minutesToHoursAndMinutes(oldPos.getMinutesInPos()) + "...", oldPos.getName(), 0x860a26);
                    positionRecordList.writeCurrentToDisk(oldPos);
                }
                break;
            case LEAVING_STABLE:
                assert(storedNewPos != null);
                if(leftLastArea(newPos, oldPos)) {
                    updatePositionTimes(oldPos, storedNewPos);

                    if (leaveStableHysteresCount >= SupportAndDefinitions.leaveSteadyHysteresCount) {
                        storeSteadyPosition(oldPos, storedNewPos);
                        STATE = STATES.MOVING;
                        logger.logStatus("Left "+oldPos.getName()+" after " + SupportAndDefinitions.minutesToHoursAndMinutes(oldPos.getMinutesInPos()), "");
                        oldPos = newPos;
                        storedNewPos = null;
                        leaveStableHysteresCount = 0;
                    }else{
                        logger.logStatus("Pending leave "+oldPos.getName()+" after " + SupportAndDefinitions.minutesToHoursAndMinutes(oldPos.getMinutesInPos()), "");
                        leaveStableHysteresCount++;
                    }

                }else{
                    SupportAndDefinitions.lowPassFilter(oldPos, newPos);
                    logger.logStatus("Recording since " + SupportAndDefinitions.minutesToHoursAndMinutes(oldPos.getMinutesInPos()) + "...", oldPos.getName(),0x860a26);
                    STATE = STATES.STABLE;
                    storedNewPos = null;

                    leaveStableHysteresCount = 0;
                }
                break;
            default:
                MyLogger.sysout("Internal state error...");
                return;
        }
    }

    /**
     * rename active position in the process of being recorded...
     * @param currentName
     * @param newName
     */
    public synchronized void renameActivePos(String currentName, String newName){
        if (oldPos == null)
            return;

        if (oldPos.getName().compareTo(currentName) == 0){
            oldPos.setName(newName, true);
        }

        if(STATE == STATES.STABLE) {
            logger.logStatus("Recording since " + SupportAndDefinitions.minutesToHoursAndMinutes(oldPos.getMinutesInPos()) + "...", oldPos.getName());
        }
    }

    /**
     * Calculate difference between two positions.
     * @param newPos
     * @param lastPos
     * @return true if we left the area according to threshold. false if still in area.
     */
    private boolean leftLastArea(PositionRecord newPos, PositionRecord lastPos){
        if(newPos == null || lastPos == null)
            return false;

        if(newPos.getLocation().distanceTo(lastPos.getLocation()) > SupportAndDefinitions.getThresholdMovedCurrent(newPos, lastPos)) {
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Returns if the two positions has identical names set by the user. E.g. two places
     * called "work"
     * @param newPos
     * @param lastPos
     * @return
     */
    private boolean areasHasSameSetName(PositionRecord newPos, PositionRecord lastPos){
        if((newPos.isNameSetByUser() && lastPos.isNameSetByUser()) &&
                (newPos.getName().compareTo(lastPos.getName()) == 0)){
            return true;
        }
        return false;
    }

    /**
     * Store a position by calling this function when we leave it.
     * @param first
     * @param second
     */
    private void storeSteadyPosition(PositionRecord first, PositionRecord second){
        updatePositionTimes(first, second);
        first.setInProgress(false);
        positionRecordList.storePosWhenLeft(first);
    }

    /**
     *
     */
    private void storeKnownPos(PositionRecord copyFromMe){
        PositionRecord newPos = new PositionRecord(copyFromMe.getLocation().getLatitude(), copyFromMe.getLocation().getLongitude(), copyFromMe.getAccuracy());
        newPos.setName(copyFromMe.getName(), copyFromMe.isNameSetByUser());
        newPos.setClearedAndInvalid(true);
    }

    /**
     * Update the time left and spent in 'first' position.
     * @param first
     * @param second
     */
    private void updatePositionTimes(PositionRecord first, PositionRecord second){
        int diff = (int)dateDiff(first.getArrivalDate(), second.getArrivalDate());
        first.setLeaveDate(second.getArrivalDate());
        first.setMinutesInPos(diff);
    }

    /**
     * Returns the ongoing recording.
     * Note: May be null if no ongoing recording
     * @return
     */
    public synchronized PositionRecord getCurrentRecording(){
        if(STATE == STATES.STABLE || STATE == STATES.LEAVING_STABLE)
            return oldPos;
        else
            return null;
    }

    /**
     * Returns the diff in seconds between two dates.
     * @param first
     * @param second
     * @return
     */
    private double dateDiff(Date first, Date second){
        double diffMs = second.getTime() - first.getTime();
        return diffMs / 1000 / 60;
    }

    /**
     * Check if time diff is larger than threshold. True if so...
     * @param first
     * @param second
     * @return
     */
    private boolean timeDiffLargerThanThresh(Date first, Date second){

        if(dateDiff(first, second) > SupportAndDefinitions.thresholdSteadyTime){
            return true;
        }
        return false;
    }

    public STATES getSTATE() {
        return STATE;
    }
}

package com.melin.jonas.LocationRecorder.Service;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by jonas on 2016-03-19.
 *
 * The "data base", that keeps all recordings, handles writing/reading from disk,
 * erasing, adding, renaming positions...
 */
public class PositionRecordListHandler {
    private LinkedList<PositionRecord> locationLog = new LinkedList<>();
    private MyLogger logger = MyLogger.getInstance();
    private static PositionRecordListHandler myself;
    private int writeCurrentToDiskCount = 0;
    private FileIO fileIO = new FileIO();
    private int autoClearLogCount = 0;


    private PositionRecordListHandler(){

    }

    public synchronized void init(){
        readFromDisk();
    }

    /**
     * Get an instance to myself
     * @return
     */
    public static PositionRecordListHandler getInstance(){
        if(myself == null){
            myself = new PositionRecordListHandler();
        }
        return myself;
    }

    /**
     * Returns the next available auto generated name, e.g. position3
     * @return
     */
    public synchronized String getNextName(){
        int counter =0;

        for (;;){
            counter++;
            String newName = "position"+counter;

            if(getFirstPositionNamePosBased(locationLog, newName, null) == null){
                // no match
                return newName;
            }
        }
    }

    /**
     * Store a position when we leave it...  We are on the move again!!
     * @param p
     */
    public synchronized void storePosWhenLeft(PositionRecord p){

        setGetNameForPosition(p);

        if(!p.hasName()){
            p.setName(getNextName(), false);
        }

        locationLog.add(p);
        fileIO.writeToDisk(locationLog);

        // Now reset the current recording on disk with a dummy record...
        PositionRecord r = new PositionRecord(0,0,0);
        r.setInProgress(false);
        fileIO.writeCurrentToDisk(r);

        writeCurrentToDiskCount = 0;

        autoDeleteLogs(UserParamValuesHandler.getInstance().getUserParams().getMAX_LOG_SIZE());
        trimListRemoveDuplicates();
    }

    /**
     * Removes duplicate entries based on name and position. Will not affect valid positions..
     * This is to keep the known locations that has name for later.
     * Some complexity here... Because different positions may have the same name...
     */
    private synchronized void trimListRemoveDuplicates(){

        LinkedList<PositionRecord> newList = new LinkedList<>();
        Iterator<PositionRecord> it = locationLog.iterator();

        while(it.hasNext()){
            PositionRecord next = it.next();

            if(!next.isClearedAndInvalid()){ // A valid position?
                newList.add(next); //just keep it..
            }

            // check if this position was already added to the new list..
            PositionRecord firstInNewListByNamePos = getFirstPositionNamePosBased(newList, next.getName(), next);

            // Did we yet not copy this location to new list? In that case, store it...
            // We basically store one, and only one, invalidated position record for each location
            // To recognize the name for future usage...
            if(firstInNewListByNamePos == null){
                newList.add(next);
            }
        }
        locationLog = newList;
    }

    /**
     * Clears all recordings but keeps known positions (e.g. gym or work) if
     * clearAll = false.
     * Really clears all and does factory reset if clearAll = true
     *
     * @param clearAll
     */
    public synchronized void clearList(boolean clearAll, int clearLast){

        if(clearAll){
            MyLogger.sysout("CLEAR EVERYTHING");
            locationLog = new LinkedList<>();
            fileIO.writeToDisk(locationLog);

            // Now reset the current recording on disk with a dummy record...
            PositionRecord r = new PositionRecord(0,0,0);
            r.setInProgress(false);
            fileIO.writeCurrentToDisk(r);
            return;
        }

        if(clearLast <= 0) {
            MyLogger.sysout("CLEAR LOG COMPLETELY, but keep known positions");
            Iterator<PositionRecord> it = locationLog.iterator();

            while (it.hasNext()) {
                PositionRecord next = it.next();
                next.setClearedAndInvalid(true);
            }

            trimListRemoveDuplicates();
            fileIO.writeToDisk(locationLog);
            return;
        }

        if(clearLast > 0) {
            MyLogger.sysout("CLEAR LAST LOGS, but keep the rest, e.g. keep last un-named, named and known positions");
            int counter = 0;

            Iterator<PositionRecord> it = locationLog.iterator();

            while (it.hasNext()) {
                PositionRecord next = it.next();
                if(!next.isClearedAndInvalid()) {
                    // Valid position in the log.. clear it and count it.
                    next.setClearedAndInvalid(true);
                    counter++;
                }
                if(counter >= clearLast){
                    // We have cleared requested logs. break..
                    MyLogger.sysout("We have cleared all requested logs. break.." + counter);
                    break;
                }
            }
            // clean up positionRecords
            clearUnnamed(false); // remove invalid unnamed..
            trimListRemoveDuplicates(); //Remove invalid duplicates...
            fileIO.writeToDisk(locationLog);
        }
    }

    /**
     *
     * @return The number of active, valid stored positions in the log.
     * It does not count "known positions"..
     */
    public LogStats getLogStats(){
        LogStats logStats = new LogStats();

        Iterator<PositionRecord> it = locationLog.iterator();

        while (it.hasNext()) {
            PositionRecord next = it.next();

            if(next.isClearedAndInvalid() == false){  //valid
                logStats.validCount++;

                if(next.isNameSetByUser()){  //name set by user
                    logStats.namedValidCount++;
                }else{ //name not set by user
                    logStats.unNamedValidCount++;
                }
            }
            if(next.isClearedAndInvalid() == true){  //invalid
                logStats.invalidatedCount++;

                if(next.isNameSetByUser()){  //name set by user
                    logStats.namedInvalidCount++;
                }else{ //name not set by user
                    logStats.unNamedInvalidCount++;
                }
            }
            if(next.isNameSetByUser()){ //Name set by user
                logStats.namedCount++;
            }else{
                logStats.unNamedCount++; //name not set by user
            }

        }
        return logStats;
    }

    /**
     * Function to typically auto clean the oldest logs.
     * @param keepThisMany This many logs will be kept...
     */
    public void autoDeleteLogs(int keepThisMany){
        int currentLogSize = getLogStats().validCount;
        int numberOfEntriesToDelete = currentLogSize - keepThisMany;

        if(numberOfEntriesToDelete < 1){
            MyLogger.sysout("autoDeleteLogs. Nothing to delete... CurrentSize: " + currentLogSize + " requested size: "+keepThisMany);
            return;
        }

        MyLogger.sysout("autoDeleteLogs. Deleting last "+numberOfEntriesToDelete+"logs");
        clearList(false, numberOfEntriesToDelete);
    }

    /**
     * Clears all positions that does not have a user crated name if clearAllIncludingValid == true..
     * However, if clearAllIncludingValid == false, only invalidated unnamed positions are cleared
     */
    public synchronized void clearUnnamed(boolean clearAllIncludingValid){
        Iterator<PositionRecord> it = locationLog.iterator();
        LinkedList<PositionRecord> toBeRemoved = new LinkedList<>();

        while(it.hasNext()){
            PositionRecord next = it.next();
            if(!next.isNameSetByUser() && //unnamed position?
                    (clearAllIncludingValid || next.isClearedAndInvalid( ))){  // Should we clear all, or is it already invalid?
                // then remove it...
                toBeRemoved.add(next);
            }
        }

        // Now, do the actual removal...
        it = toBeRemoved.iterator();

        while (it.hasNext()){
            PositionRecord next = it.next();
            locationLog.remove(next);
        }

        fileIO.writeToDisk(locationLog);
    }

    /**
     * Clear positions based on their name...
     * But keeps old, known positions.
     */
    private void clearByName(String name){
        Iterator<PositionRecord> it = locationLog.iterator();

        while(it.hasNext()){
            PositionRecord next = it.next();
            // We may now get a number of cleared and invalidated postions pointing to the same
            // geo position, but that is OK. They will be cleard when someone hits "clear log",
            // Then trimListRemoveDuplicates() will remove duplicates (but now that function would
            // affect the entire log...)
            if(next.getName().compareTo(name) == 0 ){
                next.setClearedAndInvalid(true);
            }
        }
        fileIO.writeToDisk(locationLog);
    }

    public int recordLengt() {
        return locationLog.size();
    }

    /**
     * if pos == null, simply return a stored position with the same name.
     * if pos != null, return a stored position if it has same name and is in the same location as pos
     * @param list
     * @param name
     * @param pos
     * @return
     */
    public synchronized PositionRecord getFirstPositionNamePosBased(LinkedList<PositionRecord> list, String name, PositionRecord pos) {
        Iterator<PositionRecord> it = list.iterator();

        while (it.hasNext()) {
            PositionRecord next = it.next();

            boolean matchedName = (next.getName().compareTo(name) == 0);

            if(pos == null){
                // Ignore position..
                if(matchedName) {
                    return next;
                }
            }else{
                // Use position
                if (matchedName &&
                        (next.getLocation().distanceTo(pos.getLocation()) < SupportAndDefinitions.getThresholdMovedCurrent(pos,next))) {
                    return next;
                }
            }
        }
        return null;
    }

    /**   Check if we already have named this lat/lon pos
     *    if so, update the name of p
     */
    public synchronized String setGetNameForPosition(PositionRecord p){

        Iterator <PositionRecord>it = locationLog.iterator();

        while (it.hasNext()){
            PositionRecord next = it.next();

            if(next.getLocation().distanceTo(p.getLocation()) < SupportAndDefinitions.getThresholdMovedCurrent(p, next)){
                p.setName(next.getName(), next.isNameSetByUser());
                return next.getName();
            }
        }
        return "";
    }

    /**
     * Renames a position...
     * @param oldName
     * @param newName
     */
    public synchronized void renamePositions(String oldName, String newName){

        if(newName.compareTo("") == 0){
            clearByName(oldName);
            return;
        }

        Iterator <PositionRecord>it = locationLog.iterator();

        while (it.hasNext()){
            PositionRecord next = it.next();

            if(next.getName().compareTo(oldName) == 0){
                next.setName(newName, true);
            }
        }
        fileIO.writeToDisk(locationLog);
        PositionManager.getInstance().renameActivePos(oldName, newName);

        return;
    }

    /**
     * Generates a "well formatted" string about all recordings. ready to print in UI
     * @return
     */
    @Override
    public synchronized String toString(){

        int entries = 0;

        String retVal = "";
        Iterator <PositionRecord>it = locationLog.iterator();



        // Create the string of all historical recordings...
        while (it.hasNext()) {
            PositionRecord next = it.next();
            String nextEntry = "";

            if(next.isClearedAndInvalid())
                continue;

            entries++;
            nextEntry = nextEntry + "\n -------- ";

            //spare1 = aborted
            if(next.isSpare1()){
                nextEntry = nextEntry + next.getName() + " (aborted) --\n";
            }else {
                nextEntry = nextEntry + next.getName() + " --------\n";
            }

            nextEntry = nextEntry + "Time spent: "+ SupportAndDefinitions.minutesToHoursAndMinutes(next.getMinutesInPos())+"\n";
            nextEntry = nextEntry + "From: "+next.getArrivalDate()+"\n";
            if(next.isSpare1()) {
                nextEntry = nextEntry + "Aborted: " + next.getLeaveDate() + " \n";
            }else{
                nextEntry = nextEntry + "To      : " + next.getLeaveDate() + "\n";
            }
            retVal = nextEntry + retVal;
        }

        // Append the current, ongoing recording...
        PositionRecord current = PositionManager.getInstance().getCurrentRecording();
        if(current != null){
            String nextEntry = "";
            entries ++;

            nextEntry = nextEntry + "\n -------- ";
            nextEntry = nextEntry + current.getName() + " (Recording...) --\n";
            nextEntry = nextEntry + "Time spent: "+ SupportAndDefinitions.minutesToHoursAndMinutes(current.getMinutesInPos())+"\n";
            nextEntry = nextEntry + "From: "+current.getArrivalDate()+"\n";
            nextEntry = nextEntry + "Now  : "+current.getLeaveDate()+"\n";
            retVal = nextEntry + retVal;
        }


        if(entries == 0){
            retVal = "Nothing to report yet...\n\nThings are running fine. Waiting for you to stay in a fixed position for a while..";

            try{
                MyLocationListener lm = MyLocationListener.getInstance();

                if(lm.checkPositioningEnabled() == false) {
                    retVal = ("Positioning disabled in phone!\n" +
                            "Please enable positioning through the phone settings menu...\n\n" +
                            "Note: The app uses passive positioning only so don't worry about draining the battery.");
                }
            }catch (Exception ex){}


        }
        return retVal;
    }

    public void writeCurrentToDisk(PositionRecord current){
        fileIO.writeCurrentToDisk(current);
    }
    /**
     * Read all data from disk during start and load it to ram...
     * This file reading is compatible with app version <= 4.2. Remove this function
     * when 4.2 is no longer used.
     */
    private synchronized void readFromDisk(){

        // read new format from disk
        try {
            locationLog = fileIO.readFromDisk();
            MyLogger.sysout("successfully read file from disk (new format)..");
            return;
        }catch (Exception ex){
            MyLogger.sysout("failed read file from disk (new format).. Will read old format instead");
        }

        try{
            LocationRecorderService myService = LocationRecorderService.getInstance();

            if (myService == null){
                MyLogger.sysout("COULD NOT GET HANDLE TO service!!!");
                return;
            }

            FileInputStream inputStream = myService.openFileInput("positionLog.obj");
            ObjectInputStream oi = new ObjectInputStream(inputStream);
            locationLog = (LinkedList<PositionRecord>)oi.readObject();
            MyLogger.sysout("SUCCESSFULLY READ LOGS FROM DISK");
            oi.close();

            try{
                inputStream = myService.openFileInput("aborted.obj");
                oi = new ObjectInputStream(inputStream);
                PositionRecord aborted = (PositionRecord)oi.readObject();
                MyLogger.sysout("SUCCESSFULLY READ ABORTED FROM DISK");
                oi.close();

                if(aborted.isInProgress()){
                    //We found an ongoing recording on disk. That means we got an abnormal
                    // app termination...
                    aborted.setSpare1(true);
                    aborted.setInProgress(false);
                    // Reset the aborted record on disk. not to be read again...
                    fileIO.writeCurrentToDisk(aborted);
                    locationLog.add(aborted);
                    //Store entire log to disk
                    fileIO.writeToDisk(locationLog);
                }
            }catch (Exception ex){
                MyLogger.sysout("No aborted recording was found...");
            }
        }catch (Exception ex){
            MyLogger.sysout(ex.toString());
            MyLogger.sysout("Failed de-serialize locactionLog and read it from disk");
            locationLog = new LinkedList<>();
            MyLogger.sysout("FAILED READING DISK. Creating new linked list.");
        }
        // Store this data in the new file format...
        fileIO.writeToDisk(locationLog);
    }

    public class LogStats{
        public int validCount = 0;
        public int invalidatedCount = 0;
        public int unNamedCount;
        public int unNamedValidCount;
        public int unNamedInvalidCount;
        public int namedCount;
        public int namedValidCount;
        public int namedInvalidCount;

        public String toString(){
            return "validCount:"+ validCount+
            "\ninvalidatedCount:"+ invalidatedCount+
            "\nunNamedCount:"+ unNamedCount+
            "\nunNamedValidCount:"+ unNamedValidCount+
            "\nunNamedInvalidCount:"+ unNamedInvalidCount+
            "\nnamedCount:"+ namedCount+
            "\nnamedValidCount:"+ namedValidCount+
            "\nnamedInvalidCount:"+ namedInvalidCount;
        }
    }
}

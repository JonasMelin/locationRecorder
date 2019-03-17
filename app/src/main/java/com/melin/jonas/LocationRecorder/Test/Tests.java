package com.melin.jonas.LocationRecorder.Test;

import android.test.InstrumentationTestCase;

import com.melin.jonas.LocationRecorder.Service.PositionManager;
import com.melin.jonas.LocationRecorder.Service.PositionRecord;
import com.melin.jonas.LocationRecorder.Service.PositionRecordListHandler;
import com.melin.jonas.LocationRecorder.Service.SupportAndDefinitions;
import com.melin.jonas.LocationRecorder.Service.UserParamValues;
import com.melin.jonas.LocationRecorder.Service.UserParamValuesHandler;

//import org.junit.Test;

import java.lang.Exception;

//import static org.junit.Assert.*;

/**
 * Automatic testing....
 */
public class Tests extends InstrumentationTestCase{

    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);

    }

    public void test() throws Exception{
        //sendPosition();
        newTests();
    }

    public void newTests() throws Exception{
        SupportAndDefinitions.thresholdSteadyTime = 0.1;

        PositionRecordListHandler posList = PositionRecordListHandler.getInstance();
        PositionManager posManager = PositionManager.getInstance();
        assertNotNull(posList);
        posList.init();

        //
        // Clear disk in some different ways..
        posList.clearUnnamed(true);
        posList.clearList(false, 0);
        posList.clearList(true, 0);
        assertEquals(posList.recordLengt(),0);
        printPosList(posList, "start");

        // Walk through the states
        assertNotNull(posManager);
        assertEquals(posManager.getSTATE() == PositionManager.STATES.STARTING, true);

        posManager.handlePosition(1.0d, 2.0d, 100);
        assertEquals(posManager.getSTATE() == PositionManager.STATES.STARTING, true);
        posManager.handlePosition(1.00001d, 2.00001d, 100);
        assertEquals(posManager.getSTATE() == PositionManager.STATES.STOPPED, true);

        // Set into stable state
        goToStable(posManager, 1.0, 2.0);
        assertEquals(posList.getLogStats().unNamedValidCount, 0);
        PositionRecord pCurrent = posManager.getCurrentRecording();

        // Name the stable position
        System.out.println("pCurrent.getName()");
        assertEquals(pCurrent.getName(), "position1");
        goToMoving(posManager);
        assertEquals(posList.getLogStats().unNamedValidCount, 1);
        posList.renamePositions("position1", "named1");
        assertEquals(posList.getLogStats().unNamedValidCount, 0);
        assertEquals(posList.getLogStats().namedValidCount, 1);
        goToStable(posManager, 2.0, 2.0);
        pCurrent = posManager.getCurrentRecording();
        assertEquals(pCurrent.getName(), "position1");
        assertEquals(posList.getLogStats().unNamedValidCount, 0);
        assertEquals(posList.getLogStats().namedValidCount, 1);
        goToMoving(posManager);
        assertEquals(posList.getLogStats().unNamedValidCount, 1);
        assertEquals(posList.getLogStats().namedValidCount, 1);

        posList.clearUnnamed(true);
        assertEquals(posList.getLogStats().unNamedValidCount, 0);
        assertEquals(posList.getLogStats().namedValidCount, 1);
        assertEquals(posList.getLogStats().namedInvalidCount, 0);
        assertEquals(posList.getLogStats().unNamedInvalidCount, 0);

        posList.clearList(false,0);
        assertEquals(posList.getLogStats().unNamedValidCount, 0);
        assertEquals(posList.getLogStats().namedValidCount, 0);
        assertEquals(posList.getLogStats().namedInvalidCount, 1);
        assertEquals(posList.getLogStats().unNamedInvalidCount, 0);

        goToStable(posManager, 1.0, 2.0);
        pCurrent = posManager.getCurrentRecording();
        assertEquals(pCurrent.getName(), "named1");
        assertEquals(posList.getLogStats().unNamedValidCount, 0);
        assertEquals(posList.getLogStats().namedValidCount, 0);
        assertEquals(posList.getLogStats().namedInvalidCount, 1);
        assertEquals(posList.getLogStats().unNamedInvalidCount, 0);
        goToMoving(posManager);
        goToStable(posManager, 3.0, 4.0);
        pCurrent = posManager.getCurrentRecording();
        assertEquals(pCurrent.getName(), "position1");
        goToMoving(posManager);
        goToStable(posManager, 5.0, 6.0);
        pCurrent = posManager.getCurrentRecording();
        assertEquals(pCurrent.getName(), "position2");
        goToMoving(posManager);
        goToStable(posManager, 7.0, 4.0);
        goToMoving(posManager);
        goToStable(posManager, 8.0, 4.0);
        pCurrent = posManager.getCurrentRecording();
        assertEquals(pCurrent.getName(), "position4");
        goToMoving(posManager);
        goToStable(posManager, 9.0, 4.0);
        goToMoving(posManager);
        goToStable(posManager, 8.0, 4.0);
        pCurrent = posManager.getCurrentRecording();
        assertEquals(pCurrent.getName(), "position4");

        printPosList(posList, "later");
        System.out.println(posList.getLogStats().toString());

        assertEquals(posList.getLogStats().validCount, 6);
        assertEquals(posList.getLogStats().invalidatedCount, 1);
        assertEquals(posList.getLogStats().unNamedCount, 5);
        assertEquals(posList.getLogStats().unNamedValidCount, 5);
        assertEquals(posList.getLogStats().unNamedInvalidCount, 0);
        assertEquals(posList.getLogStats().namedCount, 2);
        assertEquals(posList.getLogStats().namedValidCount, 1);
        assertEquals(posList.getLogStats().namedInvalidCount, 1);

        UserParamValuesHandler paramHandler = UserParamValuesHandler.getInstance();

        UserParamValues p = paramHandler.getUserParams();
        p.setMAX_LOG_SIZE(6);
        paramHandler.setUserParams(p);

        assertEquals(posList.getLogStats().validCount, 6);
        assertEquals(posList.getLogStats().invalidatedCount, 1);
        assertEquals(posList.getLogStats().unNamedCount, 5);
        assertEquals(posList.getLogStats().unNamedValidCount, 5);
        assertEquals(posList.getLogStats().unNamedInvalidCount, 0);
        assertEquals(posList.getLogStats().namedCount, 2);
        assertEquals(posList.getLogStats().namedValidCount, 1);
        assertEquals(posList.getLogStats().namedInvalidCount, 1);

        p.setMAX_LOG_SIZE(5);
        paramHandler.setUserParams(p);

        assertEquals(posList.getLogStats().validCount, 5);
        assertEquals(posList.getLogStats().invalidatedCount, 1);
        assertEquals(posList.getLogStats().unNamedCount, 5);
        assertEquals(posList.getLogStats().unNamedValidCount, 5);
        assertEquals(posList.getLogStats().unNamedInvalidCount, 0);
        assertEquals(posList.getLogStats().namedCount, 1);
        assertEquals(posList.getLogStats().namedValidCount, 0);
        assertEquals(posList.getLogStats().namedInvalidCount, 1);

        p.setMAX_LOG_SIZE(4);
        paramHandler.setUserParams(p);

        assertEquals(posList.getLogStats().validCount, 4);
        assertEquals(posList.getLogStats().invalidatedCount, 1);
        assertEquals(posList.getLogStats().unNamedCount, 4);
        assertEquals(posList.getLogStats().unNamedValidCount, 4);
        assertEquals(posList.getLogStats().unNamedInvalidCount, 0);
        assertEquals(posList.getLogStats().namedCount, 1);
        assertEquals(posList.getLogStats().namedValidCount, 0);
        assertEquals(posList.getLogStats().namedInvalidCount, 1);

        goToStable(posManager, 1.0, 2.0);
        pCurrent = posManager.getCurrentRecording();
        assertEquals(pCurrent.getName(), "named1");

        goToMoving(posManager);

        p = paramHandler.getUserParams();
        p.setMAX_LOG_SIZE(250);
        paramHandler.setUserParams(p);

        sendPosition();

    }


    public void sendPosition() throws Exception{

        SupportAndDefinitions.thresholdSteadyTime = 0.1;

        PositionRecordListHandler posList = PositionRecordListHandler.getInstance();
        assertNotNull(posList);
        //posList.init();


        //
        // Clear disk in some different ways..
        posList.clearUnnamed(true);
        posList.clearList(false, 0);
        posList.clearList(true, 0);
        posList.toString();
        assertEquals(posList.recordLengt(),0);

        System.out.println("----\nCURRENT LIST 1\n----");
        System.out.println(posList.toString());

        // Walk through the states
        PositionManager posManager = PositionManager.getInstance();
        assertNotNull(posManager);
        //assertEquals(posManager.getSTATE() == PositionManager.STATES.STARTING, true);

        posManager.handlePosition(1.0d, 2.0d, 100);
        //assertEquals(posManager.getSTATE() == PositionManager.STATES.STARTING, true);
        posManager.handlePosition(1.00001d, 2.00001d, 100);
        //assertEquals(posManager.getSTATE() == PositionManager.STATES.STOPPED, true);



        // Set into stable state
        goToStable(posManager, 1.0, 2.0);
        assertEquals(posList.recordLengt(), 0);
        PositionRecord pCurrent = posManager.getCurrentRecording();
        assertNotNull(pCurrent);
        // Name the stable position
        assertEquals(pCurrent.getName(), "position1");
        posList.renamePositions("position1", "test");
        assertEquals(pCurrent.getName(), "test");
        assertEquals(posList.recordLengt(),0);

        System.out.println("----\nCURRENT LIST 2\n----");
        System.out.println(posList.toString());

        //Now move
        posManager.handlePosition(3.00001d, 4.00001d, 100);
        assertEquals(posManager.getSTATE() == PositionManager.STATES.LEAVING_STABLE, true);
        goToStable(posManager, 3.0, 4.0);
        assertEquals(posList.recordLengt(), 1);

        //Name the new stable position
        pCurrent = posManager.getCurrentRecording();
        assertNotNull(pCurrent);
        assertEquals(pCurrent.getName(), "position1");
        posList.renamePositions("position1", "test2");
        assertEquals(pCurrent.getName(), "test2");

        System.out.println("----\nCURRENT LIST 3\n----");
        System.out.println(posList.toString());

        assertEquals(posList.recordLengt(),1);
        // Clear unnamed...
        posList.clearUnnamed(true);
        assertEquals(posList.recordLengt(), 1);

        // Go back to first position.
        goToStable(posManager, 1.0, 2.0);
        assertEquals(posList.recordLengt(), 2);

        // Make sure we still have the name of the last position....
        pCurrent = posManager.getCurrentRecording();
        assertNotNull(pCurrent);
        assertEquals(pCurrent.getName(), "test");

        System.out.println("----\nCURRENT LIST 4\n----");
        System.out.println(posList.toString());

        //rename positions to same..
        posList.renamePositions("test", "test2");
        posManager.handlePosition(1.0, 2.0, 100);
        //Make sure still stable
        assertEquals(posManager.getSTATE() == PositionManager.STATES.STABLE, true);
        assertEquals(posList.recordLengt(),2);
        pCurrent = posManager.getCurrentRecording();
        assertNotNull(pCurrent);
        assertEquals(pCurrent.getName(), "test2");
        //move to other with same name
        posManager.handlePosition(3.0, 4.0, 100);
        //Make sure still stable
        assertEquals(posManager.getSTATE() == PositionManager.STATES.STABLE, true);
        pCurrent = posManager.getCurrentRecording();
        assertNotNull(pCurrent);
        assertEquals(pCurrent.getName(), "test2");
        assertEquals(posList.recordLengt(), 2);

        System.out.println("----\nCURRENT LIST 5\n----");
        System.out.println(posList.toString());

        // Clear log, but make sure still knows about the two positions during following measurements
        posList.clearList(false, 0);
        assertEquals(posList.recordLengt(), 2);

        System.out.println("----\nCURRENT LIST 6\n----");
        System.out.println(posList.toString());

        posManager.handlePosition(1.0, 2.0, 100);
        //Make sure still stable
        assertEquals(posManager.getSTATE() == PositionManager.STATES.STABLE, true);
        pCurrent = posManager.getCurrentRecording();
        assertNotNull(pCurrent);
        assertEquals(pCurrent.getName(), "test2");
        //move to other with same name
        posManager.handlePosition(3.0, 4.0, 100);
        //Make sure still stable
        assertEquals(posManager.getSTATE() == PositionManager.STATES.STABLE, true);
        pCurrent = posManager.getCurrentRecording();
        assertNotNull(pCurrent);
        assertEquals(pCurrent.getName(), "test2");
        assertEquals(posList.recordLengt(), 2);
        // Jump away...
        goToStable(posManager, 5.0, 6.0);

        System.out.println("----\nCURRENT LIST 7\n----");
        System.out.println(posList.toString());

        pCurrent = posManager.getCurrentRecording();
        assertNotNull(pCurrent);
        assertEquals(pCurrent.getName(), "position1");

        // Jump away...
        goToStable(posManager,1.3, 7.4);

        pCurrent = posManager.getCurrentRecording();
        assertNotNull(pCurrent);
        assertEquals(pCurrent.getName(), "position2");
        assertEquals(posList.recordLengt(), 4);

        System.out.println("----\nCURRENT LIST 8\n----");
        System.out.println(posList.toString());

        // Jump back...
        goToStable(posManager, 1.0, 2.0);
        assertEquals(posList.recordLengt(), 5);

        pCurrent = posManager.getCurrentRecording();
        assertNotNull(pCurrent);
        assertEquals(pCurrent.getName(), "test2");

        // Jump away...
        goToStable(posManager,1.3, 7.4);

        pCurrent = posManager.getCurrentRecording();
        assertNotNull(pCurrent);
        assertEquals(pCurrent.getName(), "position2");

        System.out.println("----\nCURRENT LIST 9\n----");
        System.out.println(posList.toString());

        posList.clearUnnamed(true);
        assertEquals(posList.recordLengt(), 4);
        posList.clearList(true, 0);
        assertEquals(posList.recordLengt(), 0);

        // Jump back...
        goToStable(posManager,1.0, 2.0);

        pCurrent = posManager.getCurrentRecording();
        assertNotNull(pCurrent);
        assertEquals(pCurrent.getName(), "position1");

        System.out.println("----\nCURRENT LIST 10\n----");
        System.out.println(posList.toString());

        // Clear disk in some different ways..
        posList.clearUnnamed(true);
        posList.clearList(false, 0);
        posList.clearList(true, 0);
        posList.toString();

        System.out.println("----\nCURRENT LIST A\n----");
        System.out.println(posList.toString());

        goToStable(posManager,1.0, 0.0);
        posList.renamePositions("position1", "test1");
        goToStable(posManager,2.0, 1.0);
        posList.renamePositions("position1", "test2");
        goToStable(posManager,3.0, 2.0);
        posList.renamePositions("position1", "test3");
        goToStable(posManager,4.0, 3.0);
        posList.renamePositions("position1", "test4");
        goToStable(posManager,5.0, 4.0);
        posList.renamePositions("position1", "test5");
        goToStable(posManager,6.0, 5.0);
        posList.renamePositions("position1", "test6");
        goToStable(posManager,7.0, 6.0);
        goToStable(posManager,8.0, 7.0);

        System.out.println("----\nCURRENT LIST Before autoclear\n----");
        System.out.println(posList.toString());

        posList.autoDeleteLogs(4);

        System.out.println("----\nCURRENT LIST After autoclear 4\n----");
        System.out.println(posList.toString());

        posList.autoDeleteLogs(3);

        System.out.println("----\nCURRENT LIST After autoclear 3\n----");
        System.out.println(posList.toString());

        goToStable(posManager,7.0, 1.0);
        goToStable(posManager,8.0, 1.0);
        goToStable(posManager,9.0, 1.0);

        System.out.println("----\nCURRENT LIST After Adding two more new\n----");
        System.out.println(posList.toString());

        System.exit(0);
    }

    /**
     *
     * @param posManager
     * @param lat
     * @param lon
     * @throws Exception
     */
    private void goToStable(PositionManager posManager, double lat, double lon) throws  Exception{
        posManager.handlePosition(lat, lon, 100);
        posManager.handlePosition(lat, lon, 100);
        posManager.handlePosition(lat, lon, 100);
        posManager.handlePosition(lat, lon, 100);
        posManager.handlePosition(lat, lon, 100);
        posManager.handlePosition(lat, lon, 100);
        Thread.sleep(2000);
        posManager.handlePosition(lat, lon, 100);
        Thread.sleep(2000);
        posManager.handlePosition(lat, lon, 100);
        Thread.sleep(2000);
        posManager.handlePosition(lat, lon, 100);
        Thread.sleep(2000);
        posManager.handlePosition(lat, lon, 100);
        Thread.sleep(2000);
        posManager.handlePosition(lat, lon, 100);
        Thread.sleep(2000);
        posManager.handlePosition(lat, lon, 100);
        assertEquals(posManager.getSTATE() == PositionManager.STATES.STABLE, true);
    }

    private void goToMoving(PositionManager posManager) throws  Exception{
        posManager.handlePosition(1, 2, 100);
        posManager.handlePosition(3, 4, 100);
        posManager.handlePosition(5, 6, 100);
        posManager.handlePosition(7, 8, 100);
        posManager.handlePosition(9, 10, 100);
        posManager.handlePosition(10, 11, 100);
        posManager.handlePosition(12, 13, 100);
        posManager.handlePosition(14, 15, 100);
        assertEquals(posManager.getSTATE() == PositionManager.STATES.MOVING, true);
    }

    private void printPosList(PositionRecordListHandler posList, String text){
        System.out.println("----\nCURRENT LIST "+text+"\n----");
        System.out.println(posList.toString());
    }
}
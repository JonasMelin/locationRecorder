package com.melin.jonas.LocationRecorder.Service;

/**
 * Created by jonas on 2016-03-19.
 *
 * Some general helper functions and all definitions...
 */
public class SupportAndDefinitions {

    public final static boolean DEBUG = false;


    public static double thresholdSteadyTime = 20;  //minutes
    public static final int posUpdateInterval = 120000;  //(ms) between position updates from system
    public static final int filterEffect = 10;  // higher makes low pass filter give less effect on steady position
    public static final float filterEffectAccuracy = 20.0f; //How fast to lower accuracy error for steady pos
    public static final float accuracyMultiplyer = 1.3f; // given accuracy 500m will give stable circle of 500*accuracyMultiplyer
    public static final int thresholdMovedHigher = 600; //meters
    public static final int leaveSteadyHysteresCount = 2;
    private static final int thresholdMovedLower = 500; //meters
    private static final int thresholdMoved = thresholdMovedLower; //meters


    public static int getThresholdMovedLower() {
        return thresholdMovedLower;
    }

    public synchronized static String minutesToHoursAndMinutes(int minutes){
        String returnString ="";
        int hours = minutes / 60; //since both are ints, you get an int
        int min = minutes % 60;

        if(hours > 0){
            returnString = returnString + hours + "h ";
            returnString = returnString + min + "m";
        }else {
            returnString = returnString + min + " min";
        }
        return returnString;
    }

    /**
     * We want to draw a circle with radius thresholdMoved. Within that circle we are concidered stable
     * However, if one of the positions have less accuracy than that, we haveto increase the
     * radius of the steady circle accordingly...  But not higher than thresholdMovedHigher.
     * @param posA
     * @param posB
     * @return
     */
    public synchronized static int getThresholdMovedCurrent(PositionRecord posA, PositionRecord posB) {

        float thresholdMovedCalculated = thresholdMoved;
        float accuracyA = posA.getAccuracy() * accuracyMultiplyer;
        float accuracyB = posB.getAccuracy() * accuracyMultiplyer;

        if(accuracyA > thresholdMovedCalculated)
            thresholdMovedCalculated = accuracyA;
        if(accuracyB > thresholdMovedCalculated)
            thresholdMovedCalculated = accuracyB;

        if(thresholdMovedCalculated > thresholdMovedHigher)
            thresholdMovedCalculated = thresholdMovedHigher;

        return (int)thresholdMovedCalculated;
    }

    /**
     * The steady position will be slightly adjusted by the last position.
     * steady position is both output and input
     * filterEffect parameter adjusts rate (higher makes adjustment slower)
     * @param steady
     * @param last
     */
    public synchronized static void lowPassFilter(PositionRecord steady, PositionRecord last){

        double newLat = 0;
        double newLon = 0;
        //Latitude
        newLat = ((
                (steady.getLocation().getLatitude() * filterEffect) + last.getLocation().getLatitude())/
                (filterEffect + 1));

        //Longitude
        newLon = ((
                (steady.getLocation().getLongitude() * filterEffect) + last.getLocation().getLongitude())/
                (filterEffect + 1));

        steady.updatePosition(newLat, newLon);
        lowPassAccuracy(steady);

    }

    /**
     * If we once measured the accuracy really bad for this steady sector, decrease it slowly.
     * Otherwise once we leave it it may take to long time. Still, the accuracy of the new position
     * will overrule this value if new accuracy is worse.
     * @param p
     */
    private synchronized static void lowPassAccuracy(PositionRecord p){
        float newAccuracy = p.getAccuracy();

        newAccuracy = newAccuracy - filterEffectAccuracy;

        // Note that if accuracy value goes very low, thresholdMovedLower will take
        // effect instead, so alow it to go down to 0...
        if(newAccuracy <= 0.0f);
            newAccuracy = 0.0f;

        p.setAccuracy(newAccuracy);
    }
}

package com.melin.jonas.LocationTracker;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.melin.jonas.LocationRecorder.Test.Tests;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);

        System.out.println("\n\n\nUNIT TEST RUNNING!! \n\n\n");

        Tests t = new Tests();
        try {
            t.test();
        }catch (Exception ex){}
    }
}
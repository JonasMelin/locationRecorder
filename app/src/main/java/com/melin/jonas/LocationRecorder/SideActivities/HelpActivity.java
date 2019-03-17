package com.melin.jonas.LocationRecorder.SideActivities;

import android.annotation.SuppressLint;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.melin.jonas.LocationRecorder.Service.LocationRecorderService;
import com.melin.jonas.LocationRecorder.Service.MyLogger;
import com.melin.jonas.LocationRecorder.Service.UserParamValues;
import com.melin.jonas.LocationTracker.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class HelpActivity extends AppCompatActivity {

    private static String helpText = "\n\n\n\n\n" +
            "1. Disable power save of this app. Otherwise it may \"suddenly stop working\" after a few days.\n" +
            ">>Settings/battery/Information (energy save mode)/disable energy save for location recorder \n\n" +
            "\n-------------------\n" +
            "- Start the time tracking App, the rest is automatic! \n" +
            "\n-------------------\n" +
            "- You will get a report of how much time you spent at work or at the gym \n" +
            "- Your integrity is important! The app can and will not share your positions outside your phone! The log cannot be copy-pasted. Positions are named and never displayed as latitude/longitude! you cannot share anything to facebook ;-)\n" +
            "- Power effective! Using only passive positioning. Leave it running without worrying about draining the battery.\n" +
            "- User tell-back is presented in the Android notification field. Management is performed through the app.\n" +
            "- No adds. 100% free! 100% anonymous\n" +
            "\n" +
            "Usage:\n" +
            " - Start the app. Do nothing. \n" +
            " - The app will print the stable positions in the report view and current status in phone notifications bar.\n" +
            "       - At any time, rename your positions that you want to record and report in the future, e.g. work or gym (By renaming a position to an empty name, those positions will be cleared)\n" +
            "       - Press \"clear unnamed\" to remove dummy positions that you never gave a name.\n" +
            "       - Press \"clear log\" to clear and reset your time report log.\n" +
            "       - Press \"clear all\" to do factory reset of app. You once again must name e.g. work or gym.\n" +
            "\n" +
            "Algorithm:\n" +
            " - Stable positions means: A position of radius 500m (up to 900m in case of bad accuracy) where you spent at least 20 minutes in. If either of these are not met all information about that position will be discarded.\n" +
            "- Hysteres: You may leave a stable position for about 10 minutes and then go back, and it will be handled as one continous recording.\n" +
            "\n" +
            "Known limitations:\n" +
            " - Using only passive positioning means it is designed to be used in urban areas with many network cells, but it will still work also with only one cell, then with less accuracy in the time reports." +
            "";
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private static HelpActivity myself = null;

    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myself = this;
        mContentView = findViewById(R.id.fullscreen_content);
        setContentView(R.layout.activity_help);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.help_ok_button).setOnTouchListener(mDelayHideTouchListener);

        EditText textFieldHelp = (EditText) findViewById(R.id.textViewHelp);

        if (LocationRecorderService.getInstance() != null) {
            textFieldHelp.setText(helpText);
        }

        final Button button12 = (Button) findViewById(R.id.help_ok_button);
        button12.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MyLogger.sysout("HELP CANCEL");

                NavUtils.navigateUpFromSameTask(HelpActivity.getInstance());
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public static HelpActivity getInstance (){
        return myself;
    }
}

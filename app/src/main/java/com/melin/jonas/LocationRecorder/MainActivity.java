package com.melin.jonas.LocationRecorder;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.melin.jonas.LocationRecorder.Service.LocationRecorderService;
import com.melin.jonas.LocationRecorder.SideActivities.ConfirmClearAllActivity;
import com.melin.jonas.LocationRecorder.SideActivities.HelpActivity;
import com.melin.jonas.LocationRecorder.SideActivities.RenameActivity;
import com.melin.jonas.LocationRecorder.SideActivities.SettingsActivity;
import com.melin.jonas.LocationTracker.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static MainActivity myself = null;

    public static MainActivity getInstance(){
        return myself;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myself = this;
        setContentView(R.layout.activity_main_activity_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLogsFromService();
                checkPositioningEnabled();
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, HelpActivity.class));
            //return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        EditText editText = (EditText) findViewById(R.id.newGUI_MainLog);
        editText.setClickable(false);
        editText.setFocusable(false);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    12);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 12: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startService();
                    logReportGui("Access granted to location! Thanks!\n\n" +
                            "" +
                            "Initializing... (Do nothing! The rest is automatic...)");

                } else {

                    logReportGui("Cant do much if you dont give me access to location...");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        myself = this;
        int id = item.getItemId();

        LocationRecorderService theService = LocationRecorderService.getInstance();

        if(theService == null)
            return false;

        if (id == R.id.clear_unnamed) {
            theService.clearUnnamed();
        } else if (id == R.id.clear_all) {
            startActivity(new Intent(MainActivity.this, ConfirmClearAllActivity.class));
        } else if (id == R.id.renme) {
            startActivity(new Intent(MainActivity.this, RenameActivity.class));
        } else if (id == R.id.settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }


        getLogsFromService();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


        return true;
    }

    /**
     * (safely) reads the time report log from the service...
     */
    public synchronized void getLogsFromService(){
        LocationRecorderService s = LocationRecorderService.getInstance();
        if(s != null) {
            logReportGui(s.getReportLogs());
        }
    }

    /**
     * Logs text to the repot text field...
     * @param t
     */
    public synchronized void logReportGui(String t) {


        if(t == "")
            return;

        // Post to UI thread
        final String text = t;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    TextView editText = (TextView) findViewById(R.id.newGUI_MainLog);
                    editText.setText(text);
                } catch (Exception ex) {
                }
            }
        });

    }
    /**
     * Starts the background service if not already started.
     * If started the logs will be retreived...
     */
    private void startService(){
        if(LocationRecorderService.getInstance() == null){
            startForegroundService(new Intent(this, LocationRecorderService.class));
        }else{
            getLogsFromService();
        }
    }

    private void checkPositioningEnabled(){

        LocationRecorderService s = LocationRecorderService.getInstance();
        if((s != null) && (s.checkPositioningEnabled() == false)) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Positioning disabled in phone!\n" +
                    "Please enable positioning through the phone settings menu...\n\n" +
                    "Note: The app uses passive positioning only so don't worry about draining the battery.");
            dialog.show();
        }
    }

    /**
     * When GUI is resumed. Also called first time activity is launched...
     * (re)start the service. Get logs from service..
     */
    @Override
    protected void onResume() {
        super.onResume();
        startService();
        getLogsFromService();
        checkPositioningEnabled();
    }
}

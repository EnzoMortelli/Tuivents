package com.m2a.teamdelta.tuivents;

import android.app.AlertDialog;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.content.DialogInterface;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.PopupWindow;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.SeekBar;
import android.widget.TextView;


public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public Calendar date = new GregorianCalendar(); //initialize Calendar with current date and time. The Calendar object can be used to set dates to search for events on.
    private String today = "heute "; //variable for specialized output. If no date was chosen, the Alertdialog for the case that no events were found will display that there weren't any events today, otherwise just that there weren't any events.
    private String heute;

    private Button btnChangeDate;

    private int year;
    private int month;
    private int day;

    private int hour;
    private int minute;

    static final int DATE_DIALOG_ID = 999;

    private SeekBar bar;

    private Set<Integer> eventIDs;
    private Set<Event> events = new HashSet<Event>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*The following two lines are needed for the app to be able to connect to the database on it's main thread
         *Usually, network stuff can only be done in async threads, which is a bit elaborate, so we do this workaround
         *Changing this to a threaded thing could make a first proposal for improvements*/
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        setCurrentDateOnView();
        addListenerOnButton();

        addListenerOnSeekbar();

    }

    // display current date
    public void setCurrentDateOnView() {

        btnChangeDate = (Button) findViewById(R.id.button_datum);

        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        // set current date into buttonText
        btnChangeDate.setText(new StringBuilder()
                // Month is 0 based, just add 1
                .append(day).append(".").append(month + 1).append(".").append(year).append(" "));

    }

    public void addListenerOnButton() {

        btnChangeDate = (Button) findViewById(R.id.button_datum);

        btnChangeDate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                showDialog(DATE_DIALOG_ID);

            }

        });

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                // set date picker as current date
                return new DatePickerDialog(this, datePickerListener,
                        year, month,day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {

            if (view.isShown()) {

                year = selectedYear;
                month = selectedMonth;
                day = selectedDay;

                // set selected date into buttonText
                btnChangeDate.setText(new StringBuilder()
                        .append(day).append(".").append(month + 1).append(".").append(year).append(" "));

                // set new date for events
                changeDate(year, month, day);
                setUpMap();
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /*
     * Secure shutdown.
     */
    @Override
    public void onBackPressed(){
        //Safety Question if the user presses the Backbutton, to prevent accidental closing
        AlertDialog.Builder back = new AlertDialog.Builder(this);
        back.setTitle("Beenden")
            .setMessage("Möchten Sie die App wirklich schließen?")
            .setPositiveButton("JA", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            })
            .setNegativeButton("NEIN", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // App not gonna get closed yet, do nothing.
                }
            });
        back.show();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //don't show events of previous dates
        mMap.clear();
        events.clear();
        //center the map over the Campus
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.683032, 10.936282), 15.0f), 4000, null);
        //open DB connection
        DBVerbindung db = new DBVerbindung("intelligentgraphics", "intelligentgraph", "247bcaK9YBnPp4F7");
        db.open();
        //get all the events for a given date
        eventIDs = db.getEventsByDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH)+1, date.get(Calendar.DAY_OF_MONTH));

        if(eventIDs.isEmpty()){ //if no events were found for the given date...
            AlertDialog.Builder none = new AlertDialog.Builder(this);
            none.setMessage("Es wurden "+today+"keine Events gefunden.")//...tell the user that there are none
                    .setPositiveButton("Schade", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            none.show();//This thing is fast, it'll propably show up even before the map is fully loaded.
            db.close();
            return;
        }
        //If we found events - add them to the events list and display them
        String start;
        for(Integer ID : eventIDs){
            events.add(new Event(ID, mMap.addMarker(new MarkerOptions()
                                    .position(db.getEventGeoLoc(ID))
                                    .title(db.getEventName(ID))
                                    .snippet(db.getEventLocName(ID)+" | "+db.getEventStart(ID) + " - " + db.getEventEnd(ID))
                                    .alpha(0.5f)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                     )
                            , Integer.valueOf(db.getEventStart(ID).substring(0, 2))
                            , Integer.valueOf(db.getEventStart(ID).substring(3))
                            , Integer.valueOf(db.getEventEnd(ID).substring(0, 2))
                            , Integer.valueOf(db.getEventEnd(ID).substring(3))
                            , db.getEventLocName(ID)
                            , db.getEventDescr(ID)
                            , db.getEventLocDescr(ID)
                       )
            );
        }
        //If we're done, close the Db connection again.
        db.close();
    }

    //May be used to set the date to look for events on
    private void changeDate(int year, int month, int day){
        date.set(year, month, day);
        today = ""; //If the date was changed, we suppose that it's not set to today anymore - even if it still is.
    }

    //May be used to change the time on a given date
    private void changeTime(int hour, int minute){
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
    }

    public void addListenerOnSeekbar() {

        bar = (SeekBar)findViewById(R.id.seekBar);
        bar.setProgress(date.get(Calendar.HOUR_OF_DAY)*60+date.get(Calendar.MINUTE));

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                hour = progress/60;
                minute = progress%60;
                for(Event active : events){
                    if((hour > active.starthour || ( hour == active.starthour && minute >= active.startminute))&&(hour < active.endhour || (hour == active.endhour && minute < active.endhour))){
                        if(!active.active){
                            active.active=true;
                            active.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                            active.marker.setAlpha(1.0f);
                            active.marker.showInfoWindow();
                        }

                    }else{
                        if(active.active){
                            active.active=false;
                            active.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            active.marker.setAlpha(0.5f);
                            active.marker.hideInfoWindow();
                        }

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setSecondaryProgress(seekBar.getProgress()); // set the shade of the previous value.
            }

        });
    }
}

class Event{
    protected int ID;
    protected Marker marker;
    protected int starthour;
    protected int startminute;
    protected int endhour;
    protected int endminute;
    protected String location;
    protected String descr;
    protected String locdescr;
    protected boolean active;


    public Event(int ID, Marker marker, int shour, int smin, int endh, int endm, String location, String descr, String locdescr ){
        this.ID = ID;
        this.marker = marker;
        this.starthour = shour;
        this.startminute = smin;
        this.endhour = endh;
        this.endminute = endm;
        this.location = location;
        this.descr = descr;
        this.locdescr = locdescr;
        this.active = false;
    }

    public int getID(Event event){
        return ID;
    }

    public Marker getMarker(){
        return marker;
    }

    public int getStarthour() { return starthour; }

    public int getStartminute(Event event) { return startminute; }

    public int getEndhour(Event event) { return endhour; }

    public int getEndminute(Event event) { return endminute; }

    public String getLocation(Event event) {
        return location;
    }

    public String getDescr(Event event) {
        return descr;
    }

    public String getLocdescr(Event event) {
        return locdescr;
    }
}
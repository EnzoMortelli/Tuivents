package com.m2a.teamdelta.tuivents;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
/**
 * Created by EnzoMortelli on 20.01.2015.
 */
public class DBVerbindung {
    private final String treiber = "com.mysql.jdbc.Driver";

    private String dBase = "jdbc:mysql://localhost/";
    private String benutzer;
    private String passwort;

    private Connection con;
    private Statement stmt;

    public DBVerbindung(String dBaseH, String benutzerH, String passwortH){
        dBase = dBase + dBaseH;
        benutzer = benutzerH;
        passwort = passwortH;
    }

    public void open(){
        try{
            //lädt datenbanktreiber
            Class.forName(treiber);
            //stelle Verbindung her
            con = DriverManager.getConnection(dBase, benutzer, passwort);
            //erzeuge objekt für Anfragen
            stmt = con.createStatement();
        }
        catch (ClassNotFoundException cnfe){
            System.out.println(cnfe.toString());
        }
        catch (SQLException sqle){
            System.out.println(sqle.toString());
        }
    }

    public void close(){
        try{
            stmt.close();
            con.close();
        }
        catch (SQLException sqle){
            System.out.println(sqle.toString());
        }
    }

    /*
     * Gets the Name of an event by it's ID
     *
     * @param eventID ID of the event of which the name should be requested
     */
    public String getEventName(int eventID){
        String name = "undefined"; //init. Standard for undefined names.
        try {
            ResultSet tmp = stmt.executeQuery("SELECT NAME FROM events WHERE ID="+eventID);
            while(tmp.next()){
                name = tmp.getString("NAME");
            }
        } catch (SQLException sqle) {
            System.out.println(sqle.toString());
        }
        return name;
    }

    /*
     * Gets an event's description by it's ID
     *
     * @param eventID ID of the event to get the description for
     */
    public String getEventDescr(int eventID){
        String descr = "-no description-";//init for an empty description
        try {
            ResultSet tmp = stmt.executeQuery("SELECT DESCR FROM events WHERE ID="+eventID);//get the description
            while(tmp.next()){
                descr = tmp.getString("DESCR");
            }
        } catch (SQLException sqle) {
            System.out.println(sqle.toString());
        }
        return descr;
    }

    /*
     * Gets an event's starting time
     *
     * @param eventID ID of the event to get the starting time for
     */
    public String getEventStart(int eventID){
        String start = ""; //temporary string before formatting
        String time = "00:00"; //init. Undefined start times result in the event starting at midnight.
        try {
            ResultSet tmp = stmt.executeQuery("SELECT START FROM events WHERE ID="+eventID); //get the starting time
            while(tmp.next()){
                start = tmp.getString("START");
            }
            time=(String) start.subSequence(11, start.length()-5);//cutting out hours:minutes
        } catch (SQLException sqle) {
            System.out.println(sqle.toString());
        }
        return time;
    }

    /*
     * Gets an event's ending time
     *
     * @param eventID ID of the event to get the ending time for
     */
    public String getEventEnd(int eventID){
        String end = ""; //temporary string before formatting
        String time = "23:59"; //initializing the result String. Undefined end times end at midnight of the same day.
        try {
            ResultSet tmp = stmt.executeQuery("SELECT END FROM events WHERE ID="+eventID); //get the end time
            while(tmp.next()){
                end = tmp.getString("END"); //save for formatting
            }
            time=end.substring(11, end.length()-5); //cutting out hours:minutes
        } catch (SQLException sqle) {
            System.out.println(sqle.toString());
        }
        return time;
    }

    /*
     * Gets the name of the event location by the event's id
     *
     * @param eventID ID of the event to get the location name for
     */
    public String getEventLocName(int eventID){
        String locname = "N/A";
        try {
            ResultSet tmp = stmt.executeQuery("SELECT locations.NAME FROM events INNER JOIN locations ON events.LOC = locations.ID WHERE events.ID="+eventID);
            while(tmp.next()){
                locname = tmp.getString("NAME");
            }
        } catch (SQLException sqle) {
            System.out.println(sqle.toString());
        }
        return locname;
    }

    /*
     * Gets a description of the event location by the event's id
     *
     * @param eventID ID of the event to get the location's description for
     */
    public String getEventLocDescr(int eventID){
        String locdescr = "-no description-";
        try {
            ResultSet tmp = stmt.executeQuery("SELECT locations.DESCR FROM events INNER JOIN locations ON events.LOC = locations.ID WHERE events.ID="+eventID);
            while(tmp.next()){
                locdescr = tmp.getString("DESCR");
            }
        } catch (SQLException sqle) {
            System.out.println(sqle.toString());
        }
        return locdescr;
    }

    /*
     * Gets the Geolocation in a comfortable format by the event's id
     *
     * @return Location as String in format "<Latitude> <Longitude>".
     * @param eventID ID of the event to get the geolocation for
     */
    public String getEventGeoLoc(int eventID){
        String loc = "50.682774 10.935981"; //Somewhere on the Ehrenbergwiese
        try {
            ResultSet tmp = stmt.executeQuery("SELECT ST_AsText(locations.GEOLOC) FROM events INNER JOIN locations ON events.LOC = locations.ID WHERE events.ID="+eventID); //get the Geolocation as text
            while(tmp.next()){
                loc = tmp.getString(1); //parsing the resultset using index as ST_AsText looses column labels for some reason
            }
            loc=loc.substring(6,loc.length()-1); //cut out the surrounding "POINT(<  >)" to make the String more easily accessible.
        } catch (SQLException sqle) {
            System.out.println(sqle.toString());
        }
        return loc;
    }

    /*
     * Compiles a set of events happening on a certain day, by their event-IDs
     *
     * @param year The Year of the date where we want to find events
     * @param month The Month of the date where we want to find events - starts with 1 for January
     * @param day The Day of the date where we want to find events
     */
    public Set<Integer> getEventsByDate(int year, int month, int day){
        Set<Integer> current = new HashSet<Integer>(); //initialize result set
        try{
            String y = Integer.toString(year); //Getting the params in a string representation
            String m = Integer.toString(month);//That's needed to compare them to strings in the database
            if(month<10){ //Months with only one digit are getting a leading 0 to adhere to the database format
                m = "0"+m;
            }
            String d = Integer.toString(day);//Same for days
            if(day<10){
                d = "0"+d;
            }
            String today = y+"-"+m+"-"+d; //Getting the right date format for the database
            ResultSet events = stmt.executeQuery("SELECT * FROM events WHERE START LIKE '"+today+"%'"); //Get all events where the start time lies on that day
            while(events.next()){ //go through all events on that day
                current.add(events.getInt("ID")); //get all the event IDs into the result set
            }
        }catch(SQLException sqle){
            System.out.println(sqle.toString());
        }
        return current;
    }

    public static void main(){
        DBVerbindung dBVerb1 = new DBVerbindung("tuivents", "root", "M2A2015");
        dBVerb1.open();
        dBVerb1.close();
    }
}



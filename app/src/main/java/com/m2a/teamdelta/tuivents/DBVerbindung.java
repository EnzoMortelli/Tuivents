package com.m2a.teamdelta.tuivents;

import java.sql.*;
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

    public void erstelleVerbindung(){
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

    public void schließeVerbindung(){
        try{
            stmt.close();
            con.close();
        }
        catch (SQLException sqle){
            System.out.println(sqle.toString());
        }
    }

    public static void main(){
        DBVerbindung dBVerb1 = new DBVerbindung("tuivents", "root", "M2A2015");
        dBVerb1.erstelleVerbindung();
        dBVerb1.schließeVerbindung();
    }
}



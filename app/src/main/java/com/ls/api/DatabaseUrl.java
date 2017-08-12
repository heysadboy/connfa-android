package com.ls.api;

/**
 * Created by Varun Kumar on 7/27/2017.
 */

public class DatabaseUrl {


    private volatile static DatabaseUrl uniqueInstance;

    public static String baseUrl = "https://raw.githubusercontent.com/fossasia/open-event/master/sample/FOSSASIA16";
    public static String eventsUrl = baseUrl +  "/event";
    public static String tracksUrl = baseUrl + "/tracks";
    public static String locationUrl = baseUrl + "/microlocations";
    public static String sessionUrl = baseUrl + "/sessions";
    public static String speakerUrl = baseUrl + "/speakers";
    public static String sessionTypes = baseUrl + "/session_types";

    public DatabaseUrl() {
    }

    public static DatabaseUrl getInstance() {
        if (uniqueInstance == null) {
            synchronized (DatabaseUrl.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new DatabaseUrl();
                }
            }
        }
        return uniqueInstance;
    }

    public String getBaseUrl() {return baseUrl; }

    public String getEventsUrl() {
        return eventsUrl;
    }

    public String getLocationUrl(){return locationUrl;}

    public String getSessionurl() {return  sessionUrl;}

    public String getSpeakerUrl() {return speakerUrl;}

    public String getSessionTypes() {return sessionTypes;}

    public String getTracksUrl() {return tracksUrl;}
}
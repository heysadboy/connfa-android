package com.ls.api;

/**
 * Created by Varun Kumar on 7/27/2017.
 */

public class DatabaseUrl {


    private volatile static DatabaseUrl uniqueInstance;

    public static String eventsUrl = "https://raw.githubusercontent.com/fossasia/open-event/master/sample/FOSSASIA16/event";
    public static String tracksUrl = "https://raw.githubusercontent.com/fossasia/open-event/master/sample/FOSSASIA16/tracks";
    public static String locationUrl = "https://raw.githubusercontent.com/fossasia/open-event/master/sample/FOSSASIA16/microlocations";
    public static String sessionUrl = "https://raw.githubusercontent.com/fossasia/open-event/master/sample/FOSSASIA16/sessions";
    public static String speakerUrl = "https://raw.githubusercontent.com/fossasia/open-event/master/sample/FOSSASIA16/speakers";
    public static String sessionTypes = "https://raw.githubusercontent.com/fossasia/open-event/master/sample/FOSSASIA16/session_types";

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

    public String getEventsUrl() {
        return eventsUrl;
    }

    public String getLocationUrl(){return locationUrl;}

    public String getSessionurl() {return  sessionUrl;}

    public String getSpeakerUrl() {return speakerUrl;}

    public String getSessionTypes() {return sessionTypes;}

    public String getTracksUrl() {return tracksUrl;}
}
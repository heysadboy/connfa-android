package com.ls.api;

import android.util.Log;

import com.ls.drupalcon.model.data.Location;
import com.ls.drupalcon.model.data.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Varun Kumar on 7/27/2017.
 */

public class Processor {
    private String output;

    public List<Track> listTrack = new ArrayList<>();
    public List<Location> listLocation = new ArrayList<>();


    public Processor(String output) {
        this.output = output;
    }

    public List<Location> locationProcessor() {

        try {
            JSONArray locations = new JSONArray(output);
            int i;

            Location location;

            for (i = 0; i < locations.length(); i++) {
                JSONObject locationJSONObject = locations.getJSONObject(i);
                location = new Location();

                location.setId(locationJSONObject.getLong("id"));
                location.setName(locationJSONObject.getString("name"));
                location.setAddress("Floor " + locationJSONObject.getLong("floor") + ", " + locationJSONObject.getString("name"));
                location.setLat(locationJSONObject.getDouble("latitude"));
                location.setLon(locationJSONObject.getDouble("longitude"));

                listLocation.add(location);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listLocation;
    }

    public List<Track> trackProcessor() {
        try {
            JSONArray tracks = new JSONArray(output);
            int i;

            Track track;

            for (i = 0; i < tracks.length(); i++) {
                JSONObject tracksJSONObject = tracks.getJSONObject(i);

                track = new Track();
                track.setId(tracksJSONObject.getLong("id"));
                track.setName(tracksJSONObject.getString("name"));

                listTrack.add(track);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listTrack;
    }

}
package com.ls.api;

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


    public Processor(String output) {
        this.output = output;
    }

    public List<Track> trackProcessor()
    {
        try {
            JSONArray tracks = new JSONArray(output);
            int i, j;

            Track track;

            for(i=0;i<tracks.length();i++)
            {
                JSONObject tracksJSONObject = tracks.getJSONObject(i);

                track = new Track();
                track.setId(tracksJSONObject.getLong("id"));
                track.setName(tracksJSONObject.getString("name"));

                listTrack.add(track);
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return listTrack;
    }

}

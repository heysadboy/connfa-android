package com.ls.drupalcon.model.managers;

import com.ls.drupalcon.model.Model;
import com.ls.drupalcon.model.dao.TrackDao;
import com.ls.drupalcon.model.data.Level;
import com.ls.drupalcon.model.data.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TracksManager{

    private TrackDao mTrackDao;


    public TracksManager() {
        mTrackDao = new TrackDao();
    }

    public void setTracks(List<Track> tracks)
    {
        mTrackDao = new TrackDao();
        mTrackDao.saveOrUpdateDataSafe(tracks);
        for (Track track : tracks) {
            if (track != null) {
                if (track.isDeleted()) {
                    mTrackDao.deleteDataSafe(track.getId());
                }
            }
        }
    }

    public List<Track> getTracks() {
        List<Track> tracks = mTrackDao.getAllSafe();
        Collections.sort(tracks, new Comparator<Track>() {
            @Override
            public int compare(Track track, Track track2) {
                return Double.compare(track.getOrder(), track2.getOrder());
            }
        });
        return tracks;
    }

    public Track getTrack(long trackId) {
        List<Track> data = mTrackDao.getDataSafe(trackId);
        return data.size() > 0 ? data.get(0) : null;
    }

    public List<Level> getLevels() {
        LevelsManager levelManager = Model.instance().getLevelsManager();

        List<Level> levels = levelManager.getLevels();
        Collections.sort(levels, new Comparator<Level>() {
            @Override
            public int compare(Level level, Level level2) {
                return Double.compare(level.getOrder(), level2.getOrder());
            }
        });

        return levels;
    }

    public void clear() {
        mTrackDao.deleteAll();
    }

}

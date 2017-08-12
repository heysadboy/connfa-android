package com.ls.drupalcon.model.managers;

import com.ls.drupalcon.app.App;
import com.ls.drupalcon.model.dao.EventDao;
import com.ls.drupalcon.model.dao.SpeakerDao;
import com.ls.drupalcon.model.data.Speaker;

import java.util.List;

public class SpeakerManager {

    private SpeakerDao mSpeakerDao;

    public SpeakerManager() {
        mSpeakerDao = new SpeakerDao(App.getContext());
    }

    public boolean storeResponse(List<Speaker> speakers) {
        if (speakers == null) {
            return false;
        }

        EventDao eventDao = new EventDao(App.getContext());
        mSpeakerDao.saveOrUpdateDataSafe(speakers);
        for (Speaker speaker : speakers) {
            if (speaker != null) {
                if (speaker.isDeleted()) {
                    mSpeakerDao.deleteDataSafe(speaker.getId());
                    eventDao.deleteEventAndSpeakerBySpeaker(speaker.getId());
                }
            }
        }

        return true;
    }

    public List<Speaker> getSpeakers() {
        return mSpeakerDao.selectSpeakersOrderedByName();
    }

    public List<Speaker> getSpeakers(long speakerId) {
        return mSpeakerDao.getDataSafe(speakerId);
    }

    public List<Speaker> getSpeakersByEventId(long eventId) {
        return mSpeakerDao.getSpeakersByEventId(eventId);
    }

    public Speaker getSpeakerById(long id) {
        List<Speaker> speakerById = mSpeakerDao.getSpeakerById(id);
        if (!speakerById.isEmpty()) {
            return speakerById.get(0);
        } else {
            return null;
        }
    }

    public void clear() {
        mSpeakerDao.deleteAll();
    }
}

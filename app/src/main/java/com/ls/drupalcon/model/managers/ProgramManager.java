package com.ls.drupalcon.model.managers;

import com.ls.drupalcon.app.App;
import com.ls.drupalcon.model.dao.EventDao;
import com.ls.drupalcon.model.data.Event;
import com.ls.ui.adapter.item.EventListItem;
import com.ls.utils.DateUtils;

import java.util.Date;
import java.util.List;

public class ProgramManager {

    private EventDao mEventDao;

    public ProgramManager() {
        mEventDao = new EventDao(App.getContext());
    }

    public boolean storeResponse(Event.Holder requestResponse) {
        List<Event.Day> sessions = requestResponse.getDays();
        if (sessions == null) {
            return false;
        }

        List<Long> ids = mEventDao.selectFavoriteEventsSafe();
        for (Event.Day day : sessions) {

            for (Event event : day.getEvents()) {
                if (event != null) {

                    Date date = DateUtils.getInstance().convertEventDayDate(day.getDate());
                    if (date != null) {
                        event.setDate(date);
                    }
                    event.setEventClass(Event.PROGRAM_CLASS);

                    for (long id : ids) {
                        if (event.getId() == id) {
                            event.setFavorite(true);
                            break;
                        }
                    }

                    mEventDao.saveOrUpdateSafe(event);
//                    saveEventSpeakers(event);

                    if (event.isDeleted()) {
                        deleteEvent(event);
                    }
                }
            }
        }
        return true;
    }

    public List<EventListItem> getProgramItemsSafe(int eventClass, long day, List<Long> levelIds, List<Long> trackIds) {
        return mEventDao.selectProgramItemsSafe(eventClass, day, levelIds, trackIds);
    }

    public List<EventListItem> getFavoriteProgramItemsSafe(List<Long> favoriteEventIds, long day) {
        return mEventDao.selectFavoriteProgramItemsSafe(favoriteEventIds, day);
    }

    public void saveEventSpeakers(Event data) {
        Long eventId = data.getId();
        List<Long> speakerEventIds = mEventDao.selectEventSpeakersSafe(eventId);

        for (Long speakerId : data.getSpeakers()) {
            if (!speakerEventIds.contains(speakerId)) {
                mEventDao.insertEventSpeaker(eventId, speakerId);
            }

            speakerEventIds.remove(speakerId);
        }

        //Delete removed speakers
        for (Long speakerId : speakerEventIds) {
            mEventDao.deleteByEventAndSpeaker(eventId, speakerId);
        }
    }

    public void deleteEvent(Event data) {
        mEventDao.deleteDataSafe(data.getId());
        mEventDao.deleteEventAndSpeakerByEvent(data.getId());
        mEventDao.setFavoriteSafe(data.getId(), false);
    }

    public EventDao getEventDao() {
        return mEventDao;
    }
}
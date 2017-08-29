package com.ls.ui.drawer;

import com.ls.api.Processor;
import com.ls.drupalcon.R;
import com.ls.drupalcon.model.UpdateRequest;
import com.ls.drupalcon.model.data.Event;
import com.ls.drupalcon.model.managers.ProgramManager;
import com.ls.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ProgramStrategy implements EventHolderFragmentStrategy {

    private static UpdateRequest request = UpdateRequest.PROGRAMS;

    private List<Long> dayList = new ArrayList<>();

    public ProgramStrategy(String str) {
        Processor processor = new Processor(str);
        List<Event> eventList = processor.eventProcessor();
        dayList = processor.getDayList();
        Collections.sort(dayList);

        ProgramManager programManager = new ProgramManager();
        List<Event.Day> days = new ArrayList<>();
        List<String> ds = processor.getDays();

        int i;
        for (i = 0; i < ds.size(); i++) {

            Event.Day day = new Event.Day();
            day.setDate(ds.get(i));
            day.setEvents(eventList);
            days.add(day);
        }


        Event.Holder response = new Event.Holder();
        response.setDays(days);
        programManager.storeResponse(response);
    }


    @Override
    public List<Long> getDayList() {
        return dayList;
    }

    @Override
    public int getTextResId() {
        return R.string.placeholder_sessions;
    }

    @Override
    public int getImageResId() {
        return R.drawable.ic_no_session;
    }

    @Override
    public boolean enableOptionMenu() {
        return true;
    }

    @Override
    public boolean updateFavorites() {
        return false;
    }

    @Override
    public boolean update(List<UpdateRequest> requests) {
        return requests.contains(request);
    }

    @Override
    public EventMode getEventMode() {
        return EventMode.Program;
    }


}

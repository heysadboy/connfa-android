package com.ls.ui.fragment;

import com.ls.drupalcon.R;
import com.ls.drupalcon.model.EventGenerator;
import com.ls.drupalcon.model.PreferencesManager;
import com.ls.drupalcon.model.data.Event;
import com.ls.sponsors.GoldSponsors;
import com.ls.sponsors.SponsorItem;
import com.ls.sponsors.SponsorManager;
import com.ls.ui.activity.EventDetailsActivity;
import com.ls.ui.adapter.EventsAdapter;
import com.ls.ui.adapter.item.EventListItem;
import com.ls.ui.adapter.item.SimpleTimeRangeCreator;
import com.ls.ui.adapter.item.TimeRangeItem;
import com.ls.ui.drawer.DrawerMenu;
import com.ls.ui.drawer.EventMode;
import com.ls.ui.receiver.ReceiverManager;
import com.ls.utils.AnalyticsManager;
import com.ls.utils.DateUtils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class EventFragment extends Fragment implements EventsAdapter.Listener {

    private static final String EXTRAS_ARG_MODE = "EXTRAS_ARG_MODE";
    private static final String EXTRAS_ARG_DAY = "EXTRAS_ARG_DAY";

    private List<Long> levelIds;
    private List<Long> trackIds;
    private long mDay;

    private EventMode mEventMode;
    private EventsAdapter mAdapter;

    private ListView mListView;
    private ProgressBar mProgressBar;

    private EventGenerator mGenerator;

    private ReceiverManager receiverManager = new ReceiverManager(
            new ReceiverManager.FavoriteUpdatedListener() {
                @Override
                public void onFavoriteUpdated(long eventId, boolean isFavorite) {
                    if (mEventMode != EventMode.Favorites) {
                        new LoadData().execute();
                    }
                }
            });

    public static Fragment newInstance(long day, EventMode mode) {
        Fragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRAS_ARG_MODE, mode);
        args.putLong(EXTRAS_ARG_DAY, day);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_event, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initViews();
        new LoadData().execute();
        receiverManager.register(getActivity());
    }

    @Override
    public void onClick(int position) {
        onItemClick(position);
    }

    @Override
    public void onDestroy() {
        mGenerator.setShouldBreak(true);
        receiverManager.unregister(getActivity());
        super.onDestroy();
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mEventMode = (EventMode) bundle.getSerializable(EXTRAS_ARG_MODE);

            mDay = bundle.getLong(EXTRAS_ARG_DAY, 0);
            levelIds = PreferencesManager.getInstance().loadExpLevel();
            trackIds = PreferencesManager.getInstance().loadTracks();
        }
        mGenerator = new EventGenerator();
    }

    private void initViews() {
        if (getView() != null) {
            mProgressBar = (ProgressBar) getView().findViewById(R.id.progressBar);

            mAdapter = new EventsAdapter(getActivity());
            mAdapter.setOnItemClickListener(this);

            mListView = (ListView) getView().findViewById(R.id.listView);
            mListView.setAdapter(mAdapter);
        }
    }

    class LoadData extends AsyncTask<Void, Void, List<EventListItem>> {

        @Override
        protected List<EventListItem> doInBackground(Void... params) {
            return getEventItems();
        }

        @Override
        protected void onPostExecute(List<EventListItem> result) {
            updateViewsUI(result);
        }
    }

    private void updateViewsUI(final List<EventListItem> eventList) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleEventsResult(eventList);
                }
            });
        }
    }

    private List<EventListItem> getEventItems() {
        List<EventListItem> eventList = new ArrayList<>();

        switch (mEventMode) {
            case Program:
                eventList.addAll(mGenerator.generate(mDay, Event.PROGRAM_CLASS, levelIds, trackIds, new SimpleTimeRangeCreator()));
                break;
            case Bofs:
                eventList.addAll(mGenerator.generate(mDay, Event.BOFS_CLASS, levelIds, trackIds, new SimpleTimeRangeCreator()));
                break;
            case Social:
                eventList.addAll(mGenerator.generate(mDay, Event.SOCIALS_CLASS, levelIds, trackIds, new SimpleTimeRangeCreator()));
                break;
            case Favorites:
                eventList.addAll(mGenerator.generateForFavorites(mDay, new SimpleTimeRangeCreator()));
                break;
        }
        return eventList;
    }

    private void handleEventsResult(List<EventListItem> eventListItems) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }

        mAdapter.setData(eventListItems, mEventMode);
        if (DateUtils.getInstance().isToday(mDay) && mEventMode != EventMode.Favorites) {
            int index = getCurrentTimePosition(eventListItems);
            mListView.setSelection(index);
        }
    }


    private void onItemClick(int position) {
        EventListItem item = mAdapter.getItem(position);
        Event event = item.getEvent();
        if (item.getEvent() != null && item.getEvent().getId() != 0) {
            String eventName = item.getEvent().getName();
            AnalyticsManager.detailsScreenTracker(getActivity(), R.string.event_category, eventName);
            getSponsor();
            EventDetailsActivity.startThisActivity(getActivity(), event.getId(), mDay, true);
        }
    }

    private int getCurrentTimePosition(List<EventListItem> eventListItems) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(DateUtils.getInstance().getTimeZone());
        int deviceTimeMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

        int minDifference = Integer.MAX_VALUE;
        int pos = 0;

        EventListItem eventToSelect = null;

        for (EventListItem item : eventListItems) {

            if (item instanceof TimeRangeItem) {

                Event event = item.getEvent();
                calendar.setTimeInMillis(event.getFromMillis());
                int eventTimeMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

                int difference = Math.abs(eventTimeMinutes - deviceTimeMinutes);

                if (eventTimeMinutes <= deviceTimeMinutes && minDifference > difference) {
                    minDifference = difference;
                    eventToSelect = item;
                }

            }
        }

        if (eventToSelect != null) {
            pos = eventListItems.indexOf(eventToSelect);
        }
        return pos;
    }

    private void getSponsor() {
        Random randomGenerator = new Random();
        List<SponsorItem> sponsorsList = GoldSponsors.getSponsorsList(getContext());
        int randomInt = randomGenerator.nextInt(sponsorsList.size());
        SponsorManager.getInstance().setSponsorId(randomInt);

    }
}

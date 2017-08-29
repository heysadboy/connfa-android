package com.ls.ui.fragment;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.ls.api.AsyncDownloader;
import com.ls.api.DatabaseUrl;
import com.ls.drupalcon.R;
import com.ls.drupalcon.app.App;
import com.ls.drupalcon.model.Model;
import com.ls.drupalcon.model.PreferencesManager;
import com.ls.drupalcon.model.UpdateRequest;
import com.ls.drupalcon.model.UpdatesManager;
import com.ls.ui.activity.HomeActivity;
import com.ls.ui.adapter.BaseEventDaysPagerAdapter;
import com.ls.ui.drawer.BofsStrategy;
import com.ls.ui.drawer.EventHolderFragmentStrategy;
import com.ls.ui.drawer.EventMode;
import com.ls.ui.drawer.FavoritesStrategy;
import com.ls.ui.drawer.ProgramStrategy;
import com.ls.ui.drawer.SocialStrategy;
import com.ls.ui.receiver.ReceiverManager;
import com.ls.utils.DateUtils;
import com.ls.utils.L;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class EventHolderFragment extends Fragment implements AsyncDownloader.JsonDataSetter {

    public static final String TAG = "ProjectsFragment";
    private static final String EXTRAS_ARG_MODE = "EXTRAS_ARG_MODE";

    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerTabs;
    private BaseEventDaysPagerAdapter mAdapter;

    private View mLayoutPlaceholder;
    private ImageView mImageViewNoContent;
    private TextView mTextViewNoContent;

    private boolean mIsFilterUsed;
    private EventHolderFragmentStrategy strategy;
    private List<UpdateRequest> requests = new ArrayList<>();

    private AsyncDownloader downloader;


    private UpdatesManager.DataUpdatedListener updateReceiver = new UpdatesManager.DataUpdatedListener() {
        @Override
        public void onDataUpdated(List<UpdateRequest> requests) {
            L.e("requests = " + EventHolderFragment.this.requests.size());
            updateData(requests);
        }
    };
    private ReceiverManager favoriteReceiver = new ReceiverManager(new ReceiverManager.FavoriteUpdatedListener() {
        @Override
        public void onFavoriteUpdated(long eventId, boolean isFavorite) {
            updateFavorites();
        }
    });

    public static EventHolderFragment newInstance(EventMode eventMode) {
        EventHolderFragment fragment = new EventHolderFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRAS_ARG_MODE, eventMode);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_holder_event, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_filter, menu);

        MenuItem filter = menu.findItem(R.id.actionFilter);
        if (filter != null) {
            updateFilterState(filter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionFilter:
                showFilter();
                break;
        }
        return true;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Model.instance().getUpdatesManager().registerUpdateListener(updateReceiver);
        favoriteReceiver.register(getActivity());

        DatabaseUrl databaseUrl = new DatabaseUrl();
        downloader = new AsyncDownloader(EventHolderFragment.this);
        downloader.execute(databaseUrl.getSessionurl());
    }

    @Override
    public void onDestroyView() {
        Model.instance().getUpdatesManager().unregisterUpdateListener(updateReceiver);
        favoriteReceiver.unregister(getActivity());
        super.onDestroyView();
    }

    private void initData(String str) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            EventMode eventMode = (EventMode) bundle.getSerializable(EXTRAS_ARG_MODE);
            if (eventMode != null) {
                switch (eventMode) {
                    case Program:
                        strategy = new ProgramStrategy(str);
                        break;
                    case Bofs:
                        strategy = new BofsStrategy();
                        break;
                    case Social:
                        strategy = new SocialStrategy();
                        break;
                    case Favorites:
                        strategy = new FavoritesStrategy();
                        break;
                }
            }
        }
    }

    private void initView() {
        View view = getView();
        if (view == null) {
            return;
        }

        mAdapter = new BaseEventDaysPagerAdapter(getChildFragmentManager());
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(mAdapter);

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        mPagerTabs = (PagerSlidingTabStrip) getView().findViewById(R.id.pager_tab_strip);
        mPagerTabs.setTypeface(typeface, 0);
        mPagerTabs.setViewPager(mViewPager);

        mLayoutPlaceholder = view.findViewById(R.id.layout_placeholder);
        mTextViewNoContent = (TextView) view.findViewById(R.id.text_view_placeholder);
        mImageViewNoContent = (ImageView) view.findViewById(R.id.image_view_placeholder);

        setHasOptionsMenu(strategy.enableOptionMenu());

    }

    @Override
    public void setJsonData(String str) {
        initData(str);
        initView();
        new LoadData().execute();
    }

    class LoadData extends AsyncTask<Void, Void, List<Long>> {

        @Override
        protected List<Long> doInBackground(Void... params) {
            return strategy.getDayList();
        }

        @Override
        protected void onPostExecute(List<Long> result) {
            updateViews(result);
        }
    }


    private void updateViews(List<Long> dayList) {

        if (dayList.isEmpty()) {
            mPagerTabs.setVisibility(View.GONE);
            mLayoutPlaceholder.setVisibility(View.VISIBLE);

            if (mIsFilterUsed) {
                mImageViewNoContent.setVisibility(View.GONE);
                mTextViewNoContent.setText(getString(R.string.placeholder_no_matching_events));
            } else {
                mImageViewNoContent.setVisibility(View.VISIBLE);
                mImageViewNoContent.setImageResource(strategy.getImageResId());
                mTextViewNoContent.setText(App.getContext().getText(strategy.getTextResId()));
            }
        } else {
            mLayoutPlaceholder.setVisibility(View.GONE);
            mPagerTabs.setVisibility(View.VISIBLE);
        }

        mAdapter.setData(dayList, strategy);
        switchToCurrentDay(dayList);
    }

    private void switchToCurrentDay(List<Long> days) {
        int item = 0;
        for (Long millis : days) {
            if (DateUtils.getInstance().isToday(millis) ||
                    DateUtils.getInstance().isAfterCurrentFate(millis)) {
                mViewPager.setCurrentItem(item);
                break;
            }
            item++;
        }
    }

    private void showFilter() {
        Activity activity = getActivity();
        if (activity instanceof HomeActivity) {

            if (!((HomeActivity) activity).mFilterDialog.isAdded()) {
                ((HomeActivity) activity).mFilterDialog.show(getActivity().getSupportFragmentManager(), "filter");
            }
        }
    }

    private void updateFilterState(@NotNull MenuItem filter) {
        mIsFilterUsed = false;
        List<Long> levelIds = PreferencesManager.getInstance().loadExpLevel();
        List<Long> trackIds = PreferencesManager.getInstance().loadTracks();

        if (!levelIds.isEmpty() || !trackIds.isEmpty()) {
            mIsFilterUsed = true;
        }

        if (mIsFilterUsed) {
            filter.setIcon(getResources().getDrawable(R.drawable.ic_filter));
        } else {
            filter.setIcon(getResources().getDrawable(R.drawable.ic_filter_empty));
        }
    }

    private void updateData(List<UpdateRequest> requests) {
        if (strategy.update(requests)) {
            new LoadData().execute();
        }
    }


    private void updateFavorites() {
        if (getView() != null) {
            if (strategy.updateFavorites()) {
                new LoadData().execute();
            }
        }
    }
}

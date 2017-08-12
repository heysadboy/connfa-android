package com.ls.ui.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ls.api.AsyncDownloader;
import com.ls.api.DatabaseUrl;
import com.ls.api.Processor;
import com.ls.drupalcon.R;
import com.ls.drupalcon.model.Model;
import com.ls.drupalcon.model.UpdateRequest;
import com.ls.drupalcon.model.UpdatesManager;
import com.ls.drupalcon.model.data.Speaker;
import com.ls.drupalcon.model.managers.SpeakerManager;
import com.ls.ui.activity.SpeakerDetailsActivity;
import com.ls.ui.adapter.SpeakersAdapter;

import java.util.List;

public class SpeakersListFragment extends Fragment
        implements AdapterView.OnItemClickListener, SpeakersAdapter.OnFilterChangeListener, AsyncDownloader.JsonDataSetter {

    public static final String TAG = "SpeakersFragment";
    private View mLayoutContent, mLayoutPlaceholder;
    private SpeakersAdapter mSpeakersAdapter;
    private ListView mListView;
    private TextView mTxtNoSearchResult;

    private String lastSearchRequest;

    private ProgressBar mProgressBar;

    private List<Speaker> speakers;
    private SpeakerManager manager;

    private AsyncDownloader downloader;

    private UpdatesManager.DataUpdatedListener updateListener = new UpdatesManager.DataUpdatedListener() {
        @Override
        public void onDataUpdated(List<UpdateRequest> requests) {
            DatabaseUrl databaseUrl = new DatabaseUrl();
            downloader = new AsyncDownloader(SpeakersListFragment.this);
            downloader.execute(databaseUrl.getSpeakerUrl());
        }
    };

    public static SparseBooleanArray generateFirstLetterPositions(List<Speaker> speakers) {
        SparseBooleanArray positions = new SparseBooleanArray();
        int i = 0;
        String firstNameLetter = "";

        for (Speaker speaker : speakers) {
            String letter = speaker.getFirstName().substring(0, 1).toUpperCase();
            if (firstNameLetter.equals("") || !firstNameLetter.equals(letter)) {
                firstNameLetter = letter;
                positions.put(i, true);
            }
            i++;
        }
        return positions;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_speakers, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DatabaseUrl databaseUrl = new DatabaseUrl();
        downloader = new AsyncDownloader(SpeakersListFragment.this);
        downloader.execute(databaseUrl.getSpeakerUrl());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Model.instance().getUpdatesManager().unregisterUpdateListener(updateListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        initSearchMenuItem(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initSearchMenuItem(Menu menu) {
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.actionSearch).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchText) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String searchedText) {
                if (mSpeakersAdapter != null) {
                    lastSearchRequest = searchedText;
                    mSpeakersAdapter.getFilter().filter(searchedText);
                }
                return true;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mSpeakersAdapter != null) {
            startSpeakersDetailsActivity(mSpeakersAdapter.getItem(position));
        }
    }

    private void updateView(final List<Speaker> speakers) {
        if (isDetached() || getView() == null) {
            return;
        }

        if (speakers == null || speakers.isEmpty()) {
            mLayoutContent.setVisibility(View.GONE);
            mLayoutPlaceholder.setVisibility(View.VISIBLE);
        } else {
            mLayoutContent.setVisibility(View.VISIBLE);
            mLayoutPlaceholder.setVisibility(View.GONE);

            SparseBooleanArray letterPositions = generateFirstLetterPositions(speakers);
            if (mSpeakersAdapter == null) {
                mSpeakersAdapter = new SpeakersAdapter(getActivity(), speakers, letterPositions);
                mSpeakersAdapter.setOnFilterChangeListener(SpeakersListFragment.this);
                mListView.setOnItemClickListener(SpeakersListFragment.this);
                mListView.setAdapter(mSpeakersAdapter);
            } else {
                mSpeakersAdapter.setData(speakers, letterPositions);
            }

            if (!TextUtils.isEmpty(lastSearchRequest)) {
                mSpeakersAdapter.getFilter().filter(lastSearchRequest);
            } else {
                mSpeakersAdapter.notifyDataSetChanged();
            }
        }
        mProgressBar.setVisibility(View.GONE);
    }

    private void startSpeakersDetailsActivity(Speaker speaker) {
        Intent intent = new Intent(getActivity(), SpeakerDetailsActivity.class);
        intent.putExtra(SpeakerDetailsActivity.EXTRA_SPEAKER_ID, speaker.getId());
        intent.putExtra(SpeakerDetailsActivity.EXTRA_SPEAKER, speaker);
        startActivity(intent);
    }

    @Override
    public void onFilterChange(int position) {
        if (position == 0) {
            mTxtNoSearchResult.setVisibility(View.VISIBLE);
        } else {
            mTxtNoSearchResult.setVisibility(View.GONE);
        }
    }

    @Override
    public void setJsonData(String str) {

        Processor processor = new Processor(str);
        speakers = processor.speakerProcessor();
        manager = new SpeakerManager();
        manager.storeResponse(speakers);

        if (getView() == null) {
            return;
        }

        mLayoutContent = getView().findViewById(R.id.layout_content);
        mLayoutPlaceholder = getView().findViewById(R.id.layout_placeholder);
        mProgressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
        mTxtNoSearchResult = (TextView) getView().findViewById(R.id.txtSearchEmpty);
        mListView = (ListView) getView().findViewById(R.id.listSpeakers);

        updateView(manager.getSpeakers());

        downloader.cancel(true);
    }
}

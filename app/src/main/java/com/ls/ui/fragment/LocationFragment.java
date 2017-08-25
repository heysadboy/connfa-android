package com.ls.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ls.api.AsyncDownloader;
import com.ls.api.DatabaseUrl;
import com.ls.api.Processor;
import com.ls.drupalcon.R;
import com.ls.drupalcon.model.Model;
import com.ls.drupalcon.model.UpdateRequest;
import com.ls.drupalcon.model.UpdatesManager;
import com.ls.drupalcon.model.data.Location;
import com.ls.drupalcon.model.managers.LocationManager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationFragment extends Fragment implements CustomMapFragment.OnActivityCreatedListener, AsyncDownloader.JsonDataSetter {

    private static final int ZOOM_LEVEL = 15;
    private static final int TILT_LEVEL = 0;
    private static final int BEARING_LEVEL = 0;
    private Marker previousMarker = null;

    public static final String TAG = "LocationsFragment";
    private GoogleMap mGoogleMap;

    private AsyncDownloader downloader;
    private List<Location> locations;

    private LocationManager locationManager;

    private UpdatesManager.DataUpdatedListener updateListener = new UpdatesManager.DataUpdatedListener() {
        @Override
        public void onDataUpdated(List<UpdateRequest> requests) {
            replaceMapFragment();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_location, null);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Model.instance().getUpdatesManager().unregisterUpdateListener(updateListener);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Model.instance().getUpdatesManager().registerUpdateListener(updateListener);
        replaceMapFragment();
    }

    @Override
    public void onActivityCreated(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        DatabaseUrl databaseUrl = new DatabaseUrl();
        downloader = new AsyncDownloader(LocationFragment.this);
        downloader.execute(databaseUrl.getLocationUrl());
    }

    private void fillMapViews(final List<Location> locations) {

        if (mGoogleMap == null) {
            return;
        }

        if (locations == null || locations.isEmpty()) {
            TextView textViewAddress = (TextView) getView().findViewById(R.id.txtAddress);
            textViewAddress.setText(getString(R.string.placeholder_location));
        }

        for (int i = 0; i < locations.size(); i++) {

            Location location = locations.get(i);
            LatLng position = new LatLng(location.getLat(), location.getLon());

            String locationName = location.getName();
            String address = location.getAddress();
            address = address.replace(",", "\n");

            mGoogleMap.addMarker(new MarkerOptions().position(position)).setTitle(locationName + "#" + address);

            if (i == 0) {
                CameraPosition camPos = new CameraPosition(position, ZOOM_LEVEL, TILT_LEVEL, BEARING_LEVEL);
                mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
                fillTextViews(locationName + "#" + address);
            }
        }

        UiSettings uiSettings = mGoogleMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                String locAddress = marker.getTitle();
                fillTextViews(locAddress);
                if (previousMarker != null) {
                    previousMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                previousMarker = marker;

                return true;
            }
        });
    }

    private void fillTextViews(String locAddress) {
        if (getView() == null) {
            return;
        }

        TextView txtAmsterdam = (TextView) getView().findViewById(R.id.txtPlace);
        TextView txtAddress = (TextView) getView().findViewById(R.id.txtAddress);

        String locationName;
        String address;

        int i = locAddress.indexOf("#");

        locationName = locAddress.substring(0, i);
        address = locAddress.substring(i + 1);
        txtAmsterdam.setText(locationName);
        txtAddress.setText(address);

    }

    private void replaceMapFragment() {
        CustomMapFragment mapFragment = CustomMapFragment.newInstance(LocationFragment.this);
        LocationFragment
                .this.getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentHolder, mapFragment)
                .commitAllowingStateLoss();
    }

    private void hideProgressBar() {
        if (getView() != null) {
            getView().findViewById(R.id.progressBar).setVisibility(View.GONE);
        }
    }

    @Override
    public void setJsonData(String str) {

        Processor processor = new Processor(str);
        locations = processor.locationProcessor();
        locationManager = new LocationManager();
        locationManager.setLocations(locations);

        locations = locationManager.getLocations();

        hideProgressBar();
        fillMapViews(locations);

        downloader.cancel(true);
    }
}

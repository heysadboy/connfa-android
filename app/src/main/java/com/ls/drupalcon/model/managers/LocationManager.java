package com.ls.drupalcon.model.managers;

import com.ls.drupalcon.model.dao.LocationDao;
import com.ls.drupalcon.model.data.Location;

import java.util.List;

public class LocationManager {

    private LocationDao mLocationDao;

    public LocationManager() {
        mLocationDao = new LocationDao();
    }

    public void setLocations(List<Location> locations) {
        mLocationDao = new LocationDao();
        mLocationDao.saveOrUpdateDataSafe(locations);
        for (Location location : locations) {
            if (location != null) {
                if (location.isDeleted()) {
                    mLocationDao.deleteDataSafe(location.getId());
                }
            }
        }
    }

    public List<Location> getLocations() {
        return mLocationDao.getAllSafe();
    }

    public void clear() {
        mLocationDao.deleteAll();
    }
}

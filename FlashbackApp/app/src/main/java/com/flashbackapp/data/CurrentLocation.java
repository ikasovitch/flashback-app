package com.flashbackapp.data;

import android.location.Location;

public class CurrentLocation {
    public static Location getCurrentLocation() {
        return currentLocationGlobal;
    }

    public static void setCurrentLocation(Location currentLocation) {
        currentLocationGlobal.setLatitude(currentLocation.getLatitude());
        currentLocationGlobal.setLongitude(currentLocation.getLongitude());
    }

    private static Location currentLocationGlobal = new Location("currentLocation");

}

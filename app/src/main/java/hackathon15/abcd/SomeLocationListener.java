package hackathon15.abcd;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class SomeLocationListener implements LocationListener {
    public double lat,lon;
    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();
    }
    @Override
    public void onStatusChanged(String s, int i, Bundle b) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}
}


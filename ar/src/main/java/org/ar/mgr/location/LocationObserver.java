
package org.ar.mgr.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.ar.ArContext;
import org.ar.map.ArMap;
import org.ar.mgr.downloader.DownloadManager;

class LocationObserver implements LocationListener {

    private DownloadManager downloadManager;
    private LocationMgrImpl myController;

    public LocationObserver(LocationMgrImpl myController) {
        super();
        this.myController = myController;
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public void setDownloadManager(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    public void onLocationChanged(Location location) {
        Log.d(ArContext.TAG, "Normal Location Changed: " + location.getProvider()
                + " lat: " + location.getLatitude() + " lon: "
                + location.getLongitude() + " alt: "
                + location.getAltitude() + " acc: "
                + location.getAccuracy());
        try {
            addWalkingPathPosition(location);
            deleteAllDownloadActivity();
            Log.v(ArContext.TAG, "Location Changed: " + location.getProvider()
                    + " lat: " + location.getLatitude() + " lon: "
                    + location.getLongitude() + " alt: "
                    + location.getAltitude() + " acc: "
                    + location.getAccuracy());
            myController.setPosition(location);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteAllDownloadActivity() {
        if (downloadManager != null) {
            downloadManager.resetActivity();
        }
    }

    private void addWalkingPathPosition(Location location) {
        ArMap.addWalkingPathPosition(new LatLng((int) (location.getLatitude() * 1E6), (int)
                (location.getLongitude() * 1E6)));
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}

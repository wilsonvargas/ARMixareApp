

package org.ar.data;

import android.location.Location;
import android.util.Log;

import org.ar.ArContext;
import org.ar.ArView;
import org.ar.lib.marker.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * DataHandler is the model which provides the Marker Objects with its data.
 * <p/>
 * DataHandler is also the Factory for new Marker objects.
 */
public class DataHandler {

    // complete marker list
    private List<Marker> markerList = new ArrayList<Marker>();

    public void addMarkers(List<Marker> markers) {

        // Log.v(ArView.TAG, "Marker before: "+markerList.size());
        for (Marker ma : markers) {
            if (!markerList.contains(ma))
                markerList.add(ma);
        }

        Log.v(ArView.TAG, "Marker count: " + markerList.size());
    }

    public void sortMarkerList() {
        Collections.sort(markerList);
    }

    public void updateDistances(Location location) {
        for (Marker ma : markerList) {
            float[] dist = new float[3];
            Location.distanceBetween(ma.getLatitude(), ma.getLongitude(),
                    location.getLatitude(), location.getLongitude(), dist);
            ma.setDistance(dist[0]);
        }
    }

    public void updateActivationStatus(ArContext ArContext) {

        Hashtable<Class, Integer> map = new Hashtable<Class, Integer>();

        for (Marker ma : markerList) {

            Class<? extends Marker> mClass = ma.getClass();
            map.put(mClass, (map.get(mClass) != null) ? map.get(mClass) + 1 : 1);

            boolean belowMax = (map.get(mClass) <= ma.getMaxObjects());
            // boolean dataSourceSelected =
            // ArContext.isDataSourceSelected(ma.getDatasource());

            ma.setActive((belowMax));
        }
    }

    public void onLocationChanged(Location location) {
        updateDistances(location);
        sortMarkerList();
        for (Marker ma : markerList) {
            ma.update(location);
        }
    }

    /**
     * @deprecated Nobody should get direct access to the list
     */
    public List<Marker> getMarkerList() {
        return markerList;
    }

    /**
     * @deprecated Nobody should get direct access to the list
     */
    public void setMarkerList(List<Marker> markerList) {
        this.markerList = markerList;
    }

    public int getMarkerCount() {
        return markerList.size();
    }

    public Marker getMarker(int index) {
        return markerList.get(index);
    }
}

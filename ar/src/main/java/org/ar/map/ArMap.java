

package org.ar.map;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.ar.DataView;
import org.ar.ArContext;
import org.ar.ArListView;
import org.ar.ArView;
import org.ar.R;
import org.ar.data.DataHandler;
import org.ar.data.DataSourceList;
import org.ar.lib.marker.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * This class creates the map view and its overlay. It also adds an overlay with
 * the markers to the map.
 */
public class ArMap extends SupportMapFragment implements OnTouchListener {

    public static final String PREFS_NAME = "ArMapPrefs";
    public static List<Marker> originalMarkerList;
    private static List<Marker> markerList;
    private static DataView dataView;
    private static LatLng startPoint;
    private static List<LatLng> walkingPath = new ArrayList<LatLng>();
    //static ArMap map;
    private static Context thisContext;
    private static TextView searchNotificationTxt;
    private Drawable drawable;
    private ArContext ArContext;
    private MapView mapView;

    /**
     * Adds a position to the walking route.(This route will be drawn on the map)
     */
    public static void addWalkingPathPosition(LatLng latLng) {
        walkingPath.add(latLng);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataView = ArView.getDataView();
        setArContext(dataView.getContext());
        setMarkerList(dataView.getDataHandler().getMarkerList());
        //map = this; //savedInstanceState will save the instance for you.

        setMapContext(getActivity());
        GoogleMapOptions options = new GoogleMapOptions();
        options.zoomControlsEnabled(true);
        setMapView(new MapView(getActivity(), options));
//        setMapView(new MapView(getActivity(), "0bynx7meN9jlSdHQ4-lK_Vzsw-T82UVibnI0nCA"));
//        getMapView().setBuiltInZoomControls(true);
        getMapView().setClickable(true);
        getMapView().getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        getMapView().setEnabled(true);

        getActivity().setContentView(getMapView());

        setStartPoint();
        createOverlay();
        createWalkingPath();

        if (dataView.isFrozen()) {
            searchNotificationTxt = new TextView(getActivity());
            searchNotificationTxt.setWidth(getView().getWidth());
            searchNotificationTxt.setPadding(10, 2, 0, 0);
            searchNotificationTxt.setText(getString(R.string.search_active_1) + " " +
                    DataSourceList.getDataSourcesStringList() + getString(R.string
                    .search_active_2));
            searchNotificationTxt.setBackgroundColor(Color.DKGRAY);
            searchNotificationTxt.setTextColor(Color.WHITE);

            searchNotificationTxt.setOnTouchListener(this);
            getActivity().addContentView(searchNotificationTxt, new LayoutParams(LayoutParams
                    .MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    /**
     * Closes MapView Activity and returns that request to NOT refresh screen by default.
     *
     * @param doRefreshScreen boolean do refresh? true or false
     */
    private void closeMapViewActivity(boolean doRefreshScreen) {
        Intent closeMapView = new Intent();
        closeMapView.putExtra("RefreshScreen", doRefreshScreen);
        getActivity().setResult(Activity.RESULT_OK, closeMapView);
        getActivity().finish();
    }

    /**
     * Closes MapView Activity and returns that request to NOT refresh screen.
     * Default value is false
     */
    private void closeMapViewActivity() {
        closeMapViewActivity(false);
    }

    public void setStartPoint() {
        Location location = getArContext().getLocationFinder().getCurrentLocation();

        double latitude = location.getLatitude() * 1E6;
        double longitude = location.getLongitude() * 1E6;

        startPoint = new LatLng((int) latitude, (int) longitude);

        getMap().animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(startPoint).zoom(15).build()));
    }

    public void createOverlay() {
        BitmapDescriptor iconMap = BitmapDescriptorFactory.fromResource(R.drawable.icon_map);

        for (Marker marker : markerList) {
            if (marker.isActive()) {
                LatLng point = new LatLng((int) (marker.getLatitude() * 1E6), (int) (marker
                        .getLongitude() * 1E6));
                mapView.getMap().addMarker(new MarkerOptions().position(point).icon(iconMap));
            }
        }
        BitmapDescriptor locIcon = BitmapDescriptorFactory.fromResource(R.drawable.loc_icon);
        mapView.getMap().addMarker(new MarkerOptions().position(startPoint).title("Estás aquí")
                .icon(locIcon));
    }

    public void createWalkingPath() {
        if (isPathVisible()) {
            getMap().addPolyline(new PolylineOptions()
                    .color(Color.BLUE)
                    .addAll(walkingPath));
        }
    }

    public void createListView() {
        if (dataView.getDataHandler().getMarkerCount() > 0) {
            Intent intent1 = new Intent(getActivity(), ArListView.class);
            startActivityForResult(intent1, 42);
        }
        /*if the list is empty*/
        else {
            Toast.makeText(getActivity(), R.string.empty_list, Toast.LENGTH_LONG).show();
        }
    }

    private void togglePath() {
        final String property = "pathVisible";
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        boolean result = settings.getBoolean(property, true);
        editor.putBoolean(property, !result);
        editor.commit();
    }

	/* ************ Handlers *************/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO pasar a XML

        int base = Menu.FIRST;
        /*define the first*/

        MenuItem item1 = menu.add(base, base, base, getString(R.string.map_menu_normal_mode));
        MenuItem item2 = menu.add(base, base + 1, base + 1, getString(R.string
                .map_menu_satellite_mode));
        MenuItem item3 = menu.add(base, base + 2, base + 2, getString(R.string.map_my_location));
        MenuItem item4 = menu.add(base, base + 3, base + 3, getString(R.string.menu_item_2));
        MenuItem item5 = menu.add(base, base + 4, base + 4, getString(R.string.map_menu_cam_mode));
        MenuItem item6 = null;
        if (isPathVisible()) {
            item6 = menu.add(base, base + 5, base + 5, getString(R.string.map_toggle_path_off));
        } else {
            item6 = menu.add(base, base + 5, base + 5, getString(R.string.map_toggle_path_on));
        }
        /*assign icons to the menu items*/
        item1.setIcon(android.R.drawable.ic_menu_gallery);
        item2.setIcon(android.R.drawable.ic_menu_mapmode);
        item3.setIcon(android.R.drawable.ic_menu_mylocation);
        item4.setIcon(android.R.drawable.ic_menu_view);
        item5.setIcon(android.R.drawable.ic_menu_camera);
        item6.setIcon(android.R.drawable.ic_menu_directions);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        /*Satellite View*/
            case 1:
                getMapView().getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            /*street View*/
            case 2:
                getMapView().getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            /*go to users location*/
            case 3:
                setStartPoint();
                break;
            /*List View*/
            case 4:
                createListView();
                //finish(); don't close map if list view created
                break;
            /*back to Camera View*/
            case 5:
                closeMapViewActivity();
                break;
            case 6:
                togglePath();
                //refresh:
                startActivity(getActivity().getIntent());
                closeMapViewActivity();
        }
        return true;
    }

    public void startPointMsg() {
        Toast.makeText(getMapContext(), R.string.map_current_location_click, Toast.LENGTH_LONG)
                .show();
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doArSearch(query);
        }
    }

//    @Override
//    public void onNewIntent(Intent intent) {
//        getActivity().setIntent(intent);
//        handleIntent(intent);
//    }

    private void doArSearch(String query) {
        DataHandler jLayer = dataView.getDataHandler();
        if (!dataView.isFrozen()) {
            originalMarkerList = jLayer.getMarkerList();
            ArListView.originalMarkerList = jLayer.getMarkerList();
        }
        markerList = new ArrayList<Marker>();

        for (int i = 0; i < jLayer.getMarkerCount(); i++) {
            Marker ma = jLayer.getMarker(i);

            if (ma.getTitle().toLowerCase().indexOf(query.toLowerCase()) != -1) {
                markerList.add(ma);
            }
        }
        if (markerList.size() == 0) {
            Toast.makeText(getActivity(), getString(R.string.search_failed_notification), Toast
                    .LENGTH_LONG).show();
        } else {
            jLayer.setMarkerList(markerList);
            dataView.setFrozen(true);

            getActivity().finish();
            Intent intent1 = new Intent(getActivity(), ArMap.class);
            startActivityForResult(intent1, 42);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        dataView.setFrozen(false);
        dataView.getDataHandler().setMarkerList(originalMarkerList);

        searchNotificationTxt.setVisibility(View.INVISIBLE);
        searchNotificationTxt = null;
        getActivity().finish();
        Intent intent1 = new Intent(getActivity(), ArMap.class);
        startActivityForResult(intent1, 42);

        return false;
    }

    /**
     * @return the ArContext
     */
    private ArContext getArContext() {
        return ArContext;
    }

    /**
     * @param ArContext the ArContext to set
     */
    private void setArContext(ArContext ArContext) {
        this.ArContext = ArContext;
    }

    /**
     * @return the mapView
     */
    private MapView getMapView() {
        return mapView;
    }

    /**
     * @param mapView the mapView to set
     */
    private void setMapView(MapView mapView) {
        this.mapView = mapView;
    }

    public void setMarkerList(List<Marker> maList) {
        markerList = maList;
    }

    public DataView getDataView() {
        return dataView;
    }

    public Context getMapContext() {
        return thisContext;
    }

    public void setMapContext(Context context) {
        thisContext = context;
    }

    private boolean isPathVisible() {
        final String property = "pathVisible";
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(property, true);
    }
}


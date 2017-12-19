
package org.ar;

/**
 * This class is the main application which uses the other classes for different
 * functionalities.
 * It sets up the camera screen and the augmented screen which is in front of the
 * camera screen.
 * It also handles the main sensor events, touch events and location events.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.ar.data.DataHandler;
import org.ar.data.DataSourceList;
import org.ar.data.DataSourceStorage;
import org.ar.lib.gui.PaintScreen;
import org.ar.lib.marker.Marker;
import org.ar.lib.render.Matrix;
import org.ar.map.ArMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;

public class ArView extends Activity implements SensorEventListener,
        OnTouchListener {

    // TAG for logging
    public static final String TAG = "Ar";
    /* string to name & access the preference file in the internal storage */
    public static final String PREFS_NAME = "MyPrefsFileForMenuItems";
    private static PaintScreen dWindow;
    private static DataView dataView;
    private CameraSurface camScreen;
    private AugmentedView augScreen;
    private boolean isInited;
    private boolean fError;

    // why use Memory to save a state? ArContext? activity lifecycle?
    // private static ArView CONTEXT;
    // ----------
    private ArViewDataHolder ArViewData;
    private SeekBar.OnSeekBarChangeListener myZoomBarOnSeekBarChangeListener = new SeekBar
            .OnSeekBarChangeListener() {
        Toast t;

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            float myout = calcZoomLevel();

            getArViewData().setZoomLevel(String.valueOf(myout));
            getArViewData().setZoomProgress(
                    getArViewData().getMyZoomBar().getProgress());

            t.setText("Radius: " + String.valueOf(myout));
            t.show();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            Context ctx = seekBar.getContext();
            t = Toast.makeText(ctx, "Radius: ", Toast.LENGTH_LONG);
            // zoomChanging= true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            /* store the zoom range of the zoom bar selected by the user */
            editor.putInt("zoomLevel", getArViewData().getMyZoomBar()
                    .getProgress());
            editor.commit();
            getArViewData().getMyZoomBar().setVisibility(View.INVISIBLE);
            // zoomChanging= false;

            getArViewData().getMyZoomBar().getProgress();

            t.cancel();
            // repaint after zoom level changed.
            repaint();
            setZoomLevel();
        }

    };

    /**
     * @return the dWindow
     */
    static PaintScreen getdWindow() {
        return dWindow;
    }

    /**
     * @param dWindow the dWindow to set
     */
    static void setdWindow(PaintScreen dWindow) {
        ArView.dWindow = dWindow;
    }

    /**
     * @return the dataView
     */
    public static DataView getDataView() {
        return dataView;
    }

    /**
     * @param dataView the dataView to set
     */
    static void setDataView(DataView dataView) {
        ArView.dataView = dataView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ArView.CONTEXT = this;
        try {

            handleIntent(getIntent());

            final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            getArViewData().setmWakeLock(
                    pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                            "My Tag"));

            killOnError();
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            maintainCamera();
            maintainAugmentR();
            maintainZoomBar();

            if (!isInited) {
                // getArViewData().setArContext(new ArContext(this));
                // getArViewData().getArContext().setDownloadManager(new
                // DownloadManager(ArViewData.getArContext()));
                setdWindow(new PaintScreen());
                setDataView(new DataView(getArViewData().getArContext()));

				/* set the radius in data view to the last selected by the user */
                setZoomLevel();
                isInited = true;
            }

			/*
             * Get the preference file PREFS_NAME stored in the internal memory
			 * of the phone
			 */
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

			/* check if the application is launched for the first time */
            /*
			 * if(settings.getBoolean("firstAccess",false)==false){
			 * firstAccess(settings);
			 *
			 * }
			 */

        } catch (Exception ex) {
            doError(ex);
        }
    }

	/* ********* Operators ********** */

    public ArViewDataHolder getArViewData() {
        if (ArViewData == null) {
            // TODO: VERY inportant, only one!
            ArViewData = new ArViewDataHolder(new ArContext(this));
        }
        return ArViewData;
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            this.getArViewData().getmWakeLock().release();

            try {
                getArViewData().getSensorMgr().unregisterListener(this,
                        getArViewData().getSensorGrav());
                getArViewData().getSensorMgr().unregisterListener(this,
                        getArViewData().getSensorMag());
                getArViewData().setSensorMgr(null);

                getArViewData().getArContext().getLocationFinder()
                        .switchOff();
                getArViewData().getArContext().getDownloadManager()
                        .switchOff();

                if (getDataView() != null) {
                    getDataView().cancelRefreshTimer();
                }
            } catch (Exception ignore) {
            }

            if (fError) {
                finish();
            }
        } catch (Exception ex) {
            doError(ex);
        }
    }

    /**
     * {@inheritDoc} Ar - Receives results from other launched activities
     * Base on the result returned, it either refreshes screen or not. Default
     * value for refreshing is false
     */
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, Intent data) {
        Log.d(TAG + " WorkFlow", "ArView - onActivityResult Called");
        // check if the returned is request to refresh screen (setting might be
        // changed)
        try {
            if (data.getBooleanExtra("RefreshScreen", false)) {
                Log.d(TAG + " WorkFlow",
                        "ArView - Received Refresh Screen Request .. about to refresh");
                repaint();
                refreshDownload();
            }

        } catch (Exception ex) {
            // do nothing do to mix of return results.
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            this.getArViewData().getmWakeLock().acquire();

            killOnError();
            getArViewData().getArContext().doResume(this);

            repaint();
            getDataView().doStart();
            getDataView().clearEvents();

            getArViewData().getArContext().getDataSourceManager()
                    .refreshDataSources();

            float angleX, angleY;

            int marker_orientation = -90;

            int rotation = Compatibility.getRotation(this);

            // display text from left to right and keep it horizontal
            angleX = (float) Math.toRadians(marker_orientation);
            getArViewData().getM1().set(1f, 0f, 0f, 0f,
                    (float) Math.cos(angleX),
                    (float) -Math.sin(angleX), 0f,
                    (float) Math.sin(angleX),
                    (float) Math.cos(angleX));
            angleX = (float) Math.toRadians(marker_orientation);
            angleY = (float) Math.toRadians(marker_orientation);
            if (rotation == 1) {
                getArViewData().getM2().set(1f, 0f, 0f, 0f,
                        (float) Math.cos(angleX),
                        (float) -Math.sin(angleX), 0f,
                        (float) Math.sin(angleX),
                        (float) Math.cos(angleX));
                getArViewData().getM3().set((float) Math.cos(angleY), 0f,
                        (float) Math.sin(angleY), 0f, 1f, 0f,
                        (float) -Math.sin(angleY), 0f,
                        (float) Math.cos(angleY));
            } else {
                getArViewData().getM2().set((float) Math.cos(angleX), 0f,
                        (float) Math.sin(angleX), 0f, 1f, 0f,
                        (float) -Math.sin(angleX), 0f,
                        (float) Math.cos(angleX));
                getArViewData().getM3().set(1f, 0f, 0f, 0f,
                        (float) Math.cos(angleY),
                        (float) -Math.sin(angleY), 0f,
                        (float) Math.sin(angleY),
                        (float) Math.cos(angleY));

            }

            getArViewData().getM4().toIdentity();

            for (int i = 0; i < getArViewData().getHistR().length; i++) {
                getArViewData().getHistR()[i] = new Matrix();
            }

            getArViewData().setSensorMgr(
                    (SensorManager) getSystemService(SENSOR_SERVICE));

            getArViewData().setSensors(
                    getArViewData().getSensorMgr().getSensorList(
                            Sensor.TYPE_ACCELEROMETER));
            if (getArViewData().getSensors().size() > 0) {
                getArViewData().setSensorGrav(
                        getArViewData().getSensors().get(0));
            }

            getArViewData().setSensors(
                    getArViewData().getSensorMgr().getSensorList(
                            Sensor.TYPE_MAGNETIC_FIELD));
            if (getArViewData().getSensors().size() > 0) {
                getArViewData().setSensorMag(
                        getArViewData().getSensors().get(0));
            }

            getArViewData().getSensorMgr().registerListener(this,
                    getArViewData().getSensorGrav(), SENSOR_DELAY_GAME);
            getArViewData().getSensorMgr().registerListener(this,
                    getArViewData().getSensorMag(), SENSOR_DELAY_GAME);

            try {
                GeomagneticField gmf = getArViewData().getArContext()
                        .getLocationFinder().getGeomagneticField();
                angleY = (float) Math.toRadians(-gmf.getDeclination());
                getArViewData().getM4().set((float) Math.cos(angleY), 0f,
                        (float) Math.sin(angleY), 0f, 1f, 0f,
                        (float) -Math.sin(angleY), 0f,
                        (float) Math.cos(angleY));
            } catch (Exception ex) {
                Log.d("ar", "GPS Initialize Error", ex);
            }

            getArViewData().getArContext().getDownloadManager().switchOn();
            getArViewData().getArContext().getLocationFinder().switchOn();
        } catch (Exception ex) {
            doError(ex);
            try {
                if (getArViewData().getSensorMgr() != null) {
                    getArViewData().getSensorMgr().unregisterListener(this,
                            getArViewData().getSensorGrav());
                    getArViewData().getSensorMgr().unregisterListener(this,
                            getArViewData().getSensorMag());
                    getArViewData().setSensorMgr(null);
                }

                if (getArViewData().getArContext() != null) {
                    getArViewData().getArContext().getLocationFinder()
                            .switchOff();
                    getArViewData().getArContext().getDownloadManager()
                            .switchOff();
                }
            } catch (Exception ignore) {
            }
        }

        Log.d("------------------", "resume");
        if (getDataView().isFrozen()
                && getArViewData().getSearchNotificationTxt() == null) {
            getArViewData().setSearchNotificationTxt(new TextView(this));
            getArViewData().getSearchNotificationTxt().setWidth(
                    getdWindow().getWidth());
            getArViewData().getSearchNotificationTxt().setPadding(10, 2, 0, 0);
            getArViewData().getSearchNotificationTxt().setText(
                    getString(R.string.search_active_1) + " "
                            + DataSourceList.getDataSourcesStringList()
                            + getString(R.string.search_active_2));
            ;
            getArViewData().getSearchNotificationTxt().setBackgroundColor(
                    Color.DKGRAY);
            getArViewData().getSearchNotificationTxt().setTextColor(
                    Color.WHITE);

            getArViewData().getSearchNotificationTxt()
                    .setOnTouchListener(this);
            addContentView(getArViewData().getSearchNotificationTxt(),
                    new LayoutParams(LayoutParams.FILL_PARENT,
                            LayoutParams.WRAP_CONTENT));
        } else if (!getDataView().isFrozen()
                && getArViewData().getSearchNotificationTxt() != null) {
            getArViewData().getSearchNotificationTxt()
                    .setVisibility(View.GONE);
            getArViewData().setSearchNotificationTxt(null);
        }
    }

    /**
     * {@inheritDoc} Customize Activity after switching back to it. Currently it
     * maintain and ensures view creation.
     */
    protected void onRestart() {
        super.onRestart();
        maintainCamera();
        maintainAugmentR();
        maintainZoomBar();

    }

    public void repaint() {
        // clear stored data
        getDataView().clearEvents();
        setDataView(null); // It's smelly code, but enforce garbage collector
        // to release data.
        setDataView(new DataView(ArViewData.getArContext()));
        setdWindow(new PaintScreen());
        // setZoomLevel(); //@TODO Caller has to set the zoom. This function
        // repaints only.
    }

    /**
     * Checks camScreen, if it does not exist, it creates one.
     */
    private void maintainCamera() {
        if (camScreen == null) {
            camScreen = new CameraSurface(this);
        }
        setContentView(camScreen);
    }

    /**
     * Checks augScreen, if it does not exist, it creates one.
     */
    private void maintainAugmentR() {
        if (augScreen == null) {
            augScreen = new AugmentedView(this);
        }
        addContentView(augScreen, new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
    }

    /**
     * Creates a zoom bar and adds it to view.
     */
    private void maintainZoomBar() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        FrameLayout frameLayout = createZoomBar(settings);
        addContentView(frameLayout, new FrameLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM));
    }

    /**
     * Refreshes Download TODO refresh downloads
     */
    private void refreshDownload() {
        // try {
        // if (getArViewData().getDownloadThread() != null){
        // if (!getArViewData().getDownloadThread().isInterrupted()){
        // getArViewData().getDownloadThread().interrupt();
        // getArViewData().getArContext().getDownloadManager().restart();
        // }
        // }else { //if no download thread found
        // getArViewData().setDownloadThread(new Thread(getArViewData()
        // .getArContext().getDownloadManager()));
        // //@TODO Syncronize DownloadManager, call Start instead of run.
        // ArViewData.getArContext().getDownloadManager().run();
        // }
        // }catch (Exception ex){
        // }
    }

	/* ********* Operator - Menu ***** */

    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // int base = Menu.FIRST;
    // /* define the first */
    // MenuItem item1 = menu.add(base, base, base,
    // getString(R.string.menu_item_1));
    // MenuItem item2 = menu.add(base, base + 1, base + 1,
    // getString(R.string.menu_item_2));
    // MenuItem item3 = menu.add(base, base + 2, base + 2,
    // getString(R.string.menu_item_3));
    // MenuItem item4 = menu.add(base, base + 3, base + 3,
    // getString(R.string.menu_item_4));
    // MenuItem item5 = menu.add(base, base + 4, base + 4,
    // getString(R.string.menu_item_5));
    // MenuItem item6 = menu.add(base, base + 5, base + 5,
    // getString(R.string.menu_item_6));
    // MenuItem item7 = menu.add(base, base + 6, base + 6,
    // getString(R.string.menu_item_7));
    //
    // /* assign icons to the menu items */
    // item1.setIcon(drawable.icon_datasource);
    // item2.setIcon(android.R.drawable.ic_menu_view);
    // item3.setIcon(android.R.drawable.ic_menu_mapmode);
    // item4.setIcon(android.R.drawable.ic_menu_zoom);
    // item5.setIcon(android.R.drawable.ic_menu_search);
    // item6.setIcon(android.R.drawable.ic_menu_info_details);
    // item7.setIcon(android.R.drawable.ic_menu_share);
    //
    // return true;
    // }

    public void refresh() {
        dataView.refresh();
    }

	/* ******** Operators - Sensors ****** */

    public void setErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.connection_error_dialog));
        builder.setCancelable(false);

		/* Retry */
        builder.setPositiveButton(R.string.connection_error_dialog_button1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fError = false;
                        // TODO improve
                        try {
                            maintainCamera();
                            maintainAugmentR();
                            repaint();
                            setZoomLevel();
                        } catch (Exception ex) {
                            // Don't call doError, it will be a recursive call.
                            // doError(ex);
                        }
                    }
                });
		/* Open settings */
        builder.setNeutralButton(R.string.connection_error_dialog_button2,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent1 = new Intent(
                                Settings.ACTION_WIRELESS_SETTINGS);
                        startActivityForResult(intent1, 42);
                    }
                });
		/* Close application */
        builder.setNegativeButton(R.string.connection_error_dialog_button3,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0); // wouldn't be better to use finish (to
                        // stop the app normally?)
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public float calcZoomLevel() {

        int myZoomLevel = getArViewData().getMyZoomBar().getProgress();
        float myout = 5;

        if (myZoomLevel <= 26) {
            myout = myZoomLevel / 25f;
        } else if (25 < myZoomLevel && myZoomLevel < 50) {
            myout = (1 + (myZoomLevel - 25)) * 0.38f;
        } else if (25 == myZoomLevel) {
            myout = 1;
        } else if (50 == myZoomLevel) {
            myout = 10;
        } else if (50 < myZoomLevel && myZoomLevel < 75) {
            myout = (10 + (myZoomLevel - 50)) * 0.83f;
        } else {
            myout = (30 + (myZoomLevel - 75) * 2f);
        }

        return myout;
    }

    /**
     * Handle First time users. It display license agreement and store user's
     * acceptance.
     *
     * @param settings where setting is stored
     */
    private void firstAccess(SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(getString(R.string.license));
        builder1.setNegativeButton(getString(R.string.close_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert1 = builder1.create();
        alert1.setTitle(getString(R.string.license_title));
        alert1.show();
        editor.putBoolean("firstAccess", true);

        // value for maximum POI for each selected OSM URL to be active by
        // default is 5
        editor.putInt("osmMaxObject", 5);
        editor.commit();

        // add the default datasources to the preferences file
        DataSourceStorage.getInstance().fillDefaultDataSources();
    }

    /**
     * Create zoom bar and returns FrameLayout. FrameLayout is created to be
     * hidden and not added to view, Caller needs to add the frameLayout to
     * view, and enable visibility when needed.
     *
     * @param settings where setting is stored
     * @return FrameLayout Hidden Zoom Bar
     */
    private FrameLayout createZoomBar(SharedPreferences settings) {
        getArViewData().setMyZoomBar(new SeekBar(this));
        getArViewData().getMyZoomBar().setMax(100);
        getArViewData().getMyZoomBar().setProgress(
                settings.getInt("zoomLevel", 65));
        getArViewData().getMyZoomBar().setOnSeekBarChangeListener(
                myZoomBarOnSeekBarChangeListener);
        getArViewData().getMyZoomBar().setVisibility(View.INVISIBLE);

        FrameLayout frameLayout = new FrameLayout(this);

        frameLayout.setMinimumWidth(3000);
        frameLayout.addView(getArViewData().getMyZoomBar());
        frameLayout.setPadding(10, 0, 10, 10);
        return frameLayout;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
		/* Data sources */
            case 1:
                if (!getDataView().isLauncherStarted()) {
                    Intent intent = new Intent(ArView.this, DataSourceList.class);
                    startActivityForResult(intent, 40);
                } else {
                    Toast.makeText(this, getString(R.string.no_website_available),
                            Toast.LENGTH_LONG).show();
                }
                break;
		/* List view */
            case 2:
			/*
			 * if the list of titles to show in alternative list view is not
			 * empty
			 */
			/*
			 * if (getDataView().getDataHandler().getMarkerCount() > 0) { Intent
			 * intent1 = new Intent(ArView.this, ArListView.class);
			 * startActivityForResult(intent1, 42); } /* if the list is empty
			 */
			/*
			 * else { Toast.makeText(this, R.string.empty_list,
			 * Toast.LENGTH_LONG) .show(); } break;
			 */
                Intent intent = new Intent(
                        android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666," +
                                "45.345"));
                startActivity(intent);
			/* Map View */
            case 3:
                Intent intent2 = new Intent(ArView.this, ArMap.class);
                startActivityForResult(intent2, 20);
                break;
		/* zoom level */
            case 4:
                getArViewData().getMyZoomBar().setVisibility(View.VISIBLE);
                getArViewData().setZoomProgress(
                        getArViewData().getMyZoomBar().getProgress());
                break;
		/* Search */
            case 5:
                onSearchRequested();
                break;
		/* GPS Information */
            case 6:
                Location currentGPSInfo = getArViewData().getArContext()
                        .getLocationFinder().getCurrentLocation();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.general_info_text) + "\n\n"
                        + getString(R.string.longitude)
                        + currentGPSInfo.getLongitude() + "\n"
                        + getString(R.string.latitude)
                        + currentGPSInfo.getLatitude() + "\n"
                        + getString(R.string.altitude)
                        + currentGPSInfo.getAltitude() + "m\n"
                        + getString(R.string.speed) + currentGPSInfo.getSpeed()
                        + "km/h\n" + getString(R.string.accuracy)
                        + currentGPSInfo.getAccuracy() + "m\n"
                        + getString(R.string.gps_last_fix)
                        + new Date(currentGPSInfo.getTime()).toString() + "\n");
                builder.setNegativeButton(getString(R.string.close_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.setTitle(getString(R.string.general_info_title));
                alert.show();
                break;
		/* Case 6: license agreements */
            case 7:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(getString(R.string.license));
			/* Retry */
                builder1.setNegativeButton(getString(R.string.close_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert1 = builder1.create();
                alert1.setTitle(getString(R.string.license_title));
                alert1.show();
                break;

        }
        return true;
    }

    public void onSensorChanged(SensorEvent evt) {
        try {

            if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                getArViewData().getGrav()[0] = evt.values[0];
                getArViewData().getGrav()[1] = evt.values[1];
                getArViewData().getGrav()[2] = evt.values[2];

                augScreen.postInvalidate();
            } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                getArViewData().getMag()[0] = evt.values[0];
                getArViewData().getMag()[1] = evt.values[1];
                getArViewData().getMag()[2] = evt.values[2];

                augScreen.postInvalidate();
            }

            SensorManager.getRotationMatrix(getArViewData().getRTmp(),
                    getArViewData().getI(), getArViewData().getGrav(),
                    getArViewData().getMag());

            int rotation = Compatibility.getRotation(this);

            if (rotation == 1) {
                SensorManager.remapCoordinateSystem(getArViewData().getRTmp(),
                        SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z,
                        getArViewData().getRot());
            } else {
                SensorManager.remapCoordinateSystem(getArViewData().getRTmp(),
                        SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z,
                        getArViewData().getRot());
            }
            getArViewData().getTempR().set(getArViewData().getRot()[0],
                    getArViewData().getRot()[1], getArViewData().getRot()[2],
                    getArViewData().getRot()[3], getArViewData().getRot()[4],
                    getArViewData().getRot()[5], getArViewData().getRot()[6],
                    getArViewData().getRot()[7], getArViewData().getRot()[8]);

            getArViewData().getFinalR().toIdentity();
            getArViewData().getFinalR().prod(getArViewData().getM4());
            getArViewData().getFinalR().prod(getArViewData().getM1());
            getArViewData().getFinalR().prod(getArViewData().getTempR());
            getArViewData().getFinalR().prod(getArViewData().getM3());
            getArViewData().getFinalR().prod(getArViewData().getM2());
            getArViewData().getFinalR().invert();

            getArViewData().getHistR()[getArViewData().getrHistIdx()]
                    .set(getArViewData().getFinalR());
            getArViewData().setrHistIdx(getArViewData().getrHistIdx() + 1);
            if (getArViewData().getrHistIdx() >= getArViewData().getHistR().length)
                getArViewData().setrHistIdx(0);

            getArViewData().getSmoothR().set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,
                    0f);
            for (int i = 0; i < getArViewData().getHistR().length; i++) {
                getArViewData().getSmoothR().add(
                        getArViewData().getHistR()[i]);
            }
            getArViewData().getSmoothR().mult(
                    1 / (float) getArViewData().getHistR().length);

            getArViewData().getArContext().updateSmoothRotation(
                    getArViewData().getSmoothR());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

	/* ************ Handlers ************ */

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        try {
            killOnError();

            float xPress = me.getX();
            float yPress = me.getY();
            if (me.getAction() == MotionEvent.ACTION_UP) {
                getDataView().clickEvent(xPress, yPress);
				/*
				 * Log.e("BAH", "AQUI!!!"); Log.e("BAH", "AQUI!!!");
				 * Log.e("BAH", "AQUI!!!"); Log.e("BAH", "AQUI!!!");
				 * Log.e("BAH", "AQUI!!!"); Log.e("BAH", "AQUI!!!");
				 * Log.e("BAH", "AQUI!!!"); Log.e("BAH", "AQUI!!!");
				 * Log.e("BAH", "AQUI!!!"); Log.e("BAH", "AQUI!!!");
				 * Log.e("BAH", "AQUI!!!"); Log.e("BAH", "AQUI!!!");
				 * Log.e("BAH", "AQUI!!!"); Log.e("BAH", "AQUI!!!");
				 * Log.e("BAH", "AQUI!!!");
				 */
            }// TODO add gesture events (low)

            return true;
        } catch (Exception ex) {
            // doError(ex);
            ex.printStackTrace();
            return super.onTouchEvent(me);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            killOnError();

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (getDataView().isDetailsView()) {
                    getDataView().keyEvent(keyCode);
                    getDataView().setDetailsView(false);
                    return true;
                } else {
                    // TODO handle keyback to finish app correctly
                    return super.onKeyDown(keyCode, event);
                }
            } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                return super.onKeyDown(keyCode, event);
            } else {
                getDataView().keyEvent(keyCode);
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
                && getArViewData().getCompassErrorDisplayed() == 0) {
            for (int i = 0; i < 2; i++) {
                Toast.makeText(getArViewData().getArContext(),
                        "Compass data unreliable. Please recalibrate compass.",
                        Toast.LENGTH_LONG).show();
            }
            getArViewData().setCompassErrorDisplayed(
                    getArViewData().getCompassErrorDisplayed() + 1);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        getDataView().setFrozen(false);
        if (getArViewData().getSearchNotificationTxt() != null) {
            getArViewData().getSearchNotificationTxt()
                    .setVisibility(View.GONE);
            getArViewData().setSearchNotificationTxt(null);
        }
        return false;
    }

    public void doError(Exception ex1) {
        if (!fError) {
            fError = true;

            setErrorDialog();

            ex1.printStackTrace();
            try {
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }

        try {
            augScreen.invalidate();
        } catch (Exception ignore) {
        }
    }

	/* ******* Getter and Setters ********** */

    public void killOnError() throws Exception {
        if (fError)
            throw new Exception();
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doArSearch(query);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void doArSearch(String query) {
        DataHandler jLayer = getDataView().getDataHandler();
        if (!getDataView().isFrozen()) {
            ArListView.originalMarkerList = jLayer.getMarkerList();
            ArMap.originalMarkerList = jLayer.getMarkerList();
        }

        ArrayList<Marker> searchResults = new ArrayList<Marker>();
        Log.d("SEARCH----------------0", "" + query);
        if (jLayer.getMarkerCount() > 0) {
            for (int i = 0; i < jLayer.getMarkerCount(); i++) {
                Marker ma = jLayer.getMarker(i);
                if (ma.getTitle().toLowerCase().indexOf(query.toLowerCase()) != -1) {
                    searchResults.add(ma);
					/* the website for the corresponding title */
                }
            }
        }
        if (searchResults.size() > 0) {
            getDataView().setFrozen(true);
            jLayer.setMarkerList(searchResults);
        } else
            Toast.makeText(this,
                    getString(R.string.search_failed_notification),
                    Toast.LENGTH_LONG).show();
    }

    public boolean isZoombarVisible() {
        return getArViewData().getMyZoomBar() != null
                && getArViewData().getMyZoomBar().getVisibility() == View.VISIBLE;
    }

    public String getZoomLevel() {
        return getArViewData().getZoomLevel();
    }

    public int getZoomProgress() {
        return getArViewData().getZoomProgress();
    }

    private void setZoomLevel() {
        float myout = calcZoomLevel();

        getDataView().setRadius(myout);
        // caller has the to control of zoombar visibility, not setzoom
        // ArViewData.getMyZoomBar().setVisibility(View.INVISIBLE);
        ArViewData.setZoomLevel(String.valueOf(myout));
        // setZoomLevel, caller has to call refreash download if needed.
        // ArViewData.setDownloadThread(new
        // Thread(ArViewData.getArContext().getDownloadManager()));
        // ArViewData.getDownloadThread().start();

        getArViewData().getArContext().getDownloadManager().switchOn();

    }

    ;

}

/**
 * @author daniele
 */
class CameraSurface extends SurfaceView implements SurfaceHolder.Callback {
    ArView app;
    SurfaceHolder holder;
    Camera camera;

    CameraSurface(Context context) {
        super(context);

        try {
            app = (ArView) context;

            holder = getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        } catch (Exception ex) {

        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (camera != null) {
                try {
                    camera.stopPreview();
                } catch (Exception ignore) {
                }
                try {
                    camera.release();
                } catch (Exception ignore) {
                }
                camera = null;
            }

            camera = Camera.open();
            camera.setPreviewDisplay(holder);
        } catch (Exception ex) {
            try {
                if (camera != null) {
                    try {
                        camera.stopPreview();
                    } catch (Exception ignore) {
                    }
                    try {
                        camera.release();
                    } catch (Exception ignore) {
                    }
                    camera = null;
                }
            } catch (Exception ignore) {

            }
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            if (camera != null) {
                try {
                    camera.stopPreview();
                } catch (Exception ignore) {
                }
                try {
                    camera.release();
                } catch (Exception ignore) {
                }
                camera = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            try {
                List<Camera.Size> supportedSizes = null;
                // On older devices (<1.6) the following will fail
                // the camera will work nevertheless
                supportedSizes = Compatibility
                        .getSupportedPreviewSizes(parameters);

                // preview form factor
                float ff = (float) w / h;
                Log.d("Ar", "Screen res: w:" + w + " h:" + h
                        + " aspect ratio:" + ff);

                // holder for the best form factor and size
                float bff = 0;
                int bestw = 0;
                int besth = 0;
                Iterator<Camera.Size> itr = supportedSizes.iterator();

                // we look for the best preview size, it has to be the closest
                // to the screen form factor, and be less wide than the screen itself
                // the latter requirement is because the HTC Hero with update
                // 2.1 will report camera preview sizes larger than the screen, and it
                // will fail to initialize the camera other devices could work with previews
                // larger than the screen though

                while (itr.hasNext()) {
                    Camera.Size element = itr.next();
                    // current form factor
                    float cff = (float) element.width / element.height;
                    // check if the current element is a candidate to replace
                    // the best match so far
                    // current form factor should be closer to the bff
                    // preview width should be less than screen width
                    // preview width should be more than current bestw
                    // this combination will ensure that the highest resolution
                    // will win
                    Log.d("Ar", "Candidate camera element: w:"
                            + element.width + " h:" + element.height
                            + " aspect ratio:" + cff);
                    if ((ff - cff <= ff - bff) && (element.width <= w)
                            && (element.width >= bestw)) {
                        bff = cff;
                        bestw = element.width;
                        besth = element.height;
                    }
                }
                Log.d("Ar", "Chosen camera element: w:" + bestw + " h:"
                        + besth + " aspect ratio:" + bff);
                // Some Samsung phones will end up with bestw and besth = 0
                // because their minimum preview size is bigger then the screen
                // size.
                // In this case, we use the default values: 480x320
                if ((bestw == 0) || (besth == 0)) {
                    Log.d("Ar", "Using default camera parameters!");
                    bestw = 480;
                    besth = 320;
                }
                parameters.setPreviewSize(bestw, besth);
            } catch (Exception ex) {
                parameters.setPreviewSize(480, 320);
            }

            camera.setParameters(parameters);
            camera.startPreview();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class AugmentedView extends View {
    ArView app;
    int xSearch = 200;
    int ySearch = 10;
    int searchObjWidth = 0;
    int searchObjHeight = 0;

    Paint zoomPaint = new Paint();

    public AugmentedView(Context context) {
        super(context);

        try {
            app = (ArView) context;

            app.killOnError();
        } catch (Exception ex) {
            app.doError(ex);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            // if (app.fError) {
            //
            // Paint errPaint = new Paint();
            // errPaint.setColor(Color.RED);
            // errPaint.setTextSize(16);
            //
            // /*Draws the Error code*/
            // canvas.drawText("ERROR: ", 10, 20, errPaint);
            // canvas.drawText("" + app.fErrorTxt, 10, 40, errPaint);
            //
            // return;
            // }

            app.killOnError();

            ArView.getdWindow().setWidth(canvas.getWidth());
            ArView.getdWindow().setHeight(canvas.getHeight());

            ArView.getdWindow().setCanvas(canvas);

            if (!ArView.getDataView().isInited()) {
                ArView.getDataView().init(ArView.getdWindow().getWidth(),
                        ArView.getdWindow().getHeight());
            }
            if (app.isZoombarVisible()) {
                zoomPaint.setColor(Color.WHITE);
                zoomPaint.setTextSize(14);
                String startKM, endKM;
                endKM = "80km";
                startKM = "0km";
				/*
				 * if(ArListView.getDataSource().equals("Twitter")){ startKM =
				 * "1km"; }
				 */
                canvas.drawText(startKM, canvas.getWidth() / 100 * 4,
                        canvas.getHeight() / 100 * 85, zoomPaint);
                canvas.drawText(endKM, canvas.getWidth() / 100 * 99 + 25,
                        canvas.getHeight() / 100 * 85, zoomPaint);

                int height = canvas.getHeight() / 100 * 85;
                int zoomProgress = app.getZoomProgress();
                if (zoomProgress > 92 || zoomProgress < 6) {
                    height = canvas.getHeight() / 100 * 80;
                }
                canvas.drawText(app.getZoomLevel(), (canvas.getWidth()) / 100
                        * zoomProgress + 20, height, zoomPaint);
            }

            ArView.getDataView().draw(ArView.getdWindow());
        } catch (Exception ex) {
            app.doError(ex);
        }
    }
}

/**
 * Internal class that holds ArView field Data.
 *
 * @author A B
 */
class ArViewDataHolder {
    private final ArContext ArContext;
    private float[] RTmp;
    private float[] Rot;
    private float[] I;
    private float[] grav;
    private float[] mag;
    private SensorManager sensorMgr;
    private List<Sensor> sensors;
    private Sensor sensorGrav;
    private Sensor sensorMag;
    private int rHistIdx;
    private Matrix tempR;
    private Matrix finalR;
    private Matrix smoothR;
    private Matrix[] histR;
    private Matrix m1;
    private Matrix m2;
    private Matrix m3;
    private Matrix m4;
    private SeekBar myZoomBar;
    private WakeLock mWakeLock;
    private int compassErrorDisplayed;
    private String zoomLevel;
    private int zoomProgress;
    private TextView searchNotificationTxt;

    public ArViewDataHolder(ArContext ArContext) {
        this.ArContext = ArContext;
        this.RTmp = new float[9];
        this.Rot = new float[9];
        this.I = new float[9];
        this.grav = new float[3];
        this.mag = new float[3];
        this.rHistIdx = 0;
        this.tempR = new Matrix();
        this.finalR = new Matrix();
        this.smoothR = new Matrix();
        this.histR = new Matrix[60];
        this.m1 = new Matrix();
        this.m2 = new Matrix();
        this.m3 = new Matrix();
        this.m4 = new Matrix();
        this.compassErrorDisplayed = 0;
    }

    /* ******* Getter and Setters ********** */
    public ArContext getArContext() {
        return ArContext;
    }

    public float[] getRTmp() {
        return RTmp;
    }

    public void setRTmp(float[] rTmp) {
        RTmp = rTmp;
    }

    public float[] getRot() {
        return Rot;
    }

    public void setRot(float[] rot) {
        Rot = rot;
    }

    public float[] getI() {
        return I;
    }

    public void setI(float[] i) {
        I = i;
    }

    public float[] getGrav() {
        return grav;
    }

    public void setGrav(float[] grav) {
        this.grav = grav;
    }

    public float[] getMag() {
        return mag;
    }

    public void setMag(float[] mag) {
        this.mag = mag;
    }

    public SensorManager getSensorMgr() {
        return sensorMgr;
    }

    public void setSensorMgr(SensorManager sensorMgr) {
        this.sensorMgr = sensorMgr;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public Sensor getSensorGrav() {
        return sensorGrav;
    }

    public void setSensorGrav(Sensor sensorGrav) {
        this.sensorGrav = sensorGrav;
    }

    public Sensor getSensorMag() {
        return sensorMag;
    }

    public void setSensorMag(Sensor sensorMag) {
        this.sensorMag = sensorMag;
    }

    public int getrHistIdx() {
        return rHistIdx;
    }

    public void setrHistIdx(int rHistIdx) {
        this.rHistIdx = rHistIdx;
    }

    public Matrix getTempR() {
        return tempR;
    }

    public void setTempR(Matrix tempR) {
        this.tempR = tempR;
    }

    public Matrix getFinalR() {
        return finalR;
    }

    public void setFinalR(Matrix finalR) {
        this.finalR = finalR;
    }

    public Matrix getSmoothR() {
        return smoothR;
    }

    public void setSmoothR(Matrix smoothR) {
        this.smoothR = smoothR;
    }

    public Matrix[] getHistR() {
        return histR;
    }

    public void setHistR(Matrix[] histR) {
        this.histR = histR;
    }

    public Matrix getM1() {
        return m1;
    }

    public void setM1(Matrix m1) {
        this.m1 = m1;
    }

    public Matrix getM2() {
        return m2;
    }

    public void setM2(Matrix m2) {
        this.m2 = m2;
    }

    public Matrix getM3() {
        return m3;
    }

    public void setM3(Matrix m3) {
        this.m3 = m3;
    }

    public Matrix getM4() {
        return m4;
    }

    public void setM4(Matrix m4) {
        this.m4 = m4;
    }

    public SeekBar getMyZoomBar() {
        return myZoomBar;
    }

    public void setMyZoomBar(SeekBar myZoomBar) {
        this.myZoomBar = myZoomBar;
    }

    public WakeLock getmWakeLock() {
        return mWakeLock;
    }

    public void setmWakeLock(WakeLock mWakeLock) {
        this.mWakeLock = mWakeLock;
    }

    public int getCompassErrorDisplayed() {
        return compassErrorDisplayed;
    }

    public void setCompassErrorDisplayed(int compassErrorDisplayed) {
        this.compassErrorDisplayed = compassErrorDisplayed;
    }

    public String getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(String zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public int getZoomProgress() {
        return zoomProgress;
    }

    public void setZoomProgress(int zoomProgress) {
        this.zoomProgress = zoomProgress;
    }

    public TextView getSearchNotificationTxt() {
        return searchNotificationTxt;
    }

    public void setSearchNotificationTxt(TextView searchNotificationTxt) {
        this.searchNotificationTxt = searchNotificationTxt;
    }
}

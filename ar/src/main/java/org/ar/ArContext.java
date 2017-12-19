
package org.ar;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.ar.lib.ArContextInterface;
import org.ar.lib.render.Matrix;
import org.ar.mgr.datasource.DataSourceManager;
import org.ar.mgr.datasource.DataSourceManagerFactory;
import org.ar.mgr.downloader.DownloadManager;
import org.ar.mgr.downloader.DownloadManagerFactory;
import org.ar.mgr.location.LocationFinder;
import org.ar.mgr.location.LocationFinderFactory;
import org.ar.mgr.webcontent.WebContentManager;
import org.ar.mgr.webcontent.WebContentManagerFactory;

/**
 * Cares about location management and about the data (source, inputstream)
 */
public class ArContext extends ContextWrapper implements ArContextInterface {

    // TAG for logging
    public static final String TAG = "Ar";

    private ArView ArView;

    private Matrix rotationM = new Matrix();

    /**
     * Responsible for all download
     */
    private DownloadManager downloadManager;

    /**
     * Responsible for all location tasks
     */
    private LocationFinder locationFinder;

    /**
     * Responsible for data Source Management
     */
    private DataSourceManager dataSourceManager;

    /**
     * Responsible for Web Content
     */
    private WebContentManager webContentManager;

    public ArContext(ArView appCtx) {
        super(appCtx);
        ArView = appCtx;

        // TODO: RE-ORDER THIS SEQUENCE... IS NECESSARY?
        getDataSourceManager().refreshDataSources();

        if (!getDataSourceManager().isAtLeastOneDatasourceSelected()) {
            rotationM.toIdentity();
        }
        getLocationFinder().switchOn();
        getLocationFinder().findLocation();
    }

    public String getStartUrl() {
        Intent intent = ((Activity) getActualArView()).getIntent();
        if (intent.getAction() != null
                && intent.getAction().equals(Intent.ACTION_VIEW)) {
            return intent.getData().toString();
        } else {
            return "";
        }
    }

    @Override
    public void abrirLugar(int idLugarTuristico) {
        Log.i("ArContent", "Abriendo lugar...");
        Intent intent = new Intent();
        intent.setClassName("com.cocoa.snakeproject",
                "com.cocoa.snakeproject.ui.DetailActivity");
        intent.putExtra("idLugarTuristico", idLugarTuristico);
        startActivity(intent);
        // TODO manejar intent
    }

    public void getRM(Matrix dest) {
        synchronized (rotationM) {
            dest.set(rotationM);
        }
    }

    /**
     * Shows a webpage with the given url when clicked on a marker.
     */
    public void loadArViewWebPage(String url) throws Exception {
        // TODO: CHECK INTERFACE METHOD
        getWebContentManager().loadWebPage(url, getActualArView());
    }

    public void doResume(ArView ArView) {
        setActualArView(ArView);
    }

    public void updateSmoothRotation(Matrix smoothR) {
        synchronized (rotationM) {
            rotationM.set(smoothR);
        }
    }

    public DataSourceManager getDataSourceManager() {
        if (this.dataSourceManager == null) {
            dataSourceManager = DataSourceManagerFactory
                    .makeDataSourceManager(this);
        }
        return dataSourceManager;
    }

    public LocationFinder getLocationFinder() {
        if (this.locationFinder == null) {
            locationFinder = LocationFinderFactory.makeLocationFinder(this);
        }
        return locationFinder;
    }

    public DownloadManager getDownloadManager() {
        if (this.downloadManager == null) {
            downloadManager = DownloadManagerFactory.makeDownloadManager(this);
            getLocationFinder().setDownloadManager(downloadManager);
        }
        return downloadManager;
    }

    public WebContentManager getWebContentManager() {
        if (this.webContentManager == null) {
            webContentManager = WebContentManagerFactory
                    .makeWebContentManager(this);
        }
        return webContentManager;
    }

    public ArView getActualArView() {
        synchronized (ArView) {
            return this.ArView;
        }
    }

    private void setActualArView(ArView mv) {
        synchronized (ArView) {
            this.ArView = mv;
        }
    }

    public ContentResolver getContentResolver() {
        ContentResolver out = super.getContentResolver();
        if (super.getContentResolver() == null) {
            out = getActualArView().getContentResolver();
        }
        return out;
    }

    /**
     * Toast POPUP notification
     *
     * @param string message
     */
    public void doPopUp(final String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }

    /**
     * Toast POPUP notification
     *
     * @param connectionGpsDialogText
     */
    public void doPopUp(int RidOfString) {
        doPopUp(this.getString(RidOfString));
    }
}

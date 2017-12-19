
package org.ar.plugin.connection;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import org.ar.lib.service.IMarkerService;
import org.ar.plugin.PluginConnection;
import org.ar.plugin.PluginNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * A service connector for the marker plugins, this class creates a IMarkerService instance
 * for every loaded plugin. And stores them in a hashmap.
 *
 * @author A. Egal
 */
public class MarkerServiceConnection extends PluginConnection implements ServiceConnection {

    private Map<String, IMarkerService> markerServices = new HashMap<String, IMarkerService>();

    @Override
    public void onServiceDisconnected(ComponentName name) {
        markerServices.clear();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // get instance of the aidl binder
        IMarkerService iMarkerService = IMarkerService.Stub
                .asInterface(service);
        try {
            String markername = iMarkerService.getPluginName();
            markerServices.put(markername, iMarkerService);
            storeFoundPlugin();
        } catch (RemoteException e) {
            throw new PluginNotFoundException(e);
        }
    }

    public Map<String, IMarkerService> getMarkerServices() {
        return markerServices;
    }
}

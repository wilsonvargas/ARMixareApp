package org.ar.lib.service;

import org.ar.lib.render.ArVector;
import android.location.Location;
import android.graphics.Bitmap;
import org.ar.lib.render.Camera;
import org.ar.lib.gui.PaintScreen;
import org.ar.lib.gui.Label;
import org.ar.lib.gui.TextObj;
import org.ar.lib.marker.draw.ParcelableProperty;
import org.ar.lib.marker.draw.PrimitiveProperty;
import org.ar.lib.marker.draw.DrawCommand;
import org.ar.lib.marker.draw.ClickHandler;
/**
 * Android Interface Definition Language for contact between services in different threads,
 * In this case: The IMarkerService connects the ar core with the markers of the plugins.
 */
interface IMarkerService {
    /** Request the process ID of this service. */
    int getPid();
    
    //--other marker interface methods--//
    String buildMarker(int id, String title, double latitude, double longitude, double altitude, String URL, int type, int color);
        
    void removeMarker(String markerName);
    
    String getPluginName();
    
    String getTitle(String markerName);

	String getURL(String markerName);

	double getLatitude(String markerName);

	double getLongitude(String markerName);

	double getAltitude(String markerName);

	ArVector getLocationVector(String markerName);

	void update(String markerName, in Location curGPSFix);

	void calcPaint(String markerName, in Camera viewCam, float addX, float addY);

	DrawCommand[] remoteDraw(String markerName);

	double getDistance(String markerName);

	void setDistance(String markerName, double distance);

	String getID(String markerName);

	void setID(String markerName, String iD);

	boolean isActive(String markerName);

	void setActive(String markerName, boolean active);

	int getColour(String markerName);

	int getMaxObjects(String markerName);

	ClickHandler fClick(String markerName);

	boolean isVisible(String markerName);

	void setExtrasParc(String markerName, String name, in ParcelableProperty parcelableProperty);
	
	void setExtrasPrim(String markerName, String name, in PrimitiveProperty primitiveProperty);
	
	ArVector getCMarker(String markerName);

	ArVector getSignMarker(String markerName);

	boolean getUnderline(String markerName);
	
	void setTxtLab(String markerName, in Label txtLab);
	
	Label getTxtLab(String markerName);
}
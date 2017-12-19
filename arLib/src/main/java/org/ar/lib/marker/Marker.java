
package org.ar.lib.marker;

import org.ar.lib.ArContextInterface;
import org.ar.lib.ArStateInterface;
import org.ar.lib.gui.Label;
import org.ar.lib.gui.PaintScreen;
import org.ar.lib.marker.draw.ParcelableProperty;
import org.ar.lib.marker.draw.PrimitiveProperty;
import org.ar.lib.render.Camera;
import org.ar.lib.render.ArVector;

import android.location.Location;

/**
 * The marker interface.
 * @author A. Egal
 */
public interface Marker extends Comparable<Marker>{

	String getTitle();

	String getURL();

	double getLatitude();

	double getLongitude();

	double getAltitude();

	ArVector getLocationVector();

	void update(Location curGPSFix);

	void calcPaint(Camera viewCam, float addX, float addY);

	void draw(PaintScreen dw);

	double getDistance();

	void setDistance(double distance);

	String getID();

	void setID(String iD);

	boolean isActive();

	void setActive(boolean active);

	int getColour();
	
	public void setTxtLab(Label txtLab);

	Label getTxtLab();

	public boolean fClick(float x, float y, ArContextInterface ctx, ArStateInterface state);

	int getMaxObjects();
	
	void setExtras(String name, ParcelableProperty parcelableProperty);
	
	void setExtras(String name, PrimitiveProperty primitiveProperty);

}
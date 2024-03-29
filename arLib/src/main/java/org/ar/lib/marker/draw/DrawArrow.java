
package org.ar.lib.marker.draw;

import org.ar.lib.ArUtils;
import org.ar.lib.gui.PaintScreen;
import org.ar.lib.render.ArVector;

import android.graphics.Path;
import android.os.Parcel;

/**
 * A draw command that can be send by a plugin marker to draw an arrow on the client.
 * This class extends the DrawCommand, that stores the properties, so that it can be
 * transfered to the client.
 * @author A. Egal
 */
public class DrawArrow extends DrawCommand{

	private static String CLASS_NAME = DrawArrow.class.getName();
	
	private static String PROPERTY_NAME_VISIBLE = "visible";
	private static String PROPERTY_NAME_CMARKER = "cMarker";
	private static String PROPERTY_NAME_SIGNMARKER = "signMarker";
	
	public static DrawArrow init(Parcel in){
		Boolean visible = Boolean.valueOf(in.readString());
		ParcelableProperty cMarkerHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		ParcelableProperty signMarkerHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		return new DrawArrow(visible, (ArVector)cMarkerHolder.getObject(), (ArVector)signMarkerHolder.getObject());
	}
	
	public DrawArrow(boolean visible, ArVector cMarker, ArVector signMarker) {
		super(CLASS_NAME);
		setProperty(PROPERTY_NAME_VISIBLE, visible);
		setProperty(PROPERTY_NAME_CMARKER, cMarker);
		setProperty(PROPERTY_NAME_SIGNMARKER, signMarker);
	}

	/**
	 * The main method that draws the arrow.
	 */
	@Override
	public void draw(PaintScreen dw) {
		if (getBooleanProperty(PROPERTY_NAME_VISIBLE)) {
			ArVector cMarker = getArVectorProperty(PROPERTY_NAME_CMARKER);
			ArVector signMarker = getArVectorProperty(PROPERTY_NAME_SIGNMARKER);
			
			float currentAngle = ArUtils.getAngle(cMarker.x, cMarker.y, signMarker.x, signMarker.y);
			float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

			dw.setStrokeWidth(maxHeight / 10f);
			dw.setFill(false);

			float radius = maxHeight / 1.5f;
			
			Path arrow = buildArrow(maxHeight, radius);			
			dw.paintPath(arrow,cMarker.x,cMarker.y,radius*2,radius*2,currentAngle+90,1);			
		}
	}
	
	private Path buildArrow(float maxHeight, float radius){
		Path arrow = new Path();
		float x=0;
		float y=0;
		arrow.moveTo(x-radius/3, y+radius);
		arrow.lineTo(x+radius/3, y+radius);
		arrow.lineTo(x+radius/3, y);
		arrow.lineTo(x+radius, y);
		arrow.lineTo(x, y-radius);
		arrow.lineTo(x-radius, y);
		arrow.lineTo(x-radius/3,y);
		arrow.close();
		return arrow;
	}

}

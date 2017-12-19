
package org.ar.lib.marker.draw;

import org.ar.lib.gui.PaintScreen;
import org.ar.lib.render.ArVector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcel;
import android.util.Log;

/**
 * A draw command that can be send by a plugin marker to draw an image on the client.
 * This class extends the DrawCommand, that stores the properties, so that it can be
 * transfered to the client.
 * @author A. Egal
 */
public class DrawImage extends DrawCommand{
	
	private static String CLASS_NAME = DrawImage.class.getName();
	
	private static String PROPERTY_NAME_VISIBLE = "visible";
	private static String PROPERTY_NAME_SIGNMARKER = "signMarker";
	private static String PROPERTY_NAME_IMAGE = "image";
	
	public static DrawImage init(Parcel in){
		Boolean visible = Boolean.valueOf(in.readString());
		ParcelableProperty signMarkerHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		ParcelableProperty bitmapHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		return new DrawImage(visible, (ArVector)signMarkerHolder.getObject(), (Bitmap)bitmapHolder.getObject());
	}
	
	public DrawImage(boolean visible,ArVector signMarker, Bitmap image) {
		super(CLASS_NAME);
		setProperty(PROPERTY_NAME_VISIBLE, visible);
		setProperty(PROPERTY_NAME_SIGNMARKER, new ParcelableProperty("org.ar.lib.render.ArVector", signMarker));
		setProperty(PROPERTY_NAME_IMAGE,  new ParcelableProperty("android.graphics.Bitmap",image));
	}
	
	@Override
	public void draw(PaintScreen dw){
		if (getBooleanProperty(PROPERTY_NAME_VISIBLE)) {
			ArVector signMarker = getArVectorProperty(PROPERTY_NAME_SIGNMARKER);
			Bitmap bitmap = getBitmapProperty(PROPERTY_NAME_IMAGE);
			
			dw.setColor(Color.argb(155, 255, 255, 255));
			if(bitmap == null){
				Log.e("ar-lib", "bitmap = null");
				return;
			}
			dw.paintBitmap(bitmap, signMarker.x - (bitmap.getWidth()/2), signMarker.y - (bitmap.getHeight() / 2));
		}
	}	
	
}

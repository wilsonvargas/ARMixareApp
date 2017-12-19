

package org.ar;

import android.location.Location;

import org.ar.lib.gui.PaintScreen;

/**
 * The SocialMarker class represents a marker, which contains data from
 * sources like twitter etc. Social markers appear at the top of the screen
 * and show a small logo of the source.
 *
 * @author hannes
 */
public class SocialMarker extends LocalMarker {

    public static final int MAX_OBJECTS = 15;

    public SocialMarker(String id, String title, double latitude, double longitude,
                        double altitude, String URL, int type, int color) {
        super(id, title, latitude, longitude, altitude, URL, type, color);
    }

    @Override
    public void update(Location curGPSFix) {

        //0.35 radians ~= 20 degree
        //0.85 radians ~= 45 degree
        //minAltitude = sin(0.35)
        //maxAltitude = sin(0.85)

        // we want the social markers to be on the upper part of
        // your surrounding sphere
        double altitude = curGPSFix.getAltitude() + Math.sin(0.35) * distance + Math.sin(0.4) *
                (distance / (ArView.getDataView().getRadius() * 1000f / distance));
        mGeoLoc.setAltitude(altitude);
        super.update(curGPSFix);

    }

    @Override
    public void draw(PaintScreen dw) {

        drawTextBlock(dw);

        if (isVisible) {
            float maxHeight = Math.round(dw.getHeight() / 10f) + 1;
            //Bitmap bitmap = BitmapFactory.decodeResource(ArContext.getResources(), DataSource
            // .getDataSourceIcon());
//			if(bitmap!=null) {
//				dw.paintBitmap(bitmap, cMarker.x - maxHeight/1.5f, cMarker.y - maxHeight/1.5f);
//			}
//			else {
            dw.setStrokeWidth(maxHeight / 10f);
            dw.setFill(false);
            //dw.setColor(DataSource.getColor(type));
            dw.paintCircle(cMarker.x, cMarker.y, maxHeight / 1.5f);
            //}
        }
    }

    @Override
    public int getMaxObjects() {
        return MAX_OBJECTS;
    }

}

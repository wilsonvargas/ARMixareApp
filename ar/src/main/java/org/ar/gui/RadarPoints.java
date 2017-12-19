
package org.ar.gui;

import android.graphics.Color;

import org.ar.DataView;
import org.ar.data.DataHandler;
import org.ar.lib.gui.PaintScreen;
import org.ar.lib.gui.ScreenObj;
import org.ar.lib.marker.Marker;

/**
 * Takes care of the small radar in the top left corner and of its points
 *
 * @author daniele
 */
public class RadarPoints implements ScreenObj {
    /**
     * Radius in pixel on screen
     */
    public static float RADIUS = 40;
    /**
     * Position on screen
     */
    static float originX = 0, originY = 0;
    /**
     * Color
     */
    static int radarColor = Color.argb(100, 0, 0, 200);
    /**
     * The screen
     */
    public DataView view;
    /**
     * The radar's range
     */
    float range;

    public void paint(PaintScreen dw) {
        /** radius is in KM. */
        range = view.getRadius() * 1000;
        /** Draw the radar */
        dw.setFill(true);
        dw.setColor(radarColor);
        dw.paintCircle(originX + RADIUS, originY + RADIUS, RADIUS);

        /** put the markers in it */
        float scale = range / RADIUS;

        DataHandler jLayer = view.getDataHandler();

        for (int i = 0; i < jLayer.getMarkerCount(); i++) {
            Marker pm = jLayer.getMarker(i);
            float x = pm.getLocationVector().x / scale;
            float y = pm.getLocationVector().z / scale;

            if (pm.isActive() && (x * x + y * y < RADIUS * RADIUS)) {
                dw.setFill(true);

                // For OpenStreetMap the color is changing based on the URL
                dw.setColor(pm.getColour());

                dw.paintRect(x + RADIUS - 1, y + RADIUS - 1, 2, 2);
            }
        }
    }

    /**
     * Width on screen
     */
    public float getWidth() {
        return RADIUS * 2;
    }

    /**
     * Height on screen
     */
    public float getHeight() {
        return RADIUS * 2;
    }
}


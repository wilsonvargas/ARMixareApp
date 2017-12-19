
package org.ar;

import org.ar.lib.ArContextInterface;
import org.ar.lib.ArStateInterface;
import org.ar.lib.ArUtils;
import org.ar.lib.render.ArVector;
import org.ar.lib.render.Matrix;

/**
 * This class calculates the bearing and pitch out of the angles
 */
public class ArState implements ArStateInterface {

    public static int NOT_STARTED = 0;
    public static int PROCESSING = 1;
    public static int READY = 2;
    public static int DONE = 3;

    int nextLStatus = ArState.NOT_STARTED;
    String downloadId;

    private float curBearing;
    private float curPitch;

    private boolean detailsView;

    public boolean handleEvent(ArContextInterface ctx, String onPress) {
        if (onPress != null && onPress.startsWith("webpage")) {
            try {
                String webpage = ArUtils.parseAction(onPress);
                this.detailsView = true;
                ctx.loadArViewWebPage(webpage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }

    public float getCurBearing() {
        return curBearing;
    }

    public float getCurPitch() {
        return curPitch;
    }

    public boolean isDetailsView() {
        return detailsView;
    }

    public void setDetailsView(boolean detailsView) {
        this.detailsView = detailsView;
    }

    public void calcPitchBearing(Matrix rotationM) {
        ArVector looking = new ArVector();
        rotationM.transpose();
        looking.set(1, 0, 0);
        looking.prod(rotationM);
        this.curBearing = (int) (ArUtils.getAngle(0, 0, looking.x, looking.z) + 360) % 360;

        rotationM.transpose();
        looking.set(0, 1, 0);
        looking.prod(rotationM);
        this.curPitch = -ArUtils.getAngle(0, 0, looking.y, looking.z);
    }
}

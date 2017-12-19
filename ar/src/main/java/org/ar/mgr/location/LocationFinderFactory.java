
package org.ar.mgr.location;

import org.ar.ArContext;

/**
 * Factory Of  LocationFinder
 */
public class LocationFinderFactory {

    /**
     * Hide implementation Of LocationFinder
     *
     * @param ArContext
     * @return LocationFinder
     */
    public static LocationFinder makeLocationFinder(ArContext ArContext) {
        return new LocationMgrImpl(ArContext);
    }

}

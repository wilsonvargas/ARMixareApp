
package org.ar.mgr.webcontent;

import org.ar.ArContext;

/**
 * Factory Of  WebContentManager
 */
public class WebContentManagerFactory {
    /**
     * Hide implementation Of WebContentManager
     *
     * @param ArContext
     * @return WebContentManager
     */
    public static WebContentManager makeWebContentManager(ArContext ArContext) {
        return new WebPageMgrImpl(ArContext);
    }

}

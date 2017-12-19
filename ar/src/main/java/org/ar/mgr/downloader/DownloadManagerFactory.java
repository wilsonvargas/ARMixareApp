
package org.ar.mgr.downloader;

import org.ar.ArContext;

/**
 * Factory Of DownloadManager
 */
public class DownloadManagerFactory {

    /**
     * Hide implementation Of DownloadManager
     *
     * @param ArContext
     * @return DownloadManager
     */
    public static DownloadManager makeDownloadManager(ArContext ArContext) {
        return new DownloadMgrImpl(ArContext);
    }
}


package org.ar.mgr.webcontent;

import android.content.Context;

/**
 * This class is repsonsible for Web Content
 */
public interface WebContentManager {

    /**
     * Shows a webpage with the given url if a markerobject is selected
     * (arlistview, aroverlay).
     */
    void loadWebPage(String url, Context context) throws Exception;

    /**
     * Checks if the url can be opened by another intent activity, instead of
     * the webview This method searches for possible intents that can be used
     * instead. I.E. a mp3 file can be forwarded to a mediaplayer.
     *
     * @param url  the url to process
     * @param view
     * @return
     */
    boolean processUrl(String url, Context ctx);

}
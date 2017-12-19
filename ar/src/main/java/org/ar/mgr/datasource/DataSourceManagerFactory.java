
package org.ar.mgr.datasource;

import org.ar.ArContext;

/**
 * Factory Of DataSourceManager
 */
public class DataSourceManagerFactory {
    /**
     * Hide implementation Of DataSourceManager
     *
     * @param ctx context
     * @return DataSourceManager
     */
    public static DataSourceManager makeDataSourceManager(ArContext ctx) {
        return new DataSourceMgrImpl(ctx);
    }
}

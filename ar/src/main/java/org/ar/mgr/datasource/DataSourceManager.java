
package org.ar.mgr.datasource;

import org.ar.data.DataSource;

/**
 * This class is responsible for Data Source Managing
 */
public interface DataSourceManager {

    /**
     * Is At Least One Datasource Selected
     *
     * @return boolean
     */
    boolean isAtLeastOneDatasourceSelected();

    /**
     * Sync DataSouceManager with DataSourceStorage.
     */
    void refreshDataSources();

    /**
     * Clean all old datasource and insert only source.
     *
     * @param source
     */
    void setAllDataSourcesforLauncher(DataSource source);

    /**
     * send command to download data information from datasource
     *
     * @param lat
     * @param lon
     * @param alt
     * @param radius
     */
    void requestDataFromAllActiveDataSource(double lat, double lon, double alt,
                                            float radius);

}

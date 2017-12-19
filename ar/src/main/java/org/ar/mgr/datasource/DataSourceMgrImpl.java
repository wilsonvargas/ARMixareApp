
package org.ar.mgr.datasource;

import org.ar.ArContext;
import org.ar.data.DataSource;
import org.ar.data.DataSourceStorage;
import org.ar.mgr.downloader.DownloadRequest;

import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

class DataSourceMgrImpl implements DataSourceManager {

    private final ConcurrentLinkedQueue<DataSource> allDataSources = new
            ConcurrentLinkedQueue<DataSource>();

    private final ArContext ctx;

    public DataSourceMgrImpl(ArContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public boolean isAtLeastOneDatasourceSelected() {
        boolean atLeastOneDatasourceSelected = false;
        for (DataSource ds : this.allDataSources) {
            if (ds.getEnabled())
                atLeastOneDatasourceSelected = true;
        }
        return atLeastOneDatasourceSelected;
    }

    public void setAllDataSourcesforLauncher(DataSource datasource) {
        this.allDataSources.clear(); // TODO WHY? CLEAN ALL
        this.allDataSources.add(datasource);
    }

    public void refreshDataSources() {
        this.allDataSources.clear();

        DataSourceStorage.getInstance(ctx).fillDefaultDataSources();

        int size = DataSourceStorage.getInstance().getSize();

        // copy the value from shared preference to adapter
        for (int i = 0; i < size; i++) {
            String fields[] = DataSourceStorage.getInstance().getFields(i);
            this.allDataSources.add(new DataSource(fields[0], fields[1],
                    fields[2], fields[3], fields[4]));
        }
    }

    public void requestDataFromAllActiveDataSource(double lat, double lon,
                                                   double alt, float radius) {
        for (DataSource ds : allDataSources) {
            /*
             * when type is OpenStreetMap iterate the URL list and for selected
			 * URL send data request
			 */
            if (ds.getEnabled()) {
                requestData(ds, lat, lon, alt, radius, Locale.getDefault()
                        .getLanguage());
            }
        }

    }

    private void requestData(DataSource datasource, double lat, double lon,
                             double alt, float radius, String locale) {
        DownloadRequest request = new DownloadRequest(datasource,
                datasource.createRequestParams(lat, lon, alt, radius, locale));
        ctx.getDownloadManager().submitJob(request);

    }

}

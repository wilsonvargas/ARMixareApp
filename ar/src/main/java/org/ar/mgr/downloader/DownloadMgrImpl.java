
package org.ar.mgr.downloader;

import android.util.Log;

import org.ar.ArContext;
import org.ar.ArView;
import org.ar.data.convert.DataConvertor;
import org.ar.lib.marker.Marker;
import org.ar.mgr.HttpTools;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

class DownloadMgrImpl implements Runnable, DownloadManager {

    private boolean stop = false;
    private ArContext ctx;
    private DownloadManagerState state = DownloadManagerState.Confused;
    private LinkedBlockingQueue<ManagedDownloadRequest> todoList = new
            LinkedBlockingQueue<ManagedDownloadRequest>();
    private ConcurrentHashMap<String, DownloadResult> doneList = new ConcurrentHashMap<String,
            DownloadResult>();
    private Executor executor = Executors.newSingleThreadExecutor();

    public DownloadMgrImpl(ArContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("Ar Context IS NULL");
        }
        this.ctx = ctx;
        state = DownloadManagerState.OffLine;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ar.mgr.downloader.DownloadManager#run()
     */
    public void run() {
        ManagedDownloadRequest mRequest;
        DownloadResult result;
        stop = false;
        while (!stop) {
            state = DownloadManagerState.OnLine;
            // Wait for proceed
            while (!stop) {
                try {
                    mRequest = todoList.take();
                    state = DownloadManagerState.Downloading;
                    result = processRequest(mRequest);
                } catch (InterruptedException e) {
                    result = new DownloadResult();
                    result.setError(e, null);
                }
                doneList.put(result.getIdOfDownloadRequest(), result);
                state = DownloadManagerState.OnLine;
            }
        }
        state = DownloadManagerState.OffLine;
    }

    private DownloadResult processRequest(ManagedDownloadRequest mRequest) {
        DownloadRequest request = mRequest.getOriginalRequest();
        final DownloadResult result = new DownloadResult();
        try {
            if (request == null) {
                throw new Exception("Request is null");
            }

            if (!request.getSource().isWellFormed()) {
                throw new Exception("Datasource in not WellFormed");
            }

            String pageContent = HttpTools.getPageContent(request,
                    ctx.getContentResolver());

            if (pageContent != null) {
                // try loading Marker data
                List<Marker> markers = DataConvertor.getInstance().load(
                        request.getSource().getUrl(), pageContent,
                        request.getSource());
                result.setAccomplish(mRequest.getUniqueKey(), markers,
                        request.getSource());
            }
        } catch (Exception ex) {
            result.setError(ex, request);
            Log.w(ArContext.TAG, "ERROR ON DOWNLOAD REQUEST", ex);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ar.mgr.downloader.DownloadManager#purgeLists()
     */
    public synchronized void resetActivity() {
        todoList.clear();
        doneList.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ar.mgr.downloader.DownloadManager#submitJob(org.ar.mgr.downloader
     * .DownloadRequest)
     */
    public String submitJob(DownloadRequest job) {
        String jobId = null;
        if (job != null && job.getSource().isWellFormed()) {
            ManagedDownloadRequest mJob;
            if (!todoList.contains(job)) {
                mJob = new ManagedDownloadRequest(job);
                todoList.add(mJob);
                Log.i(ArView.TAG, "Submitted " + job.toString());
                jobId = mJob.getUniqueKey();
            }
        }
        return jobId;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ar.mgr.downloader.DownloadManager#getReqResult(java.lang.String)
     */
    public DownloadResult getReqResult(String jobId) {
        DownloadResult result = doneList.get(jobId);
        doneList.remove(jobId);
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ar.mgr.downloader.DownloadManager#getNextResult()
     */
    public synchronized DownloadResult getNextResult() {
        DownloadResult result = null;
        if (!doneList.isEmpty()) {
            String nextId = doneList.keySet().iterator().next();
            result = doneList.get(nextId);
            doneList.remove(nextId);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ar.mgr.downloader.DownloadManager#getResultSize()
     */
    public int getResultSize() {
        return doneList.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ar.mgr.downloader.DownloadManager#isDone()
     */
    public Boolean isDone() {
        return todoList.isEmpty();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ar.mgr.downloader.DownloadManager#goOnline()
     */
    public void switchOn() {
        if (DownloadManagerState.OffLine.equals(getState())) {
            executor.execute(this);
        } else {
            Log.i(ArView.TAG, "DownloadManager already started");
        }
    }

    public void switchOff() {
        stop = true;
    }

    @Override
    public DownloadManagerState getState() {
        return state;
    }

}

package com.codedead.advancedportchecker.domain;

import android.os.AsyncTask;

import com.codedead.advancedportchecker.interfaces.AsyncResponse;
import com.codedead.advancedportchecker.net.NetController;

public class ScanController extends AsyncTask<Void, ScanProgress, Void> {

    private String host;
    private int startPort;
    private int endPort;
    private int timeOut;
    private ScanMethod scanMethod;

    private boolean cancel;
    private AsyncResponse response;

    public ScanController(String host, int startPort,
                          int endPort, int timeOut,
                          ScanMethod scanMethod, AsyncResponse response) {
        if (host == null || host.isEmpty()) throw new NullPointerException("Host cannot be null or empty!");
        if (response == null) throw new NullPointerException("AsyncResponse delegate cannot be null!");

        this.host = host;
        this.startPort = startPort;
        this.endPort = endPort;
        this.timeOut = timeOut;
        this.scanMethod = scanMethod;
        this.cancel = false;

        this.response = response;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int currentPort = startPort;
        while (currentPort <= endPort) {
            if (cancel) break;

            switch (scanMethod) {
                case TCP:
                    publishProgress(NetController.scanTcp(host, currentPort, timeOut));
                    break;
                case UDP:
                    publishProgress(NetController.scanUdp(host, currentPort, timeOut));
                    break;
                case All:
                    publishProgress(NetController.scanTcp(host, currentPort, timeOut));
                    publishProgress(NetController.scanUdp(host, currentPort, timeOut));
                    break;
            }

            currentPort++;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        response.scanComplete();
        super.onPostExecute(aVoid);
    }

    @Override
    protected void onProgressUpdate(ScanProgress... values) {
        response.reportProgress(values[0]);
        super.onProgressUpdate(values);
    }
}

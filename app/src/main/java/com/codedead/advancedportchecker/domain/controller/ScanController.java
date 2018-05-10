package com.codedead.advancedportchecker.domain.controller;

import android.os.AsyncTask;

import com.codedead.advancedportchecker.domain.object.ScanProgress;
import com.codedead.advancedportchecker.domain.interfaces.AsyncResponse;
import com.codedead.advancedportchecker.domain.object.ScanStatus;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public final class ScanController extends AsyncTask<Void, ScanProgress, Void> {

    private String host;
    private int startPort;
    private int endPort;
    private int timeOut;

    private AsyncResponse response;

    public ScanController(String host, int startPort,
                          int endPort, int timeOut,
                          AsyncResponse response) {

        if (host == null || host.isEmpty()) throw new NullPointerException("Host cannot be null or empty!");
        if (response == null) throw new NullPointerException("AsyncResponse delegate cannot be null!");

        this.host = host;
        this.startPort = startPort;
        this.endPort = endPort;
        this.timeOut = timeOut;
        this.response = response;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int currentPort = startPort;
        while (currentPort <= endPort) {
            if (isCancelled()) {
                break;
            }
            publishProgress(scanTcp(host, currentPort, timeOut));
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
        if (isCancelled()) return;

        response.update(values[0]);
        super.onProgressUpdate(values);
    }

    private static ScanProgress scanTcp(String host, int port, int timeOut) {
        ScanProgress scan = new ScanProgress(host, port);
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeOut);
            socket.close();
            scan.setStatus(ScanStatus.OPEN);
        } catch (SocketTimeoutException ce) {
            scan.setStatus(ScanStatus.TIMEOUT);
        } catch (Exception ex) {
            scan.setStatus(ScanStatus.CLOSED);
        }

        return scan;
    }

    @Override
    protected void onCancelled() {
        response.scanCancelled();
        super.onCancelled();
    }
}

package com.codedead.advancedportchecker.domain;

import android.os.AsyncTask;

import com.codedead.advancedportchecker.interfaces.AsyncResponse;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ScanController extends AsyncTask<Void, ScanProgress, Void> {

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
            if (isCancelled()) break;
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
        response.reportProgress(values[0]);
        super.onProgressUpdate(values);
    }

    private static ScanProgress scanTcp(String host, int port, int timeOut) {
        ScanProgress scan = new ScanProgress(host, port);
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeOut);
            socket.close();
            scan.setStatus("OPEN");
        } catch (SocketTimeoutException ce) {
            ce.printStackTrace();
            scan.setStatus("TIMEOUT");
        } catch (Exception ex) {
            ex.printStackTrace();
            scan.setStatus("CLOSED");
        }

        return scan;
    }
}

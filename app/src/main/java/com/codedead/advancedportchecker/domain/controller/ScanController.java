package com.codedead.advancedportchecker.domain.controller;

import android.content.Context;
import android.os.AsyncTask;

import com.codedead.advancedportchecker.R;
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

    /**
     * Initialize a new ScanController
     * @param context   The context that can be used to retrieve error messages
     * @param host      The host address that needs to be scanned
     * @param startPort The initial port for a range of ports that need to be scanned
     * @param endPort   The final port in a range of ports that need to be scanned
     * @param timeOut   The time it takes before a connection is marked as timed-out
     * @param response  The AsyncResponse that can be called when a scan has finished or been cancelled
     */
    public ScanController(Context context, String host, int startPort,
                          int endPort, int timeOut,
                          AsyncResponse response) {

        if (context == null) throw new NullPointerException("Context cannot be null!");
        if (host == null || host.isEmpty() || !UtilController.isValidAddress(host)) throw new IllegalArgumentException(context.getString(R.string.string_invalid_host));
        if (response == null) throw new NullPointerException(context.getString(R.string.asyncResponse_null_exception));

        if (startPort < 1) throw new IllegalArgumentException(context.getString(R.string.string_invalid_startport));
        if (endPort < 1) throw new IllegalArgumentException(context.getString(R.string.string_invalid_endport));
        if (endPort < startPort) throw new IllegalArgumentException(context.getString(R.string.string_endport_larger_than_startport));
        if (endPort > 65535 || startPort > 65535) throw new IllegalArgumentException(context.getString(R.string.string_largest_possible_port));

        host = host.replace("http://", "");
        host = host.replace("https://", "");
        host = host.replace("ftp://", "");

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

    /**
     * Scan a host using TCP
     * @param host    The host that needs to be scanned
     * @param port    The port that needs to be scanned
     * @param timeOut The time it takes before a connection is closed due to a time-out
     * @return A ScanProgress object containing the result of the scan
     */
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

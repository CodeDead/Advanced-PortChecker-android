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

    private final String host;
    private final int startPort;
    private final int endPort;
    private final int timeOut;

    private final AsyncResponse response;

    /**
     * Initialize a new ScanController
     *
     * @param context   The context that can be used to retrieve error messages
     * @param host      The host address that needs to be scanned
     * @param startPort The initial port for a range of ports that need to be scanned
     * @param endPort   The final port in a range of ports that need to be scanned
     * @param timeOut   The time it takes before a connection is marked as timed-out
     * @param response  The AsyncResponse that can be called when a scan has finished or been cancelled
     */
    public ScanController(final Context context, String host, final int startPort,
                          final int endPort, final int timeOut,
                          final AsyncResponse response) {

        if (context == null) throw new NullPointerException("Context cannot be null!");
        if (host == null || host.isEmpty() || !UtilController.isValidAddress(host))
            throw new IllegalArgumentException(context.getString(R.string.string_invalid_host));
        if (response == null)
            throw new NullPointerException(context.getString(R.string.asyncResponse_null_exception));

        if (startPort < 1)
            throw new IllegalArgumentException(context.getString(R.string.string_invalid_startport));
        if (endPort < 1)
            throw new IllegalArgumentException(context.getString(R.string.string_invalid_endport));
        if (endPort < startPort)
            throw new IllegalArgumentException(context.getString(R.string.string_endport_larger_than_startport));
        if (endPort > 65535)
            throw new IllegalArgumentException(context.getString(R.string.string_largest_possible_port));

        host = host
                .replace("http://", "")
                .replace("https://", "")
                .replace("ftp://", "");

        this.host = host;
        this.startPort = startPort;
        this.endPort = endPort;
        this.timeOut = timeOut;
        this.response = response;
    }

    @Override
    protected Void doInBackground(final Void... voids) {
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
    protected void onPostExecute(final Void aVoid) {
        response.scanComplete();
        super.onPostExecute(aVoid);
    }

    @Override
    protected void onProgressUpdate(final ScanProgress... values) {
        if (isCancelled()) return;

        response.update(values[0]);
        super.onProgressUpdate(values);
    }

    /**
     * Scan a host using TCP
     *
     * @param host    The host that needs to be scanned
     * @param port    The port that needs to be scanned
     * @param timeOut The time it takes before a connection is closed due to a time-out
     * @return A ScanProgress object containing the result of the scan
     */
    private static ScanProgress scanTcp(final String host, final int port, final int timeOut) {
        final ScanProgress scan = new ScanProgress(host, port);
        try {
            try (final Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), timeOut);
            }
            scan.setStatus(ScanStatus.OPEN);
        } catch (final SocketTimeoutException ce) {
            scan.setStatus(ScanStatus.TIMEOUT);
        } catch (final Exception ex) {
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

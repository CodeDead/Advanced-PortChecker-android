package com.codedead.advancedportchecker.domain.controller;

import android.content.Context;

import com.codedead.advancedportchecker.R;
import com.codedead.advancedportchecker.domain.object.ScanProgress;
import com.codedead.advancedportchecker.domain.interfaces.AsyncResponse;
import com.codedead.advancedportchecker.domain.object.ScanStatus;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ScanController {

    private final Context context;
    private ExecutorService executorService;
    private final int poolSize;
    private final AsyncResponse response;

    /**
     * Initialize a new ScanController
     *
     * @param context  The context that can be used to retrieve error messages
     * @param response The AsyncResponse that can be called when a scan has finished or been cancelled
     */
    public ScanController(final Context context,
                          final AsyncResponse response) {
        if (context == null)
            throw new NullPointerException("Context cannot be null!");

        this.context = context;

        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        poolSize = Math.max(2, Math.min(numberOfCores * 2, 4));

        this.response = response;
    }

    /**
     * Start scanning a host for open ports
     *
     * @param host      The host that needs to be scanned
     * @param startPort The initial port for a range of ports that need to be scanned
     * @param endPort   The final port in a range of ports that need to be scanned
     * @param timeOut   The time it takes before a connection is marked as timed-out
     */
    public void startScan(String host, final int startPort, final int endPort, final int timeOut) {
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

        final int totalNumberOfPorts = endPort - startPort + 1;
        // Divide the total number of ports by the number of cores
        final int numberOfPortsPerThread = totalNumberOfPorts / poolSize;
        // Calculate the remaining ports that need to be scanned
        final int remainingPorts = totalNumberOfPorts % poolSize;

        executorService = Executors.newFixedThreadPool(Math.min(totalNumberOfPorts, poolSize));

        host = host
                .replace("http://", "")
                .replace("https://", "")
                .replace("ftp://", "")
                .replace("ssh://", "")
                .replace("telnet://", "")
                .replace("smtp://", "");


        final String finalHost = host;
        for (int i = 0; i < poolSize; i++) {
            final int currentStartPort = startPort + i * numberOfPortsPerThread;
            int currentEndPort = currentStartPort + numberOfPortsPerThread - 1;

            if (i == poolSize - 1) {
                // Add the remaining ports to the last thread
                currentEndPort += remainingPorts;
            }

            int finalCurrentEndPort = currentEndPort;
            executorService.execute(() -> {
                // Loop over the ports to scan
                for (int currentPort = currentStartPort; currentPort <= finalCurrentEndPort; currentPort++) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    final ScanProgress scan = scanTcp(finalHost, currentPort, timeOut);
                    response.update(scan);
                }
            });
        }
    }

    /**
     * Cancel the current scan
     *
     * @throws InterruptedException If the cancellation cannot be awaited
     */
    public void cancelScan() throws InterruptedException {
        if (executorService == null)
            return;

        executorService.shutdownNow();
        //noinspection ResultOfMethodCallIgnored
        executorService.awaitTermination(30000, TimeUnit.MILLISECONDS);
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
}

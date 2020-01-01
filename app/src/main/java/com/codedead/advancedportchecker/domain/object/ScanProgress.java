package com.codedead.advancedportchecker.domain.object;

public final class ScanProgress {

    private final String host;
    private final int port;
    private ScanStatus status;

    /**
     * Initialize a new ScanProgress object
     *
     * @param host The host that was scanned
     * @param port The port that was scanned
     */
    public ScanProgress(String host, int port) {
        if (host == null || host.isEmpty())
            throw new IllegalArgumentException("Host cannot be null or empty!");

        this.host = host;
        this.port = port;
        this.status = null;
    }

    /**
     * Set the status of a scan
     *
     * @param status The status of the scan
     */
    public void setStatus(ScanStatus status) {
        if (status == null) throw new NullPointerException("Status cannot be null!");
        this.status = status;
    }

    /**
     * Get the status of a scan
     *
     * @return The status of a scan
     */
    public ScanStatus getStatus() {
        return status;
    }

    /**
     * Get the full address of the host that was scanned including the port
     *
     * @return The full address of the host that was scanned including the port
     */
    public String getFullHost() {
        return host + ":" + port;
    }
}


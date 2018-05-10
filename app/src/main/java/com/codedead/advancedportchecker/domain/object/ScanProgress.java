package com.codedead.advancedportchecker.domain.object;

public final class ScanProgress {

    private final String host;
    private final int port;
    private ScanStatus status;

    public ScanProgress(String host, int port) {
        if (host == null || host.isEmpty()) throw new NullPointerException("Host cannot be null or empty!");

        this.host = host;
        this.port = port;
        this.status = null;
    }

    public void setStatus(ScanStatus status) {
        if (status == null) throw new NullPointerException("Status cannot be null!");
        this.status = status;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public String getFullHost() {
        return host + ":" + port;
    }
}


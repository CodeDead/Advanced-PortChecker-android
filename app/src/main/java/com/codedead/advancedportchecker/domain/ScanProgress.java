package com.codedead.advancedportchecker.domain;

public class ScanProgress {

    private String host;
    private int port;
    private String status;

    public ScanProgress(String host, int port) {
        if (host == null || host.isEmpty()) throw new NullPointerException("Host cannot be null or empty!");

        this.host = host;
        this.port = port;
        this.status = "";
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void setStatus(String status) {
        if (status == null) throw new NullPointerException("Status cannot be null!");
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String getFullHost() {
        return host + ":" + port;
    }
}

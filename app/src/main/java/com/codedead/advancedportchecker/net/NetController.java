package com.codedead.advancedportchecker.net;

import com.codedead.advancedportchecker.domain.ScanMethod;
import com.codedead.advancedportchecker.domain.ScanProgress;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class NetController {

    public static ScanProgress scanTcp(String host, int port, int timeOut) {
        ScanProgress scan = new ScanProgress(host, port, ScanMethod.TCP);

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

    public static ScanProgress scanUdp(String host, int port, int timeOut) {
        ScanProgress scan = new ScanProgress(host, port, ScanMethod.TCP);

        return scan;
    }
}

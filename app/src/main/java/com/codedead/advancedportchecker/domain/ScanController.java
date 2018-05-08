package com.codedead.advancedportchecker.domain;

import android.os.AsyncTask;
import android.widget.EditText;

import com.codedead.advancedportchecker.interfaces.AsyncResponse;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ScanController extends AsyncTask<Void, ScanProgress, Void> {

    private String host;
    private int startPort;
    private int endPort;
    private int timeOut;

    private WeakReference<EditText> edtOutput;

    private AsyncResponse response;

    public ScanController(String host, int startPort,
                          int endPort, int timeOut,
                          EditText edtOutput, AsyncResponse response) {

        if (host == null || host.isEmpty()) throw new NullPointerException("Host cannot be null or empty!");
        if (edtOutput == null) throw new NullPointerException("Output canot be null!");
        if (response == null) throw new NullPointerException("AsyncResponse delegate cannot be null!");

        this.host = host;
        this.startPort = startPort;
        this.endPort = endPort;
        this.timeOut = timeOut;
        this.edtOutput = new WeakReference<>(edtOutput);
        this.response = response;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int currentPort = startPort;
        while (currentPort <= endPort) {
            if (isCancelled()) {
                onCancelled();
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
        if (edtOutput.get() == null) return;

        if (!edtOutput.get().getText().toString().isEmpty()) {
            edtOutput.get().append("\n");
        }
        edtOutput.get().append(values[0].getFullHost() + " | " + values[0].getStatus());
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

    @Override
    protected void onCancelled() {
        response.scanCancelled();
        super.onCancelled();
    }
}

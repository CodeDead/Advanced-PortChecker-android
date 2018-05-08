package com.codedead.advancedportchecker.gui;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.codedead.advancedportchecker.R;
import com.codedead.advancedportchecker.domain.ScanController;
import com.codedead.advancedportchecker.domain.ScanProgress;
import com.codedead.advancedportchecker.interfaces.AsyncResponse;

public class ScanActivity extends AppCompatActivity implements AsyncResponse {

    private EditText edtHost;
    private EditText edtStartPort;
    private EditText edtEndPort;
    private EditText edtOutput;
    private ProgressBar pgbScan;

    private ScanController scanController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        edtHost = findViewById(R.id.EdtHost);
        edtStartPort = findViewById(R.id.EdtStartPort);
        edtEndPort = findViewById(R.id.EdtEndPort);
        edtOutput = findViewById(R.id.EdtScanOutput);
        pgbScan = findViewById(R.id.PgbScanProgress);

        Button btnScan = findViewById(R.id.BtnScan);

        edtOutput.setKeyListener(null);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setControlModifiers(!edtHost.isEnabled());

                if (scanController != null && !scanController.isCancelled()) {
                    stopScan();
                } else {
                    startScan();
                }
            }
        });
    }

    private void setControlModifiers(boolean enabled) {
        edtHost.setEnabled(enabled);
        edtStartPort.setEnabled(enabled);
        edtEndPort.setEnabled(enabled);
    }

    private void startScan() {
        if (!scanController.isCancelled()) return;
        scanController = new ScanController(edtHost.getText().toString(), Integer.parseInt(edtStartPort.getText().toString()), Integer.parseInt(edtEndPort.getText().toString()), 5000, this);
        scanController.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void stopScan() {
        if (scanController == null) return;
        scanController.cancel(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scan_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_scan_settings:
                break;
            case R.id.nav_scan_about:
                break;
            case R.id.nav_scan_exit:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void scanComplete() {
        setControlModifiers(true);
        scanController = null;
    }

    @Override
    public void reportProgress(ScanProgress progress) {
        if (!edtOutput.getText().toString().isEmpty()) {
            edtOutput.append("\n");
        }
        edtOutput.append(progress.getFullHost() + " | " + progress.getStatus());
    }
}

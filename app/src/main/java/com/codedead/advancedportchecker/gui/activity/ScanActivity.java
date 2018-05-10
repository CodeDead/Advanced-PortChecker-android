package com.codedead.advancedportchecker.gui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codedead.advancedportchecker.R;
import com.codedead.advancedportchecker.domain.controller.ScanController;
import com.codedead.advancedportchecker.domain.object.ScanProgress;
import com.codedead.advancedportchecker.domain.interfaces.AsyncResponse;

public class ScanActivity extends AppCompatActivity implements AsyncResponse {

    private EditText edtHost;
    private EditText edtStartPort;
    private EditText edtEndPort;
    private EditText edtOutput;
    private Button btnScan;
    private ProgressBar pgbScan;

    private ScanController scanController;
    private int progress;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtHost = findViewById(R.id.EdtHost);
        edtStartPort = findViewById(R.id.EdtStartPort);
        edtEndPort = findViewById(R.id.EdtEndPort);
        btnScan = findViewById(R.id.BtnScan);
        edtOutput = findViewById(R.id.EdtScanOutput);
        pgbScan = findViewById(R.id.PgbScanProgress);

        edtOutput.setKeyListener(null);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scanController != null && !scanController.isCancelled()) {
                    stopScan();
                } else if (scanController == null) {
                    startScan();
                } else {
                    showAlert(getString(R.string.string_wait_scancontroller));
                }
            }
        });
    }

    private void setControlModifiers(boolean enabled) {
        edtHost.setEnabled(enabled);
        edtStartPort.setEnabled(enabled);
        edtEndPort.setEnabled(enabled);

        if (enabled) {
            pgbScan.setVisibility(View.GONE);
        } else {
            pgbScan.setVisibility(View.VISIBLE);
        }
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton(
                android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void startScan() {
        if (scanController != null && !scanController.isCancelled()) return;

        if (edtHost.getText().toString().length() == 0) {
            showAlert("Please enter a valid host address!");
            return;
        }

        if (edtStartPort.getText().toString().length() == 0) {
            showAlert("Please enter a valid start port!");
            return;
        }

        if (edtEndPort.getText().toString().length() == 0) {
            showAlert("Please enter a valid end port!");
            return;
        }

        int startPort = Integer.parseInt(edtStartPort.getText().toString());
        int endPort = Integer.parseInt(edtEndPort.getText().toString());

        if (startPort < 1) {
            showAlert("Start port is not a valid port number!");
            return;
        }

        if (endPort < 1) {
            showAlert("End port is not a valid port number!");
            return;
        }

        if (endPort < startPort) {
            showAlert("The end port cannot be smaller than the start port!");
            return;
        }

        if (endPort > 65535 || startPort > 65535) {
            showAlert("The largest possible port is 65535!");
            return;
        }

        edtHost.setText(edtHost.getText().toString().replace("http://",""));
        edtHost.setText(edtHost.getText().toString().replace("https://",""));
        edtHost.setText(edtHost.getText().toString().replace("ftp://",""));

        int max = Integer.parseInt(edtEndPort.getText().toString()) - Integer.parseInt(edtStartPort.getText().toString()) + 1;
        pgbScan.setMax(max);
        pgbScan.setProgress(0);
        progress = 0;

        int timeOut = sharedPreferences.getInt("socketTimeout", 2000);

        try {
            scanController = new ScanController(edtHost.getText().toString(), Integer.parseInt(edtStartPort.getText().toString()), Integer.parseInt(edtEndPort.getText().toString()), timeOut, this);
            scanController.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            edtOutput.setText("");
            btnScan.setText(getString(R.string.string_cancel_scan));
            setControlModifiers(!edtHost.isEnabled());
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
            case R.id.nav_scan_clear_output:
                edtOutput.setText("");
                break;
            case R.id.nav_scan_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case android.R.id.home:
            case R.id.nav_scan_exit:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addCancelText() {
        if (!edtOutput.getText().toString().isEmpty()) {
            edtOutput.append("\n");
        }

        edtOutput.append(getString(R.string.string_scan_cancelled));
    }

    @Override
    public void scanComplete() {
        setControlModifiers(true);
        scanController = null;

        if (!edtOutput.getText().toString().isEmpty()) {
            edtOutput.append("\n");
        }
        edtOutput.append(getString(R.string.string_scan_complete));
        btnScan.setText(getString(R.string.string_scan));
    }

    @Override
    public void scanCancelled() {
        setControlModifiers(true);
        scanController = null;
        addCancelText();
        btnScan.setText(getString(R.string.string_scan));
    }

    @Override
    public void update(ScanProgress scanProgress) {
        progress++;

        if (!edtOutput.getText().toString().isEmpty()) {
            edtOutput.append("\n");
        }
        edtOutput.append(scanProgress.getFullHost() + " | " + scanProgress.getStatus());

        pgbScan.setProgress(progress);
    }

    @Override
    protected void onPause() {
        if (scanController != null && !scanController.isCancelled()) {
            scanController.cancel(true);
        }
        super.onPause();
    }
}
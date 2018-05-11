package com.codedead.advancedportchecker.gui.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codedead.advancedportchecker.R;
import com.codedead.advancedportchecker.domain.controller.LocaleHelper;
import com.codedead.advancedportchecker.domain.controller.ScanController;
import com.codedead.advancedportchecker.domain.controller.UtilController;
import com.codedead.advancedportchecker.domain.object.ScanProgress;
import com.codedead.advancedportchecker.domain.interfaces.AsyncResponse;

import java.util.Random;

import static android.content.pm.PackageManager.GET_META_DATA;

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
    private boolean displayTimedOut;
    private boolean displayClosed;
    private int timeOut;
    private boolean vibrateOnComplete;
    private boolean displayNotification;

    private boolean active;

    private String lastLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        lastLanguage = sharedPreferences.getString("appLanguage", "en");
        LocaleHelper.setLocale(this, lastLanguage);

        resetTitle();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

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
                    UtilController.showAlert(ScanActivity.this, getString(R.string.string_wait_scancontroller));
                }
            }
        });

        createNotificationChannel();
        reviewAlert();
    }

    private void resetTitle() {
        try {
            int label = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA).labelRes;
            if (label != 0) {
                setTitle(label);
            }
        } catch (PackageManager.NameNotFoundException ignored) {

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleHelper.onAttach(getBaseContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onPause() {
        active = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (!lastLanguage.equals(sharedPreferences.getString("appLanguage", "en"))) {
            LocaleHelper.setLocale(getApplicationContext(), sharedPreferences.getString("language", "en"));
            recreate();
        }

        active = true;
        if (sharedPreferences.getBoolean("keepScreenOn", true)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        displayTimedOut = sharedPreferences.getBoolean("displayTimeOut", true);
        displayClosed = sharedPreferences.getBoolean("displayClosed", true);
        timeOut = Integer.parseInt(sharedPreferences.getString("socketTimeout", "200"));
        vibrateOnComplete = sharedPreferences.getBoolean("vibrateOnComplete", true);
        displayNotification = sharedPreferences.getBoolean("notificationOnComplete", true);
        super.onResume();
    }

    private void reviewAlert() {
        if (sharedPreferences.getInt("reviewTimes", 0) > 2) return;
        Random rnd = new Random();

        new CountDownTimer(rnd.nextInt(180) * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage(R.string.alert_review_text);
                builder.setCancelable(false);

                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        addReview(true);
                        UtilController.openWebsite(getApplicationContext(), "market://details?id=com.codedead.advancedportchecker");
                    }
                });

                builder.setNeutralButton(R.string.alert_review_never, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        addReview(true);
                    }
                });

                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        addReview(false);
                    }
                });

                AlertDialog alert = builder.create();
                if (!isFinishing() && active) {
                    alert.show();
                } else {
                    cancel();
                    start();
                }
            }
        }.start();
    }

    private void addReview(boolean done) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (done) {
            editor.putInt("reviewTimes", 3);
        } else {
            editor.putInt("reviewTimes", sharedPreferences.getInt("reviewTimes", 0) + 1);
        }

        editor.apply();
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null) return;

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            v.vibrate(500);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void displayNotification() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_network_wifi_24dp)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.string_notification_scan_complete))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1337, mBuilder.build());
        }
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

    private void startScan() {
        if (scanController != null && !scanController.isCancelled()) return;

        if (edtHost.getText().toString().length() == 0) {
            UtilController.showAlert(this, getString(R.string.string_invalid_host));
            return;
        }

        if (edtStartPort.getText().toString().length() == 0) {
            UtilController.showAlert(this, getString(R.string.string_invalid_startport));
            return;
        }

        if (edtEndPort.getText().toString().length() == 0) {
            UtilController.showAlert(this, getString(R.string.string_invalid_endport));
            return;
        }

        int startPort = Integer.parseInt(edtStartPort.getText().toString());
        int endPort = Integer.parseInt(edtEndPort.getText().toString());

        if (startPort < 1) {
            UtilController.showAlert(this, getString(R.string.string_invalid_startport));
            return;
        }

        if (endPort < 1) {
            UtilController.showAlert(this, getString(R.string.string_invalid_endport));
            return;
        }

        if (endPort < startPort) {
            UtilController.showAlert(this, getString(R.string.string_endport_larger_than_startport));
            return;
        }

        if (endPort > 65535 || startPort > 65535) {
            UtilController.showAlert(this, getString(R.string.string_largest_possible_port));
            return;
        }

        edtHost.setText(edtHost.getText().toString().replace("http://",""));
        edtHost.setText(edtHost.getText().toString().replace("https://",""));
        edtHost.setText(edtHost.getText().toString().replace("ftp://",""));

        int max = Integer.parseInt(edtEndPort.getText().toString()) - Integer.parseInt(edtStartPort.getText().toString()) + 1;
        pgbScan.setMax(max);
        pgbScan.setProgress(0);
        progress = 0;

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
                startActivity(new Intent(this, SettingsActivity.class));
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

        boolean vibrate = false;
        if (vibrateOnComplete && displayNotification && !active) {
            vibrate = false;
        } else if (vibrateOnComplete && active) {
            vibrate = true;
        }

        if (vibrate) {
            vibrate();
        }

        if (displayNotification && !active) {
            displayNotification();
        }
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

        boolean display = true;
        String scanStatus = "";

        switch (scanProgress.getStatus()) {
            case TIMEOUT:
                scanStatus = getString(R.string.string_timeout);
                if (!displayTimedOut) {
                    display = false;
                }
                break;
            case CLOSED:
                scanStatus = getString(R.string.string_closed);
                if (!displayClosed) {
                    display = false;
                }
                break;
            case OPEN:
                scanStatus = getString(R.string.string_open);
                break;
        }

        if (display) {
            if (!edtOutput.getText().toString().isEmpty()) {
                edtOutput.append("\n");
            }
            edtOutput.append(scanProgress.getFullHost() + " | " + scanStatus);
        }

        pgbScan.setProgress(progress);
    }
}

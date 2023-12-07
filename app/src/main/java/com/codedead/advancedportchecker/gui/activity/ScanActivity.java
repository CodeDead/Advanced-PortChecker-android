package com.codedead.advancedportchecker.gui.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.codedead.advancedportchecker.R;
import com.codedead.advancedportchecker.domain.controller.LocaleHelper;
import com.codedead.advancedportchecker.domain.controller.ScanController;
import com.codedead.advancedportchecker.domain.controller.UtilController;
import com.codedead.advancedportchecker.domain.object.NetworkUtils;
import com.codedead.advancedportchecker.domain.object.ScanProgress;
import com.codedead.advancedportchecker.domain.interfaces.AsyncResponse;

import java.util.Objects;
import java.util.Random;

import static android.content.pm.PackageManager.GET_META_DATA;

public final class ScanActivity extends AppCompatActivity implements AsyncResponse {

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
    private boolean statusColorCoded;
    private int timeOut;
    private boolean vibrateOnComplete;
    private boolean displayNotification;

    private boolean active;

    private NetworkUtils networkUtils;
    private String lastLanguage;
    private int maxProgress;
    private NotificationCompat.Builder mBuilder;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        lastLanguage = sharedPreferences.getString("appLanguage", "en");
        LocaleHelper.setLocale(this, lastLanguage);

        resetTitle();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        edtHost = findViewById(R.id.EdtHost);
        edtStartPort = findViewById(R.id.EdtStartPort);
        edtEndPort = findViewById(R.id.EdtEndPort);
        btnScan = findViewById(R.id.BtnScan);
        edtOutput = findViewById(R.id.EdtScanOutput);
        pgbScan = findViewById(R.id.PgbScanProgress);

        edtOutput.setKeyListener(null);

        btnScan.setOnClickListener(v -> {
            if (scanController != null && !scanController.isCancelled()) {
                stopScan();
            } else if (scanController == null) {
                startScan();
            } else {
                UtilController.showAlert(ScanActivity.this, getString(R.string.string_wait_scancontroller));
            }
        });

        createNotificationChannel();
        reviewAlert();
        networkUtils = new NetworkUtils(this);
    }

    /**
     * Reset the title of the activity
     */
    private void resetTitle() {
        try {
            final int label = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA).labelRes;
            if (label != 0) {
                setTitle(label);
            }
        } catch (PackageManager.NameNotFoundException ex) {
            UtilController.showAlert(this, ex.getMessage());
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleHelper.onAttach(getBaseContext());
    }

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onPause() {
        active = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        final String selectedLanguage = sharedPreferences.getString("appLanguage", "en");
        if (!lastLanguage.equals(selectedLanguage)) {
            LocaleHelper.setLocale(getApplicationContext(), selectedLanguage);
            recreate();
        }

        active = true;
        if (sharedPreferences.getBoolean("keepScreenOn", true)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        displayTimedOut = sharedPreferences.getBoolean("displayTimeOut", false);
        displayClosed = sharedPreferences.getBoolean("displayClosed", false);
        statusColorCoded = sharedPreferences.getBoolean("statusColorCoded", true);
        timeOut = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("socketTimeout", "200")));
        vibrateOnComplete = sharedPreferences.getBoolean("vibrateOnComplete", true);
        displayNotification = sharedPreferences.getBoolean("notificationOnComplete", true);

        super.onResume();
    }

    /**
     * Display an alert to the user, requesting to review the application
     */
    private void reviewAlert() {
        if (sharedPreferences.getInt("reviewTimes", 0) > 2)
            return;

        final Random rnd = new Random();

        new CountDownTimer(rnd.nextInt(180) * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage(R.string.alert_review_text);
                builder.setCancelable(false);

                builder.setPositiveButton(android.R.string.yes, (dialog, id) -> {
                    dialog.cancel();

                    addReview(true);
                    UtilController.openWebsite(getApplicationContext(), "market://details?id=com.codedead.advancedportchecker");
                });

                builder.setNeutralButton(R.string.alert_review_never, (dialog, id) -> {
                    dialog.cancel();
                    addReview(true);
                });

                builder.setNegativeButton(android.R.string.no, (dialog, which) -> {
                    dialog.cancel();
                    addReview(false);
                });

                final AlertDialog alert = builder.create();
                if (!isFinishing() && active) {
                    alert.show();
                } else {
                    cancel();
                    start();
                }
            }
        }.start();
    }

    /**
     * Keep track of the amount of times the user has been requested to review the application
     *
     * @param done True if the user has reviewed or does not want to review the application
     */
    private void addReview(boolean done) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        if (done) {
            editor.putInt("reviewTimes", 3);
        } else {
            editor.putInt("reviewTimes", sharedPreferences.getInt("reviewTimes", 0) + 1);
        }

        editor.apply();
    }

    /**
     * Vibrate the device for half a second
     */
    private void vibrate() {
        final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null)
            return;

        // Vibrate for 500 milliseconds
        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    /**
     * Create a notification channel to display notifications
     */
    private void createNotificationChannel() {
        final CharSequence name = getString(R.string.app_name);
        final String description = getString(R.string.channel_description);
        final int importance = NotificationManager.IMPORTANCE_DEFAULT;
        final NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);
        channel.setDescription(description);

        final NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Display a notification in the device
     */
    private void displayNotification() {
        final Intent intent = new Intent(this, ScanActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_network_wifi_24dp)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.string_notification_scan_complete))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1337, mBuilder.build());
        }
    }

    /**
     * Set the availability of certain controls before or after a scan
     *
     * @param enabled True if all important controls should be enabled, otherwise false
     */
    private void setControlModifiers(final boolean enabled) {
        edtHost.setEnabled(enabled);
        edtStartPort.setEnabled(enabled);
        edtEndPort.setEnabled(enabled);

        if (enabled) {
            pgbScan.setVisibility(View.GONE);
        } else {
            pgbScan.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Start a new scan
     */
    private void startScan() {
        if (scanController != null && !scanController.isCancelled())
            return;

        if (!networkUtils.hasNetworkConnection()) {
            UtilController.showAlert(this, getString(R.string.string_no_internet));
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

        try {
            maxProgress = Integer.parseInt(edtEndPort.getText().toString()) - Integer.parseInt(edtStartPort.getText().toString()) + 1;
            pgbScan.setMax(maxProgress);
            pgbScan.setProgress(0);
            progress = 0;

            if (displayNotification) {
                final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                mBuilder = new NotificationCompat.Builder(this, getString(R.string.app_name))
                        .setSmallIcon(R.drawable.ic_network_wifi_24dp)
                        .setContentTitle(getString(R.string.scan_progress))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setOnlyAlertOnce(true)
                        .setProgress(maxProgress, 0, false);

                notificationManager.notify(69, mBuilder.build());
            }

            scanController = new ScanController(this.getApplicationContext(), edtHost.getText().toString(), Integer.parseInt(edtStartPort.getText().toString()), Integer.parseInt(edtEndPort.getText().toString()), timeOut, this);
            scanController.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            edtOutput.setText("");
            btnScan.setText(getString(R.string.string_cancel_scan));
            setControlModifiers(!edtHost.isEnabled());
        } catch (final Exception ex) {
            UtilController.showAlert(this, ex.getMessage());
        }
    }

    /**
     * Cancel a scan
     */
    private void stopScan() {
        if (scanController == null)
            return;

        scanController.cancel(true);

        if (displayNotification) {
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(69);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scan_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_scan_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (itemId == R.id.nav_scan_clear_output) {
            edtOutput.setText("");
        } else if (itemId == R.id.nav_scan_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (itemId == R.id.nav_scan_exit) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Add text to the output to indicate that a scan was cancelled
     */
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

        final boolean vibrate = (active && vibrateOnComplete);
        if (vibrate) {
            vibrate();
        }

        if (displayNotification && !active) {
            displayNotification();
        }

        if (displayNotification) {
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(69);
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
    public void update(final ScanProgress scanProgress) {
        progress++;

        boolean display = true;

        String scanStatus;
        ForegroundColorSpan colorSpan;

        switch (scanProgress.getStatus()) {
            case TIMEOUT -> {
                scanStatus = getString(R.string.string_timeout);
                colorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                if (!displayTimedOut) {
                    display = false;
                }
            }
            default -> {
                scanStatus = getString(R.string.string_closed);
                colorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                if (!displayClosed) {
                    display = false;
                }
            }
            case OPEN -> {
                scanStatus = getString(R.string.string_open);
                colorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            }
        }

        if (display) {
            Spannable coloredText = new SpannableString(scanStatus);
            if (!edtOutput.getText().toString().isEmpty()) {
                edtOutput.append("\n");
            }

            if (statusColorCoded) {
                coloredText.setSpan(colorSpan, 0, scanStatus.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            edtOutput.append(scanProgress.getFullHost() + " | ");
            edtOutput.append(coloredText);
        }

        pgbScan.setProgress(progress);

        if (displayNotification) {
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mBuilder.setProgress(maxProgress, progress, false);
            notificationManager.notify(69, mBuilder.build());
        }
    }
}

package com.codedead.advancedportchecker.gui.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.codedead.advancedportchecker.R;
import com.codedead.advancedportchecker.domain.controller.LocaleHelper;
import com.codedead.advancedportchecker.domain.controller.UtilController;

import static android.content.pm.PackageManager.GET_META_DATA;

public final class LoadingActivity extends AppCompatActivity {

    private static final int ACTIVITY_SETTINGS_CODE = 1337;
    private WifiManager wifi;
    private static boolean hasStopped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        LocaleHelper.setLocale(this, sharedPreferences.getString("appLanguage", "en"));

        resetTitle();
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_loading);

        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasStopped = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        hasStopped = true;
    }

    /**
     * Reset the title of the activity
     */
    private void resetTitle() {
        try {
            int label = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA).labelRes;
            if (label != 0) {
                setTitle(label);
            }
        } catch (PackageManager.NameNotFoundException ex) {
            UtilController.showAlert(this, ex.getMessage());
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

    /**
     * Check whether all the required permissions are granted. If they are not granted, prompt the user to allow the permissions
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {

            // If some or all of the required permissions are not available, we need to request the user to grant them
            // Don't worry, this will only request permission to data that hasn't been given out yet
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE
                    , Manifest.permission.CHANGE_WIFI_STATE
                    , Manifest.permission.ACCESS_WIFI_STATE
                    , Manifest.permission.INTERNET
                    , Manifest.permission.VIBRATE}, 1);
        } else {
            checkConnectivity();
        }
    }

    /**
     * Continue the loading animation and display the new activity when the loading animation has finished
     */
    private void continueLoading() {
        final Intent i = new Intent(this, ScanActivity.class);

        new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (!hasStopped) {
                    startActivity(i);
                    finish();
                } else {
                    this.start();
                }
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            // The required permissions have not been granted and thus the app cannot function correctly
            // Close the app
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.string_give_manual_permissions);
            builder.setCancelable(false);

            builder.setPositiveButton(android.R.string.yes,
                    (dialog, id) -> {
                        dialog.cancel();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, ACTIVITY_SETTINGS_CODE);
                    });

            builder.setNegativeButton(android.R.string.no,
                    (dialog, id) -> {
                        Toast.makeText(LoadingActivity.this, R.string.string_networkpermissions_required, Toast.LENGTH_SHORT).show();
                        finish();
                        dialog.cancel();
                    });

            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
        // Check if Wifi connection is available
        checkConnectivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_SETTINGS_CODE) {
            // Make sure the request was successful
            checkPermissions();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Check whether an internet connection is available
     * @return True if an internet connection is available, otherwise false
     */
    private boolean hasInternet() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if Wifi is enabled. If Wifi is not enabled, request the user to enable Wifi
     */
    private void checkWifiState() {
        if (!wifi.isWifiEnabled()) {
            wifiConfirmationCheck();
        } else {
            delayedWifiCheck();
        }
    }

    /**
     * Check whether an internet connection is available
     */
    private void checkConnectivity() {
        // Initialize WifiManager
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Check if an internet connection is available
        if (!hasInternet()) {
            // Check if the wifi module on the device is enabled or not
            checkWifiState();
        } else {
            continueLoading();
        }
    }

    /**
     * Request the user to enable the Wifi of the device
     */
    private void wifiConfirmationCheck() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Turn on WiFi
                    wifi.setWifiEnabled(true);
                    // Check if WiFi is connected
                    delayedWifiCheck();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // Close this app since an internet connection is required
                    finish();
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(LoadingActivity.this);
        builder.setMessage(R.string.string_enable_wifi)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, dialogClickListener)
                .setNegativeButton(android.R.string.no, dialogClickListener)
                .show();
    }

    /**
     * Check whether an internet connection is available after a certain amount of time has passed
     */
    private void delayedWifiCheck() {
        new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
                // Do nothing
            }

            @Override
            public void onFinish() {
                // No need for this code if the app is already finishing
                if (isFinishing()) return;
                if (hasInternet()) {
                    // Load the next screen because an internet connection is available
                    continueLoading();
                } else {
                    // Close app because no internet connection is available
                    Toast.makeText(LoadingActivity.this, R.string.string_no_internet, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }.start();
    }
}

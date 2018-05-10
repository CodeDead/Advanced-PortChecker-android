package com.codedead.advancedportchecker.gui.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
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

public class LoadingActivity extends AppCompatActivity {

    private static final int ACTIVITY_SETTINGS_CODE = 1337;
    private WifiManager wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_loading);

        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            // If some or all of the required permissions are not available, we need to request the user to grant them
            // Don't worry, this will only request permission to data that hasn't been given out yet
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE
                    , Manifest.permission.CHANGE_WIFI_STATE
                    , Manifest.permission.ACCESS_WIFI_STATE
                    , Manifest.permission.INTERNET}, 1);
        } else {
            checkConnectivity();
        }
    }

    private void continueLoading() {
        final Intent i = new Intent(this, ScanActivity.class);

        new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                startActivity(i);
                finish();
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
            builder.setMessage("Would you like to manually give permissions?");
            builder.setCancelable(false);

            builder.setPositiveButton(android.R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, ACTIVITY_SETTINGS_CODE);
                        }
                    });

            builder.setNegativeButton(android.R.string.no,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Toast.makeText(LoadingActivity.this, "Network permissions are required in order to run this app!", Toast.LENGTH_SHORT).show();
                            finish();
                            dialog.cancel();
                        }
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

    private boolean hasInternet() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    private void checkWifiState() {
        if (!wifi.isWifiEnabled()) {
            wifiConfirmationCheck();
        } else {
            delayedWifiCheck();
        }
    }

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

    private void wifiConfirmationCheck() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(LoadingActivity.this);
        builder.setMessage("Would you like to enable wifi?")
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, dialogClickListener)
                .setNegativeButton(android.R.string.no, dialogClickListener)
                .show();
    }

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
                    Toast.makeText(LoadingActivity.this, "You don't have an active network connection!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }.start();
    }
}

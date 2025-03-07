package com.codedead.advancedportchecker.gui.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.preference.PreferenceManager;

import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.codedead.advancedportchecker.R;
import com.codedead.advancedportchecker.domain.controller.LocaleHelper;
import com.codedead.advancedportchecker.domain.controller.UtilController;
import com.codedead.advancedportchecker.domain.object.NetworkUtils;

import static android.content.pm.PackageManager.GET_META_DATA;

public final class LoadingActivity extends AppCompatActivity {

    private NetworkUtils networkUtils;
    private static boolean hasStopped;

    private final ActivityResultLauncher<Intent> wifiResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> delayedWifiCheck());

    private final ActivityResultLauncher<Intent> permissionResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> checkPermissions());

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        LocaleHelper.setLocale(this, sharedPreferences.getString("appLanguage", "en"));

        resetTitle();
        super.onCreate(savedInstanceState);

        final Window window = getWindow();
        final View decorView = getWindow().getDecorView();

        WindowCompat.setDecorFitsSystemWindows(window, false);
        final WindowInsetsControllerCompat controllerCompat = new WindowInsetsControllerCompat(window, decorView);
        controllerCompat.hide(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.navigationBars());
        controllerCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        setContentView(R.layout.activity_loading);
        networkUtils = new NetworkUtils(this);

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
            final int label = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA).labelRes;
            if (label != 0) {
                setTitle(label);
            }
        } catch (final PackageManager.NameNotFoundException ex) {
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

    /**
     * Check whether all the required permissions are granted. If they are not granted, prompt the user to allow the permissions
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            // If some or all of the required permissions are not available, we need to request the user to grant them
            // Don't worry, this will only request permission to data that hasn't been given out yet
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE
                        , Manifest.permission.CHANGE_WIFI_STATE
                        , Manifest.permission.ACCESS_WIFI_STATE
                        , Manifest.permission.POST_NOTIFICATIONS
                        , Manifest.permission.INTERNET
                        , Manifest.permission.VIBRATE}, 1);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE
                        , Manifest.permission.CHANGE_WIFI_STATE
                        , Manifest.permission.ACCESS_WIFI_STATE
                        , Manifest.permission.INTERNET
                        , Manifest.permission.VIBRATE}, 1);
            }
        } else {
            checkConnectivity();
        }
    }

    /**
     * Continue the loading animation and display the new activity when the loading animation has finished
     */
    private void continueLoading() {
        final Intent i = new Intent(this, ScanActivity.class);

        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(final long millisUntilFinished) {

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
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            // The required permissions have not been granted and thus the app cannot function correctly
            // Close the app
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.string_give_manual_permissions);
            builder.setCancelable(false);

            builder.setPositiveButton(R.string.yes,
                    (dialog, id) -> {
                        dialog.cancel();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        permissionResultLaunch.launch(intent);
                    });

            builder.setNegativeButton(R.string.no,
                    (dialog, id) -> {
                        Toast.makeText(LoadingActivity.this, R.string.string_networkpermissions_required, Toast.LENGTH_SHORT).show();
                        finish();
                        dialog.cancel();
                    });

            final AlertDialog alert = builder.create();
            alert.show();
            return;
        }
        // Check if Wifi connection is available
        checkConnectivity();
    }

    /**
     * Check whether an internet connection is available
     */
    private void checkConnectivity() {
        // Check if an internet connection is available
        if (!networkUtils.hasNetworkConnection()) {
            if (!networkUtils.isWifiEnabled()) {
                wifiConfirmationCheck();
            } else {
                delayedWifiCheck();
            }
        } else {
            continueLoading();
        }
    }

    /**
     * Request the user to enable the Wifi of the device
     */
    private void wifiConfirmationCheck() {
        final DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE ->
                        wifiResultLaunch.launch(new Intent(Settings.ACTION_WIFI_SETTINGS));
                case DialogInterface.BUTTON_NEGATIVE -> finish();
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(LoadingActivity.this);
        builder.setMessage(R.string.string_enable_wifi)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener)
                .show();
    }

    /**
     * Check whether an internet connection is available after a certain amount of time has passed
     */
    private void delayedWifiCheck() {
        if (networkUtils.hasNetworkConnection()) {
            // Load the next screen because an internet connection is available
            continueLoading();
        } else {
            // Close app because no internet connection is available
            Toast.makeText(LoadingActivity.this, R.string.string_no_internet, Toast.LENGTH_LONG).show();
            finish();
        }
    }
}

package com.codedead.advancedportchecker.domain.controller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;

public final class UtilController {

    /**
     * Open a website in the default browser
     * @param context The context that can be used to start the activity
     * @param url The URL that should be opened
     */
    public static void openWebsite(Context context, String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Display an alert to the user
     * @param context The context that can be used to display the alert
     * @param message The message that needs to be displayed to the user
     */
    public static void showAlert(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton(
                android.R.string.ok,
                (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Check whether a string is a valid WEB URL or IP address
     * @param address The address that needs to be verified
     * @return True if the input is a valid WEB URL or IP address
     */
    static boolean isValidAddress(String address) {
        return Patterns.WEB_URL.matcher(address).matches() || Patterns.IP_ADDRESS.matcher(address).matches();
    }
}

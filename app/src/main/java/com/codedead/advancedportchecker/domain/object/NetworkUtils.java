package com.codedead.advancedportchecker.domain.object;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;

public final class NetworkUtils {

    private final WifiManager wifiManager;
    private final Context context;

    /**
     * Initialize a new NetworkUtils
     * @param context The context that can be used to access device information
     */
    public NetworkUtils(final Context context) {
        if (context == null)
            throw new NullPointerException("Context cannot be null!");

        this.context = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * Check whether a network connection is available
     * @return True if a network connection is available, otherwise false
     */
    public boolean hasNetworkConnection() {
        final ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            final NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (capabilities != null) {
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            }
        }
        return false;
    }

    /**
     * Check whether Wifi is enabled or not
     * @return True if Wifi is enabled, otherwise false
     */
    public boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }
}

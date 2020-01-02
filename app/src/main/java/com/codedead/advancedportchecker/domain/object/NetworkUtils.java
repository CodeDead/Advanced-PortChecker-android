package com.codedead.advancedportchecker.domain.object;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

public final class NetworkUtils {

    private WifiManager wifiManager;
    private Context context;

    /**
     * Initialize a new NetworkUtils
     * @param context The context that can be used to access device information
     */
    public NetworkUtils(Context context) {
        if (context == null) throw new NullPointerException("Context cannot be null!");

        this.context = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * Check whether a network connection is available
     * @return True if a network connection is available, otherwise false
     */
    public boolean hasNetworkConnection() {
        final ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                final NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                }
            }
        } else {
            if (cm != null) {
                final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
                }
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

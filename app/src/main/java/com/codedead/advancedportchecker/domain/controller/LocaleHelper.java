package com.codedead.advancedportchecker.domain.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import androidx.preference.PreferenceManager;

import java.util.Locale;

public class LocaleHelper {

    /**
     * Method that is called when a Context object is attached
     *
     * @param context The Context object that is attached
     * @return The Context object containing correct resource properties
     */
    public static Context onAttach(Context context) {
        final String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang);
    }

    /**
     * Method that is called when a Context object is attached
     *
     * @param context         The Context object that is attached
     * @param defaultLanguage The default language code
     * @return The Context object containing correct resource properties
     */
    public static Context onAttach(Context context, String defaultLanguage) {
        final String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    /**
     * Set the Locale depending on the language
     *
     * @param context  The Context object that can be used to change resource properties
     * @param language The language that should be attached
     * @return The Context object containing correct resource properties
     */
    public static Context setLocale(Context context, String language) {
        persist(context, language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }

        return updateResourcesLegacy(context, language);
    }

    /**
     * Get the language code that is stored in the shared preferences
     *
     * @param context         The Context object that can be used to load the preferences
     * @param defaultLanguage The default language code
     * @return The language code that is stored in the shared preferences
     */
    private static String getPersistedData(Context context, String defaultLanguage) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("language", defaultLanguage);
    }

    /**
     * Store the language code in the shared preferences
     *
     * @param context  The Context object that can be used to store preferences
     * @param language The language code that should be stored
     */
    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("language", language);
        editor.apply();
    }

    /**
     * Update the resources of a Context object
     *
     * @param context  The Context object that should be configured
     * @param language The language code that should be applied to the Context object
     * @return The Context object containing correct resource properties
     */
    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        final Locale locale = new Locale(language);
        Locale.setDefault(locale);

        final Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);

        return context.createConfigurationContext(configuration);
    }

    /**
     * Update the resources of a Context object
     *
     * @param context  The Context object that should be configured
     * @param language The language code that should be applied to the Context object
     * @return The Context object containing correct resource properties
     */
    private static Context updateResourcesLegacy(Context context, String language) {
        final Locale locale = new Locale(language);
        Locale.setDefault(locale);

        final Resources resources = context.getResources();
        final Configuration configuration = resources.getConfiguration();

        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }
}

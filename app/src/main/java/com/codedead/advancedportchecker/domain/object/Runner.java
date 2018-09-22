package com.codedead.advancedportchecker.domain.object;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.codedead.advancedportchecker.domain.controller.LocaleHelper;

public final class Runner extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(base);
        super.attachBaseContext(LocaleHelper.onAttach(base, sharedPref.getString("appLanguage", "en")));
    }
}

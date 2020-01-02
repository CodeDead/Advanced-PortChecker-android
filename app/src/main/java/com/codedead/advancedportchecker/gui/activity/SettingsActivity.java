package com.codedead.advancedportchecker.gui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.codedead.advancedportchecker.R;
import com.codedead.advancedportchecker.domain.controller.LocaleHelper;
import com.codedead.advancedportchecker.domain.controller.UtilController;

import static android.content.pm.PackageManager.GET_META_DATA;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        LocaleHelper.setLocale(this, sharedPreferences.getString("appLanguage", "en"));

        resetTitle();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs, key) -> {
        if (key.equals("appLanguage")) {
            LocaleHelper.setLocale(getApplicationContext(), prefs.getString("appLanguage", "en"));
            recreate();
        } else if (key.equals("socketTimeout")) {
            try {
                Integer.parseInt(prefs.getString("socketTimeout", "200"));
            } catch (NumberFormatException ex) {
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString("socketTimeout", "200");
                editor.apply();
            }
        }
    };

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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleHelper.onAttach(getBaseContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            final EditTextPreference editTextPreference = getPreferenceManager().findPreference("socketTimeout");
            if (editTextPreference != null) {
                editTextPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
            }
        }
    }
}

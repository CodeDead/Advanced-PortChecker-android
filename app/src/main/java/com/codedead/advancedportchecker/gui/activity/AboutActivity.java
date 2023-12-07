package com.codedead.advancedportchecker.gui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;

import com.codedead.advancedportchecker.R;
import com.codedead.advancedportchecker.domain.controller.LocaleHelper;
import com.codedead.advancedportchecker.domain.controller.UtilController;
import com.codedead.advancedportchecker.gui.fragment.AboutFragment;
import com.codedead.advancedportchecker.gui.fragment.InfoFragment;

import static android.content.pm.PackageManager.GET_META_DATA;

public final class AboutActivity extends AppCompatActivity {

    private Fragment aboutFragment;
    private Fragment infoFragment;

    private int selectedFragment;

    private static final String fragmentKey = "selectedFragment";

    private final BottomNavigationView.OnItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_about_about) {
            switchFragment(0);
            return true;
        } else if (itemId == R.id.nav_about_help) {
            switchFragment(1);
            return true;
        }
        return false;
    };

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
    protected void onCreate(final Bundle savedInstanceState) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        LocaleHelper.setLocale(this, sharedPreferences.getString("appLanguage", "en"));

        super.onCreate(savedInstanceState);
        resetTitle();
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        aboutFragment = new AboutFragment();
        infoFragment = new InfoFragment();

        final BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener);
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
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        switchFragment(selectedFragment);
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putInt(fragmentKey, selectedFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        selectedFragment = savedInstanceState.getInt(fragmentKey, 0);
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Switch the active fragment
     *
     * @param selected The index of the fragment that should be displayed
     */
    private void switchFragment(final int selected) {
        final FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        Fragment fragment;

        selectedFragment = selected;
        if (selected == 1) {
            fragment = infoFragment;
        } else {
            fragment = aboutFragment;
        }

        trans.replace(R.id.content, fragment);
        trans.commit();
    }
}

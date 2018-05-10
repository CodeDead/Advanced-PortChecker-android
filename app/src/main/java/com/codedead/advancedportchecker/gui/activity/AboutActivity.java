package com.codedead.advancedportchecker.gui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.codedead.advancedportchecker.R;
import com.codedead.advancedportchecker.gui.fragment.AboutFragment;
import com.codedead.advancedportchecker.gui.fragment.InfoFragment;

public class AboutActivity extends AppCompatActivity {

    private Fragment aboutFragment;
    private Fragment infoFragment;

    private int selectedFragment;

    private static final String fragmentKey = "selectedFragment";

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_about_about:
                    switchFragment(0);
                    return true;
                case R.id.nav_about_help:
                    switchFragment(1);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        aboutFragment = new AboutFragment();
        infoFragment = new InfoFragment();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(fragmentKey, selectedFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        selectedFragment = savedInstanceState.getInt(fragmentKey, 0);
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void switchFragment(int selected) {
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        Fragment fragment;

        selectedFragment = selected;
        switch (selected) {
            default:
            case 0:
                fragment = aboutFragment;
                break;
            case 1:
                fragment = infoFragment;
                break;
        }

        trans.replace(R.id.content, fragment);
        trans.commit();
    }

}

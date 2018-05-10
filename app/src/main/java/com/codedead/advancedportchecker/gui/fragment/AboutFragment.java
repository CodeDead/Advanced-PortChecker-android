package com.codedead.advancedportchecker.gui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.codedead.advancedportchecker.R;
import com.codedead.advancedportchecker.domain.controller.UtilController;

public class AboutFragment extends Fragment implements View.OnClickListener {

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        ImageButton btnFacebook = v.findViewById(R.id.BtnFacebook);
        ImageButton btnTwitter = v.findViewById(R.id.BtnTwitter);
        ImageButton btnWebsite = v.findViewById(R.id.BtnWebsiteAbout);

        btnFacebook.setOnClickListener(this);
        btnTwitter.setOnClickListener(this);
        btnWebsite.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        if (getContext() == null) return;

        switch (v.getId()) {
            case R.id.BtnFacebook:
                UtilController.openWebsite(getContext(), "https://facebook.com/deadlinecodedead/");
                break;
            case R.id.BtnTwitter:
                UtilController.openWebsite(getContext(), "https://twitter.com/C0DEDEAD");
                break;
            case R.id.BtnWebsiteAbout:
                UtilController.openWebsite(getContext(), "https://codedead.com/");
                break;
        }
    }
}

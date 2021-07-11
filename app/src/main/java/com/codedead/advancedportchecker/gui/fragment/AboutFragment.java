package com.codedead.advancedportchecker.gui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.codedead.advancedportchecker.R;
import com.codedead.advancedportchecker.domain.controller.UtilController;

public final class AboutFragment extends Fragment implements View.OnClickListener {

    /**
     * Initialize a new AboutFragment
     */
    public AboutFragment() {
        // Default constructor
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_about, container, false);

        final ImageButton btnFacebook = v.findViewById(R.id.BtnFacebook);
        final ImageButton btnTwitter = v.findViewById(R.id.BtnTwitter);
        final ImageButton btnWebsite = v.findViewById(R.id.BtnWebsiteAbout);
        final TextView txtAbout = v.findViewById(R.id.TxtAbout);

        btnFacebook.setOnClickListener(this);
        btnTwitter.setOnClickListener(this);
        btnWebsite.setOnClickListener(this);
        txtAbout.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }

    @Override
    public void onClick(final View v) {
        if (getContext() == null)
            return;

        int id = v.getId();
        if (id == R.id.BtnFacebook) {
            UtilController.openWebsite(getContext(), "https://facebook.com/deadlinecodedead/");
        } else if (id == R.id.BtnTwitter) {
            UtilController.openWebsite(getContext(), "https://twitter.com/C0DEDEAD");
        } else if (id == R.id.BtnWebsiteAbout) {
            UtilController.openWebsite(getContext(), "https://codedead.com/");
        }
    }
}

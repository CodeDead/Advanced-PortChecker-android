package com.codedead.advancedportchecker.gui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.codedead.advancedportchecker.R;
import com.codedead.advancedportchecker.domain.controller.UtilController;

public class InfoFragment extends Fragment implements View.OnClickListener {

    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info, container, false);

        ImageButton btnWebsite = v.findViewById(R.id.BtnWebsiteInfo);
        ImageButton btnSupport = v.findViewById(R.id.BtnSendMail);

        btnWebsite.setOnClickListener(this);
        btnSupport.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.BtnWebsiteInfo:
                if (getContext() == null) return;
                UtilController.openWebsite(getContext(), "https://codedead.com/?page_id=145");
                break;
            case R.id.BtnSendMail:
                if (getActivity() == null) return;
                ShareCompat.IntentBuilder.from(getActivity())
                        .setType("message/rfc822")
                        .addEmailTo("admin@codedead.com")
                        .setSubject("Advanced PortChecker - Android")
                        .setText("")
                        .setChooserTitle(getString(R.string.text_send_mail))
                        .startChooser();
                break;
        }
    }
}

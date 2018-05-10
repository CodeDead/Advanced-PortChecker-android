package com.codedead.advancedportchecker.domain.controller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public final class UtilController {

    public static void openWebsite(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }
}

package com.wilsovargas.demos.appvisita.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Wilson on 12/14/2017.
 */

public class Util {

    public static void abrirMapa(float lat, float lon, Context contexto) {
        String urlMaps = "http://maps.google.com/maps?daddr=" + lat + "," + lon
                + "&mode=driving";
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(urlMaps));
        intent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity");

        contexto.startActivity(intent);

    }

    public static void abrirCamara(String url, Context context) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse(url), "application/ar-json");
        context.startActivity(i);
    }
}

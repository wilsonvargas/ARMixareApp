
package org.ar.data.convert;

import android.util.Log;

import org.ar.ArView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ar.POIMarker;
import org.ar.data.DataHandler;
import org.ar.lib.HtmlUnescape;
import org.ar.lib.marker.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * A data processor for custom urls or data, Responsible for converting raw data (to json and
 * then) to marker data.
 *
 * @author A. Egal
 */
public class ArDataProcessor extends DataHandler implements DataProcessor {

    public static final int MAX_JSON_OBJECTS = 1000;

    @Override
    public String[] getUrlMatch() {
        String[] str = new String[0]; //only use this data source if all the others don't match
        return str;
    }

    @Override
    public String[] getDataMatch() {
        String[] str = new String[0]; //only use this data source if all the others don't match
        return str;
    }

    @Override
    public boolean matchesRequiredType(String type) {
        return true; //this datasources has no required type, it will always match.
    }

    @Override
    public List<Marker> load(String rawData, int taskId, int colour) throws JSONException {
        List<Marker> markers = new ArrayList<Marker>();
        JSONObject root = convertToJSON(rawData);
        JSONArray dataArray = root.getJSONArray("results");
        int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

        for (int i = 0; i < top; i++) {
            JSONObject jo = dataArray.getJSONObject(i);

            Marker ma = null;
            if (jo.has("title") && jo.has("lat") && jo.has("lng")
                    && jo.has("elevation")) {

                String id = "";
                if (jo.has("id"))
                    id = jo.getString("id");

                Log.v(ArView.TAG, "processing Ar JSON object");
                String link = null;

                if (jo.has("has_detail_page") && jo.getInt("has_detail_page") != 0 && jo.has
                        ("webpage"))
                    link = jo.getString("webpage");

                ma = new POIMarker(
                        id,
                        HtmlUnescape.unescapeHTML(jo.getString("title"), 0),
                        jo.getDouble("lat"),
                        jo.getDouble("lng"),
                        jo.getDouble("elevation"),
                        link,
                        taskId, colour);
                markers.add(ma);
            }
        }
        return markers;
    }

    private JSONObject convertToJSON(String rawData) {
        try {
            return new JSONObject(rawData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}

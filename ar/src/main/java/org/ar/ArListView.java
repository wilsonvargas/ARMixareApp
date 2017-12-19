
package org.ar;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.ar.data.DataHandler;
import org.ar.data.DataSourceList;
import org.ar.lib.ArUtils;
import org.ar.lib.marker.Marker;
import org.ar.map.ArMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * This class holds vectors with informaction about sources, their description
 * and whether they have been selected.
 */
public class ArListView extends ListActivity {

    public static List<Marker> searchResultMarkers;
    public static List<Marker> originalMarkerList;
    /*
     * private ArContext ArContext; private ListItemAdapter adapter; private
     * static Context ctx;
     */
    private static String searchQuery = "";
    private static SpannableString underlinedTitle;
    private Vector<SpannableString> listViewMenu;
    private Vector<String> selectedItemURL;
    private Vector<String> dataSourceMenu;
    private Vector<String> dataSourceDescription;
    private Vector<Boolean> dataSourceChecked;
    private Vector<Integer> dataSourceIcon;
    private DataView dataView;

    public static String getSearchQuery() {
        return searchQuery;
    }

    public static void setSearchQuery(String query) {
        searchQuery = query;
    }

    public Vector<String> getDataSourceMenu() {
        return dataSourceMenu;
    }

    public Vector<String> getDataSourceDescription() {
        return dataSourceDescription;
    }

    public Vector<Boolean> getDataSourceChecked() {
        return dataSourceChecked;
    }

    public Vector<Integer> getDataSourceIcon() {
        return dataSourceIcon;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataView = ArView.getDataView();

        selectedItemURL = new Vector<String>();
        listViewMenu = new Vector<SpannableString>();
        DataHandler jLayer = dataView.getDataHandler();
        if (dataView.isFrozen() && jLayer.getMarkerCount() > 0) {
            selectedItemURL.add("search");
        }
        /* add all marker items to a title and a URL Vector */
        for (int i = 0; i < jLayer.getMarkerCount(); i++) {
            Marker ma = jLayer.getMarker(i);
            if (ma.isActive()) {
                if (ma.getURL() != null) {
                    /* Underline the title if website is available */
                    underlinedTitle = new SpannableString(ma.getTitle());
                    underlinedTitle.setSpan(new UnderlineSpan(), 0,
                            underlinedTitle.length(), 0);
                    listViewMenu.add(underlinedTitle);
                } else {
                    listViewMenu.add(new SpannableString(ma.getTitle()));
                }
                /* the website for the corresponding title */
                if (ma.getURL() != null)
                    selectedItemURL.add(ma.getURL());
				/* if no website is available for a specific title */
                else
                    selectedItemURL.add("");
            }

            if (dataView.isFrozen()) {

                TextView searchNotificationTxt = new TextView(this);
                searchNotificationTxt.setVisibility(View.VISIBLE);
                searchNotificationTxt
                        .setText(getString(R.string.search_active_1) + " "
                                + DataSourceList.getDataSourcesStringList()
                                + getString(R.string.search_active_2));
                searchNotificationTxt.setWidth(ArView.getdWindow().getWidth());

                searchNotificationTxt.setPadding(10, 2, 0, 0);
                searchNotificationTxt.setBackgroundColor(Color.DKGRAY);
                searchNotificationTxt.setTextColor(Color.WHITE);

                getListView().addHeaderView(searchNotificationTxt);

            }

            setListAdapter(new ArrayAdapter<SpannableString>(this,
                    android.R.layout.simple_list_item_1, listViewMenu));
            getListView().setTextFilterEnabled(true);
            break;

        }
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doArSearch(query);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void doArSearch(String query) {
        DataHandler jLayer = dataView.getDataHandler();
        if (!dataView.isFrozen()) {
            originalMarkerList = jLayer.getMarkerList();
            ArMap.originalMarkerList = jLayer.getMarkerList();
        }
        originalMarkerList = jLayer.getMarkerList();
        searchResultMarkers = new ArrayList<Marker>();
        Log.d("SEARCH----------------0", "" + query);
        setSearchQuery(query);

        selectedItemURL = new Vector<String>();
        listViewMenu = new Vector<SpannableString>();
        for (int i = 0; i < jLayer.getMarkerCount(); i++) {
            Marker ma = jLayer.getMarker(i);

            if (ma.getTitle().toLowerCase().indexOf(searchQuery.toLowerCase()) != -1) {
                searchResultMarkers.add(ma);
                listViewMenu.add(new SpannableString(ma.getTitle()));
				/* the website for the corresponding title */
                if (ma.getURL() != null)
                    selectedItemURL.add(ma.getURL());
				/* if no website is available for a specific title */
                else
                    selectedItemURL.add("");
            }
        }
        if (listViewMenu.size() == 0) {
            Toast.makeText(this,
                    getString(R.string.search_failed_notification),
                    Toast.LENGTH_LONG).show();
        } else {
            jLayer.setMarkerList(searchResultMarkers);
            dataView.setFrozen(true);
            finish();
            Intent intent1 = new Intent(this, ArListView.class);
            startActivityForResult(intent1, 42);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        clickOnListView(position);
    }

    public void clickOnListView(int position) {
		/* if no website is available for this item */
        String selectedURL = position < selectedItemURL.size() ? selectedItemURL
                .get(position) : null;
        if (selectedURL == null || selectedURL.length() <= 0)
            Toast.makeText(this, getString(R.string.no_website_available),
                    Toast.LENGTH_LONG).show();
        else if ("search".equals(selectedURL)) {
            dataView.setFrozen(false);
            dataView.getDataHandler().setMarkerList(originalMarkerList);
            finish();
            Intent intent1 = new Intent(this, ArListView.class);
            startActivityForResult(intent1, 42);
        } else {
            try {
                if (selectedURL.startsWith("webpage")) {
                    String newUrl = ArUtils.parseAction(selectedURL);
                    dataView.getContext().getWebContentManager()
                            .loadWebPage(newUrl, this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int base = Menu.FIRST;

		/* define menu items */
        MenuItem item1 = menu.add(base, base, base,
                getString(R.string.menu_item_3));
        MenuItem item2 = menu.add(base, base + 1, base + 1,
                getString(R.string.map_menu_cam_mode));
		/* assign icons to the menu items */
        item1.setIcon(android.R.drawable.ic_menu_mapmode);
        item2.setIcon(android.R.drawable.ic_menu_camera);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
		/* Map View */
            case 1:
                createArMap();
                finish();
                break;
		/* back to Camera View */
            case 2:
                finish();
                break;
        }
        return true;
    }

    public void createArMap() {
        Intent intent2 = new Intent(ArListView.this, ArMap.class);
        startActivityForResult(intent2, 20);
    }
}

/**
 * The ListItemAdapter is can store properties of list items, like background or
 * text color
 */
class ListItemAdapter extends BaseAdapter {

    public static int itemPosition = 0;
    static ViewHolder holder;
    private ArListView arListView;
    private LayoutInflater myInflater;
    private int[] bgcolors = new int[]{0, 0, 0, 0, 0};
    private int[] textcolors = new int[]{Color.WHITE, Color.WHITE,
            Color.WHITE, Color.WHITE, Color.WHITE};
    private int[] descriptioncolors = new int[]{Color.GRAY, Color.GRAY,
            Color.GRAY, Color.GRAY, Color.GRAY};

    public ListItemAdapter(ArListView arListView) {
        this.arListView = arListView;
        myInflater = LayoutInflater.from(arListView);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        itemPosition = position;
        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.main, null);

            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.list_text);
            holder.description = (TextView) convertView
                    .findViewById(R.id.description_text);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setPadding(20, 8, 0, 0);
        holder.description.setPadding(20, 40, 0, 0);

        holder.text.setText(arListView.getDataSourceMenu().get(position));
        holder.description.setText(arListView.getDataSourceDescription().get(
                position));

        int colorPos = position % bgcolors.length;
        convertView.setBackgroundColor(bgcolors[colorPos]);
        holder.text.setTextColor(textcolors[colorPos]);
        holder.description.setTextColor(descriptioncolors[colorPos]);

        return convertView;
    }

    public void changeColor(int index, int bgcolor, int textcolor) {
        if (index < bgcolors.length) {
            bgcolors[index] = bgcolor;
            textcolors[index] = textcolor;
        } else
            Log.d("Color Error", "too large index");
    }

    public void colorSource(String source) {
        for (int i = 0; i < bgcolors.length; i++) {
            bgcolors[i] = 0;
            textcolors[i] = Color.WHITE;
        }

        if (source.equals("Wikipedia"))
            changeColor(0, Color.WHITE, Color.DKGRAY);
        else if (source.equals("Twitter"))
            changeColor(1, Color.WHITE, Color.DKGRAY);
        else if (source.equals("Buzz"))
            changeColor(2, Color.WHITE, Color.DKGRAY);
        else if (source.equals("OpenStreetMap"))
            changeColor(3, Color.WHITE, Color.DKGRAY);
        else if (source.equals("OwnURL"))
            changeColor(4, Color.WHITE, Color.DKGRAY);
        else if (source.equals("ARENA"))
            changeColor(5, Color.WHITE, Color.DKGRAY);
    }

    @Override
    public int getCount() {
        return arListView.getDataSourceMenu().size();
    }

    @Override
    public Object getItem(int position) {
        return this;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView text;
        TextView description;
    }
}

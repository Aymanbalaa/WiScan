package com.example.nsgs_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WiFiActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;

    private RecyclerView recyclerView;
    private NetworkAdapter networkAdapter;
    private List<Network> networkList;
    private List<Network> filteredNetworkList; // List for filtered networks
    private TextView totalNetworksTextView;
    private Handler handler;
    private Runnable fetchTask;
    private int fetchInterval; // This variable will hold the fetch interval in milliseconds
    private Comparator<Network> currentComparator; // Save the current comparator
    private String currentFilter; // Save the current filter
    private boolean isFilteringMode = false; // Track whether filtering mode is active
    private Button btnExportCsv, btnScrollBottom;
    private boolean isAtBottom = false; // Track the current scroll position
    private String currentQuery = ""; // This will hold the current search query

    private static final String PREFS_NAME = "WiFiActivityPrefs"; // USED TO SAVE POS IN SHARED PREFS
    private static final String SCROLL_POSITION_KEY = "scroll_position";
    private static final String SCROLL_OFFSET_KEY = "scroll_offset";
    private static final String NETWORK_LIST_KEY = "network_list";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        ThemeSelection.themeInitializer(findViewById(R.id.wifi_activity_layout),this);

        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        totalNetworksTextView = findViewById(R.id.totalNetworksTextView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnExportCsv = findViewById(R.id.btn_export_csv);
        btnScrollBottom = findViewById(R.id.btn_scroll_bottom);

        handler = new Handler();

        // Retrieve the fetch interval from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
        String fetchIntervalString = preferences.getString("fetch_unit", "10");
        int fetchIntervalSeconds = Integer.parseInt(fetchIntervalString);
        fetchInterval = fetchIntervalSeconds * 1000; // Convert to milliseconds

        fetchTask = new Runnable() {
            @Override
            public void run() {
                saveScrollPosition(); // Save the scroll position before fetching data
                fetchNetworks();
                handler.postDelayed(this, fetchInterval);
            }
        };

        fetchNetworks(); // Initial fetch on create

        handler.postDelayed(fetchTask, fetchInterval); // Schedule fetch every interval

        // Check for write permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }

        btnExportCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportToCsv();
            }
        });

        btnScrollBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAtBottom) {
                    recyclerView.scrollToPosition(0);
                    btnScrollBottom.setText(getString(R.string.button_scroll_to_bottom));
                } else {
                    recyclerView.scrollToPosition(networkList.size() - 1);
                    btnScrollBottom.setText(getString(R.string.button_scroll_to_top));
                }
                isAtBottom = !isAtBottom;
            }
        });

        // Setup SearchView
        SearchView searchView = findViewById(R.id.search_ssid);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                filterNetworks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                filterNetworks(newText);
                return true;
            }
        });
       // reverseList(true);
    }

    /*
    private void reverseList(boolean reverse){
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        assert linearLayoutManager != null;
        linearLayoutManager.setReverseLayout(reverse);
    }


     */
    @Override
    protected void onPause() {
        super.onPause();
        // Save the current scroll position and offset
        saveScrollPosition();
    }



    @Override
    protected void onResume() {
        super.onResume();
        // Check if there is a filter state passed back from NetworkDetailActivity
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("currentFilter")) {
            currentFilter = intent.getStringExtra("currentFilter");
            isFilteringMode = currentFilter != null;
        }
        applyCurrentSortOrFilter();

        recyclerView.setAdapter(networkAdapter);

        // Restore the scroll position and offset
        restoreScrollPosition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fetchTask);
    }

    private void fetchNetworks() {
        NetworkManager networkManager = NetworkManager.getInstance(this);
        networkManager.fetchNetworks();
        networkList = networkManager.getNetworkList();
        applyCurrentSortOrFilter();
        invalidateOptionsMenu(); // Refresh Filter Listtt
    }

    private void exportToCsv() {
        File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File csvFile = new File(downloadFolder, "networks.csv");

        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.append("SSID,BSSID,Security,Coordinates,Neighborhood,Postal Code\n");
            for (Network network : networkList) {
                writer.append(escapeCsvValue(network.getSsid()))
                        .append(',')
                        .append(escapeCsvValue(network.getBssid()))
                        .append(',')
                        .append(escapeCsvValue(network.getSecurity())) // main reason why we need to escape before
                        .append(',')
                        .append(escapeCsvValue(String.valueOf(network.getCoordinates())))
                        .append(',')
                        .append(escapeCsvValue(String.valueOf(network.getNeighborhood())))
                        .append(',')
                        .append(escapeCsvValue(String.valueOf(network.getPostalCode())))
                        .append('\n');
            }
            Toast.makeText(this, "CSV file exported to Downloads folder", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to export CSV file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with exporting
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_wifi, menu);

        // Clear existing items in the filter submenu before adding new ones AVOIDING DUPLICATES
        SubMenu filterSubMenu = menu.findItem(R.id.action_filter_submenu).getSubMenu();
        filterSubMenu.clear(); // clearrrr
        Set<String> securityProtocols = getUniqueSecurityProtocols(networkList); // refill the list and get all protocols

        for (String protocol : securityProtocols) {
            filterSubMenu.add(Menu.NONE, Menu.NONE, Menu.NONE, protocol).setOnMenuItemClickListener(item -> {
                isFilteringMode = true;
                filterNetworkList(protocol);
                return true;
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {// Back button
            navigateToMainActivity();
            return true;
        } else if (itemId == R.id.sort_by_ssid) {// Sort by SSID
            isFilteringMode = false;
            sortNetworkList(Comparator.comparing(Network::getSsid, (ssid1, ssid2) -> {
                boolean ssid1HasNumbers = ssid1.matches(".*\\d.*");
                boolean ssid2HasNumbers = ssid2.matches(".*\\d.*");
                if (ssid1HasNumbers && !ssid2HasNumbers) return 1;
                if (!ssid1HasNumbers && ssid2HasNumbers) return -1;
                return ssid1.compareToIgnoreCase(ssid2);
            }));
            return true;
        } else if (itemId == R.id.sort_by_security) {// Sort by Security Protocol
            isFilteringMode = false;
            sortNetworkList(Comparator.comparing(Network::getSecurity, String::compareToIgnoreCase));
            return true;
        }else if(itemId == R.id.action_default_view){
            resetFiltersAndSort();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void sortNetworkList(Comparator<Network> comparator) {
        currentComparator = comparator;
        if (networkList != null) {
            filteredNetworkList = new ArrayList<>(networkList);
            filteredNetworkList.sort(comparator);
            updateAdapter(filteredNetworkList);
          //  reverseList(false);
        }
    }

    private void filterNetworkList(String securityType) {
        currentFilter = securityType;
        if (networkList != null) {
            if (securityType == null) {
                filteredNetworkList = new ArrayList<>(networkList); // No filter, show all networks
            } else {
                filteredNetworkList = networkList.stream()
                        .filter(network -> network.getSecurity().equalsIgnoreCase(securityType))
                        .collect(Collectors.toList());
            }
          //  reverseList(false);
            updateAdapter(filteredNetworkList);
        }
    }

    private void applyCurrentSortOrFilter() {
        if (networkList == null) {
            return;
        }

        if (isFilteringMode && currentFilter != null) {
            // Apply the current filter
            filteredNetworkList = networkList.stream()
                    .filter(network -> network.getSecurity().equalsIgnoreCase(currentFilter))
                    .collect(Collectors.toList());
           // reverseList(false);
        } else if (!isFilteringMode && currentComparator != null) {
            // Apply the current sort
            filteredNetworkList = new ArrayList<>(networkList);
            filteredNetworkList.sort(currentComparator);
         //   reverseList(false);
        } else {
            filteredNetworkList = new ArrayList<>(networkList);
          //  reverseList(true);
        }

        updateAdapter(filteredNetworkList);
    }

    private void resetFiltersAndSort() { // no need for function
        isFilteringMode = false;
        currentFilter = null;
        currentComparator = null;
        applyCurrentSortOrFilter();
    }

    @SuppressLint("StringFormatMatches")
    private void updateAdapter(List<Network> networkList) {
        // Update the total networks count (Top Page)
        totalNetworksTextView.setText(getString(R.string.total_networks_label, networkList.size()));

        if (networkAdapter == null) {
            networkAdapter = new NetworkAdapter(WiFiActivity.this, networkList, currentFilter);
            recyclerView.setAdapter(networkAdapter);
        } else {
            networkAdapter.updateNetworkList(networkList);
            networkAdapter.notifyDataSetChanged();
        }
    }

    private void saveScrollPosition() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            int scrollPosition = layoutManager.findFirstVisibleItemPosition();
            int scrollOffset = 0;
            if (scrollPosition != RecyclerView.NO_POSITION) {
                View firstVisibleView = layoutManager.findViewByPosition(scrollPosition);
                if (firstVisibleView != null) {
                    scrollOffset = firstVisibleView.getTop();
                }
            }
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(SCROLL_POSITION_KEY, scrollPosition);
            editor.putInt(SCROLL_OFFSET_KEY, scrollOffset);
            editor.apply();
        }
    }

    private void restoreScrollPosition() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE); // causes slight offset that we did not fix
        int scrollPosition = preferences.getInt(SCROLL_POSITION_KEY, RecyclerView.NO_POSITION);
        int scrollOffset = preferences.getInt(SCROLL_OFFSET_KEY, 0);
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null && scrollPosition != RecyclerView.NO_POSITION) {
            layoutManager.scrollToPositionWithOffset(scrollPosition, scrollOffset);
        }
    }

    private void filterNetworks(String query) {
        filteredNetworkList.clear();
        if (TextUtils.isEmpty(query)) {
            filteredNetworkList.addAll(networkList);
        } else {
            for (Network network : networkList) {
                if (network.getSsid().toLowerCase().contains(query.toLowerCase())) {
                    filteredNetworkList.add(network);
                }
            }
        }
        if (networkAdapter != null) {
            networkAdapter.notifyDataSetChanged();
        }
    }

    private Set<String> getUniqueSecurityProtocols(List<Network> networks) {
        //Function to retrieve the unique protocols from the fetched networks list
        Set<String> securityProtocols = new HashSet<>();
        for (Network network : networks) {
            securityProtocols.add(network.getSecurity());
        }
        return securityProtocols;
    }

    private String escapeCsvValue(String value) { // because of Security = (WEP, XXXX) and coordinates = [34.34,47.74]
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}

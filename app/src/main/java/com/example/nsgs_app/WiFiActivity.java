package com.example.nsgs_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class WiFiActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;

    private RecyclerView recyclerView;
    private NetworkAdapter networkAdapter;
    private List<Network> networkList;
    private List<Network> filteredNetworkList;
    private TextView totalNetworksTextView;
    private Handler handler;
    private Runnable fetchTask;
    private int fetchInterval;
    private Comparator<Network> currentComparator;
    private String currentSortDescription = "Oldest to Newest"; // default sort description
    private String currentFilter;
    private boolean isFilteringMode = false;
    private Button btnExportCsv, btnScroll;
    private boolean isAtBottom = false;
    private String currentQuery = "";
    private boolean isReversedOrder = false;

    private static final String PREFS_NAME = "WiFiActivityPrefs";
    private static final String SCROLL_POSITION_KEY = "scroll_position";
    private static final String SCROLL_OFFSET_KEY = "scroll_offset";
    private static final String NETWORK_LIST_KEY = "network_list";
    private static final String ORDER_KEY = "order_key";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        String currentTheme = ThemeSelection.themeInitializer(findViewById(R.id.wifi_activity_layout), this,this);

        getSupportActionBar().setTitle(getString(R.string.wifi_activity_bar_title));
        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        switch(currentTheme) {
            case "Light":
            case "Clair":
            case "Свет":
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
                break;
        }

        totalNetworksTextView = findViewById(R.id.totalNetworksTextView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnExportCsv = findViewById(R.id.btn_export_csv);
        btnScroll = findViewById(R.id.btn_scroll);

        handler = new Handler();

        SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
        String fetchIntervalString = preferences.getString("fetch_interval", "10");
        int fetchIntervalSeconds = Integer.parseInt(fetchIntervalString);
        fetchInterval = fetchIntervalSeconds * 1000;

        isReversedOrder = preferences.getBoolean(ORDER_KEY, false);

        fetchTask = new Runnable() {
            @Override
            public void run() {
                saveScrollPosition();
                fetchNetworks();
                handler.postDelayed(this, fetchInterval);
            }
        };

        fetchNetworks();
        handler.postDelayed(fetchTask, fetchInterval);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }

        btnExportCsv.setOnClickListener(v -> exportToCsv());

        btnScroll.setOnClickListener(v -> {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                if (isAtBottom) {
                    recyclerView.scrollToPosition(0);
                } else {
                    recyclerView.scrollToPosition(filteredNetworkList.size() - 1); // Use filtered list size
                }
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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();
                    isAtBottom = lastVisiblePosition == filteredNetworkList.size() - 1; // Use filtered list size
                    updateScrollButton();
                }
            }
        });
    }

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
        networkManager.fetchNetworks("http://217.15.171.225:5000/get_all_networks", false, isReversedOrder);
        networkList = networkManager.getNetworkList();

        applyCurrentSortOrFilter();

        // Reapply search query after refresh
        if (!TextUtils.isEmpty(currentQuery)) {
            filterNetworks(currentQuery);
        } else {
            updateAdapter(filteredNetworkList);
        }
        invalidateOptionsMenu();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_wifi, menu);
        SubMenu filterSubMenu = menu.findItem(R.id.action_filter_submenu).getSubMenu();
        filterSubMenu.clear();
        Set<String> securityProtocols = getUniqueSecurityProtocols(networkList);
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
        if (itemId == android.R.id.home) {
            navigateToMainActivity();
            return true;
        } else if (itemId == R.id.sort_by_ssid) {
            isFilteringMode = false;
            currentSortDescription = "SSID";
            sortNetworkList(Comparator.comparing(network -> network.getSsid().toLowerCase()));
            return true;
        } else if (itemId == R.id.sort_by_security) {
            isFilteringMode = false;
            currentSortDescription = "Security";
            sortNetworkList(Comparator.comparing(Network::getSecurity, String::compareToIgnoreCase));
            return true;
        } else if (itemId == R.id.action_toggle_order) {
            toggleOrder();
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
        } else if (!isFilteringMode && currentComparator != null) {
            // Apply the current sort
            filteredNetworkList = new ArrayList<>(networkList);
            filteredNetworkList.sort(currentComparator);
        } else {
            filteredNetworkList = new ArrayList<>(networkList);
        }

        updateAdapter(filteredNetworkList);
    }

    @SuppressLint("StringFormatMatches")
    private void updateAdapter(List<Network> networkList) {
        String sortDescription = isFilteringMode ? getString(R.string.filtered_by) + currentFilter : getString(R.string.sorted_by) + currentSortDescription;
        totalNetworksTextView.setText(getString(R.string.total_networks_label, networkList.size(), sortDescription));

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
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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
        Set<String> securityProtocols = new HashSet<>();
        for (Network network : networks) {
            securityProtocols.add(network.getSecurity());
        }
        return securityProtocols;
    }

    private String escapeCsvValue(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void updateScrollButton() {
        if (isAtBottom) {
            btnScroll.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_up, 0, 0, 0);
        } else {
            btnScroll.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_down, 0, 0, 0);
        }
    }

    private void toggleOrder() {
        isReversedOrder = !isReversedOrder;
        if (networkList != null) {
            Collections.reverse(networkList); // Directly reverse the list
        }
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ORDER_KEY, isReversedOrder);
        editor.apply();
        updateAdapter(networkList);
        resetFiltersAndSort();
    }

    private void resetFiltersAndSort() {
        isFilteringMode = false;
        currentFilter = null;
        currentComparator = null;
        currentSortDescription = isReversedOrder ? getString(R.string.newest_to_oldest) : getString(R.string.oldest_to_newest);
        applyCurrentSortOrFilter();
    }
}

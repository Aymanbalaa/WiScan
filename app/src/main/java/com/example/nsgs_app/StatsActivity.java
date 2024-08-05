package com.example.nsgs_app;

import static com.example.nsgs_app.NetworkProviderGuesser.getNetworkProvider;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class StatsActivity extends AppCompatActivity {

    private List<Network> networkList;
    private PieChart pieChartProtocols;
    private PieChart pieChartProviders;
    private TextView textViewToRightOfPieTitle;
    private TextView textViewBelowPieTitle;
    private String neighborhood;
    private Button buttonPieChart1;
    private Button buttonPieChart2;
    public Set<String> protocols;

    @SuppressLint({"MissingInflatedId", "StringFormatMatches"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        String currentTheme = ThemeSelection.themeInitializer(findViewById(R.id.activity_stats), this,this);

        getSupportActionBar().setTitle(getString(R.string.stats_bar_title));
        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        switch(currentTheme) {
            case "Warm":
            case "Amical":
            case "Теплый":
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.warm)));

            case "Light":
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_stats), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pieChartProtocols = findViewById(R.id.pie_chart_protocols);
        pieChartProviders = findViewById(R.id.pie_chart_providers);
        textViewToRightOfPieTitle = findViewById(R.id.text_right_of_title);
        textViewBelowPieTitle = findViewById(R.id.text_below_title);
        buttonPieChart1 = findViewById(R.id.button_pie_chart_1);
        buttonPieChart2 = findViewById(R.id.button_pie_chart_2);

        // Fetching network list from NetworkManager
        NetworkManager networkManager = NetworkManager.getInstance(this);
        networkManager.fetchNetworks("http://217.15.171.225:5000/get_all_networks", false);
        networkList = new ArrayList<>(networkManager.getNetworkList()); // Ensure a local copy of the list

        protocols = getUniqueSecurityProtocols(networkList);

        for (Network network : networkList) {
            if (!Objects.equals(network.getNeighborhood(), "Area name not found")) {
                neighborhood = network.getNeighborhood();
                break;
            }
        }

        textViewToRightOfPieTitle.setText(neighborhood);
        textViewBelowPieTitle.setText(getString(R.string.total_networks_label, networkList.size()));

        if (networkList != null && protocols != null) {
            setUpPieChart(pieChartProtocols, "Security Protocols");
            setUpPieChart(pieChartProviders, "Network Providers");
            loadPieChartDataProtocols();
            loadPieChartDataProviders();
            pieChartProtocols.setVisibility(View.VISIBLE);
        } else {
            Log.e("StatsActivity", "Network list or Protocols set is null");
        }

        buttonPieChart1.setOnClickListener(v -> {
            pieChartProtocols.setVisibility(View.VISIBLE);
            pieChartProviders.setVisibility(View.GONE);
        });

        buttonPieChart2.setOnClickListener(v -> {
            pieChartProtocols.setVisibility(View.GONE);
            pieChartProviders.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpPieChart(PieChart pieChart, String centerText) {
        pieChart.setUsePercentValues(true);
        pieChart.setCenterText(centerText);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setCenterTextSize(22f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(40f);
        pieChart.setExtraOffsets(25, 10, 10, 5);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(14f);
        legend.setFormSize(14f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);
        legend.setXEntrySpace(10f);
        legend.setYEntrySpace(15f); // Increased vertical space to avoid overlapping
    }

    private void loadPieChartDataProtocols() {
        pieChartProtocols.setCenterText(getString(R.string.security_protocol));
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        List<String> customLegendEntries = new ArrayList<>();

        for (String protocol : protocols) {
            List<Network> filteredNetworkList = networkList.stream()
                    .filter(network -> network.getSecurity().equalsIgnoreCase(protocol))
                    .collect(Collectors.toList());

            pieEntries.add(new PieEntry(((float) filteredNetworkList.size() / networkList.size()) * 100, protocol));
            customLegendEntries.add(protocol + " (" + String.format("%.1f", ((float) filteredNetworkList.size() / networkList.size()) * 100) + "%)");
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(getColorList(customLegendEntries.size()));

        if (customLegendEntries.size() == pieEntries.size()) {
            pieChartProtocols.setData(new PieData(pieDataSet));
            pieDataSet.setValueTextSize(0f); // Hides the values

            pieChartProtocols.animateXY(2000, 2000); // Faster animation
            setCustomLegendEntries(pieChartProtocols, customLegendEntries);
            pieChartProtocols.invalidate();

            // Request layout pass to fix legend overlap
            pieChartProtocols.requestLayout();
        } else {
            Log.e("StatsActivity", "Mismatch between legend entries and pie chart data entries.");
        }
    }

    private void loadPieChartDataProviders() {
        pieChartProviders.setCenterText(getString(R.string.network_provider));
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        List<String> customLegendEntries = new ArrayList<>();

        Set<String> providers = getUniqueProviders(networkList);
        for (String provider : providers) {
            List<Network> filteredNetworkList = networkList.stream()
                    .filter(network -> getNetworkProvider(network.getSsid()).equalsIgnoreCase(provider))
                    .collect(Collectors.toList());

            pieEntries.add(new PieEntry(((float) filteredNetworkList.size() / networkList.size()) * 100, provider));
            customLegendEntries.add(provider + " (" + String.format("%.1f", ((float) filteredNetworkList.size() / networkList.size()) * 100) + "%)");
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(getColorList(customLegendEntries.size()));

        if (customLegendEntries.size() == pieEntries.size()) {
            pieChartProviders.setData(new PieData(pieDataSet));
            pieDataSet.setValueTextSize(0f); // Hides the values

            pieChartProviders.animateXY(2000, 2000); // Faster animation
            setCustomLegendEntries(pieChartProviders, customLegendEntries);
            pieChartProviders.invalidate();

            // Request layout pass to fix legend overlap
            pieChartProviders.requestLayout();
        } else {
            Log.e("StatsActivity", "Mismatch between legend entries and pie chart data entries.");
        }
    }

    private void setCustomLegendEntries(PieChart pieChart, List<String> customLegendEntries) {
        Legend legend = pieChart.getLegend();
        legend.setCustom(createLegendEntries(customLegendEntries));
        legend.setWordWrapEnabled(true);
        legend.setXEntrySpace(10f);
        legend.setYEntrySpace(15f); // Increased vertical space to avoid overlapping
    }

    private List<LegendEntry> createLegendEntries(List<String> customLegendEntries) {
        List<LegendEntry> legendEntries = new ArrayList<>();

        int[] colors = getColorList(customLegendEntries.size());

        for (int i = 0; i < customLegendEntries.size(); i++) {
            LegendEntry entry = new LegendEntry();
            entry.label = customLegendEntries.get(i);
            entry.formColor = colors[i % colors.length];

            legendEntries.add(entry);
        }

        return legendEntries;
    }

    private int[] getColorList(int size) {
        int[] baseColors = ColorTemplate.COLORFUL_COLORS;
        int[] colors = new int[size];

        for (int i = 0; i < size; i++) {
            colors[i] = baseColors[i % baseColors.length];
        }

        return colors;
    }

    private Set<String> getUniqueSecurityProtocols(List<Network> networks) {
        Set<String> securityProtocols = new HashSet<>();
        for (Network network : networks) {
            securityProtocols.add(network.getSecurity());
        }
        return securityProtocols;
    }

    private Set<String> getUniqueProviders(List<Network> networks) {
        Set<String> providers = new HashSet<>();
        for (Network network : networks) {
            providers.add(getNetworkProvider(network.getSsid()));
        }
        return providers;
    }
}

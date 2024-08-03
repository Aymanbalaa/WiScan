package com.example.nsgs_app;

import static com.example.nsgs_app.NetworkProviderGuesser.getNetworkProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class StatsActivity extends AppCompatActivity {

    private List<Network> networkList;
    private PieChart pieChartNetworksDistribution;
    private TextView textViewToRightOfPieTitle;
    private TextView textViewBelowPieTitle;
    private String neighborhood;
//    private Button buttonSaveChart;
    private Button buttonPieChart1;
    private Button buttonPieChart2;
    private static final int PERMISSION_REQUEST_CODE = 1;

    public Set<String> protocols;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_stats), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pieChartNetworksDistribution = findViewById(R.id.pie_chart_networks_distribution);
        textViewToRightOfPieTitle = findViewById(R.id.text_right_of_title);
        textViewBelowPieTitle = findViewById(R.id.text_below_title);
//        buttonSaveChart = findViewById(R.id.save_chart_button);
        buttonPieChart1 = findViewById(R.id.button_pie_chart_1);
        buttonPieChart2 = findViewById(R.id.button_pie_chart_2);

        // Fetching network list from NetworkManager
        NetworkManager networkManager = NetworkManager.getInstance(this);
        networkManager.fetchNetworks("http://217.15.171.225:5000/get_all_networks", false);
        networkList = networkManager.getNetworkList();

        protocols = getUniqueSecurityProtocols(networkList);

        for (Network network : networkList) {
            if (!Objects.equals(network.getNeighborhood(), "Area name not found")) {
                neighborhood = network.getNeighborhood();
                break;
            }
        }

        textViewToRightOfPieTitle.setText(neighborhood);
        textViewBelowPieTitle.setText("Total Networks: " + networkList.size());

        if (networkList != null && protocols != null) {
            setUpPieChart();
            loadPieChartData1();
        } else {
            Log.e("StatsActivity", "Network list or Protocols set is null");
        }

        buttonPieChart1.setOnClickListener(v -> {
            pieChartNetworksDistribution.clear();
            loadPieChartData1();
        });

        buttonPieChart2.setOnClickListener(v -> {
            pieChartNetworksDistribution.clear();
            loadPieChartData2();
        });
//
//        buttonSaveChart.setOnClickListener(v -> {
//            if (ContextCompat.checkSelfPermission(StatsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(StatsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
//            } else {
//                saveChart(pieChartNetworksDistribution, "networks_chart", "Download");
//            }
//        });
    }

    private void setUpPieChart() {
        pieChartNetworksDistribution.setUsePercentValues(true);
        pieChartNetworksDistribution.setCenterText("Security Protocols");
        pieChartNetworksDistribution.getDescription().setEnabled(false);
        pieChartNetworksDistribution.setEntryLabelTextSize(12f);
        pieChartNetworksDistribution.setCenterTextSize(22f);
        pieChartNetworksDistribution.setDrawEntryLabels(false);
        pieChartNetworksDistribution.setHoleRadius(45f);
        pieChartNetworksDistribution.setTransparentCircleRadius(40f);
        pieChartNetworksDistribution.setExtraOffsets(25, 10, 10, 5);

        Legend legend = pieChartNetworksDistribution.getLegend();
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

    private void loadPieChartData1() {
        pieChartNetworksDistribution.setCenterText("Security Protocols");
        ArrayList<PieEntry> pieEntriesNetworkDistribution = new ArrayList<>();
        List<String> customLegendEntries = new ArrayList<>();

        for (String protocol : protocols) {
            List<Network> filteredNetworkList = networkList.stream()
                    .filter(network -> network.getSecurity().equalsIgnoreCase(protocol))
                    .collect(Collectors.toList());

            pieEntriesNetworkDistribution.add(new PieEntry(((float) filteredNetworkList.size() / networkList.size()) * 100, protocol));
            customLegendEntries.add(protocol + " (" + String.format("%.1f", ((float) filteredNetworkList.size() / networkList.size()) * 100) + "%)");
        }

        PieDataSet pieDataSetChartNetworkDistribution = new PieDataSet(pieEntriesNetworkDistribution, "");
        pieDataSetChartNetworkDistribution.setColors(getColorList(customLegendEntries.size()));

        if (customLegendEntries.size() == pieEntriesNetworkDistribution.size()) {
            pieChartNetworksDistribution.setData(new PieData(pieDataSetChartNetworkDistribution));
            pieDataSetChartNetworkDistribution.setValueTextSize(0f); // Hides the values

            pieChartNetworksDistribution.animateXY(2000, 2000); // Faster animation
            setCustomLegendEntries(customLegendEntries);
            pieChartNetworksDistribution.invalidate();

            // Request layout pass to fix legend overlap
            pieChartNetworksDistribution.requestLayout();
        } else {
            Log.e("StatsActivity", "Mismatch between legend entries and pie chart data entries.");
        }
    }

    private void loadPieChartData2() {
        pieChartNetworksDistribution.setCenterText("Network Providers");
        ArrayList<PieEntry> pieEntriesNetworkDistribution = new ArrayList<>();
        List<String> customLegendEntries = new ArrayList<>();

        Set<String> providers = getUniqueProviders(networkList);
        for (String provider : providers) {
            List<Network> filteredNetworkList = networkList.stream()
                    .filter(network -> getNetworkProvider(network.getSsid()).equalsIgnoreCase(provider))
                    .collect(Collectors.toList());

            pieEntriesNetworkDistribution.add(new PieEntry(((float) filteredNetworkList.size() / networkList.size()) * 100, provider));
            customLegendEntries.add(provider + " (" + String.format("%.1f", ((float) filteredNetworkList.size() / networkList.size()) * 100) + "%)");
        }

        PieDataSet pieDataSetChartNetworkDistribution = new PieDataSet(pieEntriesNetworkDistribution, "");
        pieDataSetChartNetworkDistribution.setColors(getColorList(customLegendEntries.size()));

        if (customLegendEntries.size() == pieEntriesNetworkDistribution.size()) {
            pieChartNetworksDistribution.setData(new PieData(pieDataSetChartNetworkDistribution));
            pieDataSetChartNetworkDistribution.setValueTextSize(0f); // Hides the values

            pieChartNetworksDistribution.animateXY(2000, 2000); // Faster animation
            setCustomLegendEntries(customLegendEntries);
            pieChartNetworksDistribution.invalidate();

            // Request layout pass to fix legend overlap
            pieChartNetworksDistribution.requestLayout();
        } else {
            Log.e("StatsActivity", "Mismatch between legend entries and pie chart data entries.");
        }
    }


    private void setCustomLegendEntries(List<String> customLegendEntries) {
        Legend legend = pieChartNetworksDistribution.getLegend();
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

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                saveChart(pieChartNetworksDistribution, "networks_chart", "Download");
//            } else {
//                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    private void saveChart(PieChart pieChart, String fileName, String folderName) {
//        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + folderName;
//        File dir = new File(filePath);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//        File file = new File(dir, fileName + ".png");
//        if (pieChart.saveToPath(file.getName(), file.getParent())) {
//            Toast.makeText(this, "Chart saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(this, "Failed to save chart", Toast.LENGTH_LONG).show();
//        }
//    }
}

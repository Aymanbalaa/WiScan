package com.example.nsgs_app;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class StatisticsActivity extends AppCompatActivity {

    private List<Network> networkList;
   // private PieChart pieChartNeigborhoodStats;
    private PieChart pieChartNetworksDistrubution; //Piechart object for Network distribution;
    private TextView textviewToRightofPieTtile;
    private TextView textviewBelowfPieTtile;
    private String Neighberhood;
    private Button buttonSavechart;
    private static final int PERMISSION_REQUEST_CODE = 1;

    public Set<String> Protocols;//Piechart object for Neighborhood statistics;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.statisticsView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Assigning variables for piecharts
        pieChartNetworksDistrubution = findViewById(R.id.pie_chart_networks_distribution);
        //pieChartNeigborhoodStats = findViewById(R.id.pie_chart_neighborhood_stats);

        textviewToRightofPieTtile = findViewById(R.id.text_right_of_title);
        textviewBelowfPieTtile = findViewById(R.id.text_below_title);
        buttonSavechart =findViewById(R.id.save_chart_button);


        //Retrieving the networkList and protocols list from the intent
        Intent intent= getIntent();

        if(intent != null && intent.hasExtra("networkList") ){
            String networkListJson = intent.getStringExtra("networkList");
            Gson gson = new Gson();
            Type networkListType = new TypeToken<List<Network>>() {}.getType();

            networkList = gson.fromJson(networkListJson, networkListType);
        }

        Protocols = getUniqueSecurityProtocols(networkList);

        //retrieving neighborhood

        for(Network network:networkList){
            if(!Objects.equals(network.getNeighborhood(), "Area name not found")){
                Neighberhood = network.getNeighborhood();
                break;
            }
        }

        textviewToRightofPieTtile.setText(Neighberhood);
        textviewBelowfPieTtile.setText("Total Networks:" +networkList.size());


/*
        if (intent.hasExtra("Protocols")) {
            Type protocolsSetType = new TypeToken<Set<String>>() {}.getType();
            String protocolsListJson = intent.getStringExtra("Protocols");
            Gson gson = new Gson();
            Protocols = gson.fromJson(protocolsListJson, protocolsSetType);
        }*/

        if (networkList != null && Protocols != null) {
            setUpPieChart();
            loadPieChartData();
        } else {
            Log.e("StatisticsActivity", "Network list or Protocols set is null");
        }



        buttonSavechart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for write permission before attempting to save the chart
                if (ContextCompat.checkSelfPermission(StatisticsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(StatisticsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                } else {
                    saveChart(pieChartNetworksDistrubution, "networks_chart", "Download");
                }
            }
        });
    }
    private void setUpPieChart(){

        //Network Distribution charts
        pieChartNetworksDistrubution.setUsePercentValues(true);
        pieChartNetworksDistrubution.setCenterText("Security Protocols");
        pieChartNetworksDistrubution.getDescription().setEnabled(false);
        pieChartNetworksDistrubution.setEntryLabelTextSize(12f);
        pieChartNetworksDistrubution.setCenterTextSize(22f);
        pieChartNetworksDistrubution.setDrawEntryLabels(false); // Disable drawing labels on the slices

        // Set hole radius
        pieChartNetworksDistrubution.setHoleRadius(45f); // Set hole radius as a percentage of the pie radius

        // Set transparent circle radius
        pieChartNetworksDistrubution.setTransparentCircleRadius(40f); // Set transparent circle radius

        // Extra offsets to adjust the size and position
        pieChartNetworksDistrubution.setExtraOffsets(25, 10, 10, 5);

        // Customize the built-in legend
        Legend legend = pieChartNetworksDistrubution.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(14f); // Set the desired text size for legend entries
        legend.setFormSize(14f); // Set the size of the form (color box)
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true); // Allow word wrap for long labels
        legend.setXEntrySpace(7f); // Space between the legend entries
        legend.setYEntrySpace(5f); // Space between the lines

        //Neighborhood stats charts




    }

    private void loadPieChartData(){
        //initializing array list to hold pie chart data for each pie chart
        ArrayList<PieEntry>pieEntriesNetworkDistribution = new ArrayList<>();
        ArrayList<PieEntry>pieEntriesNeigborhoodStats = new ArrayList<>();
        List<String> customLegendEntries = new ArrayList<>();

        //initialize a pie chart entry for each chart to convert the data to pie chart entry and adding data to array
        for(String protocol:Protocols){
            //computing number of number for the each protocol
            List<Network> filteredNetworkList = networkList.stream()
                    .filter(network -> network.getSecurity().equalsIgnoreCase(protocol))
                    .collect(Collectors.toList());

            pieEntriesNetworkDistribution.add(new PieEntry(((float) filteredNetworkList.size() /networkList.size())*100, protocol));
            customLegendEntries.add(protocol + " (" + String.format("%.1f", ((float) filteredNetworkList.size() /networkList.size())*100) + "%)");

        }

        //Adding colors to chart sections
        PieDataSet pieDataSetChartNetworkDistribution =  new PieDataSet(pieEntriesNetworkDistribution, "");

        pieDataSetChartNetworkDistribution.setColors(ColorTemplate.COLORFUL_COLORS);


        //Display the chart
        pieChartNetworksDistrubution.setData(new PieData(pieDataSetChartNetworkDistribution));

        pieDataSetChartNetworkDistribution.setValueTextSize(14f);

        pieDataSetChartNetworkDistribution.setValueLinePart1OffsetPercentage(100.f); // Extend line starting point
        pieDataSetChartNetworkDistribution.setValueLinePart1Length(0.5f); // Length of the first part of the line
        pieDataSetChartNetworkDistribution.setValueLinePart2Length(0.4f); // Length of the second part of the line
        pieDataSetChartNetworkDistribution.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);//Place labels outside chart
        pieDataSetChartNetworkDistribution.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE); // Place percentage  outside


        //Setting animation for the charts
        pieChartNetworksDistrubution.animateXY( 5000,5000);

        pieChartNetworksDistrubution.invalidate(); // Refresh the chart

        setCustomLegendEntries(customLegendEntries);

    }

    private void setCustomLegendEntries(List<String> customLegendEntries) {
        // Get the chart's legend
        Legend legend = pieChartNetworksDistrubution.getLegend();
        legend.setCustom(createLegendEntries(customLegendEntries)); // Set the custom legend entries
    }

    private List<LegendEntry> createLegendEntries(List<String> customLegendEntries) {
        List<LegendEntry> legendEntries = new ArrayList<>();

        // Iterate through customLegendEntries to create LegendEntry objects
        for (int i = 0; i < customLegendEntries.size(); i++) {
            LegendEntry entry = new LegendEntry();
            entry.label = customLegendEntries.get(i);
            entry.formColor = ColorTemplate.COLORFUL_COLORS[i]; // Set corresponding color

            legendEntries.add(entry);
        }

        return legendEntries;
    }





    private Set<String> getUniqueSecurityProtocols(List<Network> networks) {
        //Function to retrieve the unique protocols from the fetched networks list
        Set<String> securityProtocols = new HashSet<>();
        for (Network network : networks) {
            securityProtocols.add(network.getSecurity());
        }
        return securityProtocols;
    }

    //Saving chart

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveChart(pieChartNetworksDistrubution, "networks_chart", "Download");
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveChart(PieChart pieChart, String fileName, String folderName) {
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + folderName;
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName + ".png");
        if (pieChart.saveToPath(file.getName(), file.getParent())) {
            Toast.makeText(this, "Chart saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to save chart", Toast.LENGTH_LONG).show();
        }
    }


}
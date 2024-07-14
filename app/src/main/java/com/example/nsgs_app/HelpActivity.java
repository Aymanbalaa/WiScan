package com.example.nsgs_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ExpandableListView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HelpActivity extends AppCompatActivity {

    ExpandableListView faqListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        faqListView = findViewById(R.id.faqListView);
        prepareListData();

        FaqExpandableListAdapter listAdapter = new FaqExpandableListAdapter(this, listDataHeader, listDataChild);
        faqListView.setAdapter(listAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        // Adding FAQ questions
        listDataHeader.add("What is NSGS App?");
        listDataHeader.add("How do I start a WiFi scan?");
        listDataHeader.add("How do I configure the WiFi scan settings?");
        listDataHeader.add("Where can I see the scanned WiFi data?");

        // Adding FAQ answers
        List<String> answer1 = new ArrayList<>();
        answer1.add("The NSGS (Network Security Geo-Scanner) App is designed to capture local WiFi data, clean it, display progress to the user, and send the data for advanced visualizations such as graphs and maps. The app allows users to configure WiFi scan frequency and radius.");
        List<String> answer2 = new ArrayList<>();
        answer2.add("To start a WiFi scan, navigate to the WiFiActivity section, and press the 'Start Scan' button. The app will begin scanning for local WiFi networks based on the configured settings.");
        List<String> answer3 = new ArrayList<>();
        answer3.add("You can configure the WiFi scan settings by going to the SettingsActivity section. Here you can adjust the scan frequency and radius according to your needs.");
        List<String> answer4 = new ArrayList<>();
        answer4.add("The scanned WiFi data is displayed in the LocationActivity section. This section provides a detailed view of the networks captured during the scan, including their BSSIDs, SSIDs, and security protocols.");

        listDataChild.put(listDataHeader.get(0), answer1);
        listDataChild.put(listDataHeader.get(1), answer2);
        listDataChild.put(listDataHeader.get(2), answer3);
        listDataChild.put(listDataHeader.get(3), answer4);
    }
}

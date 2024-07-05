package com.example.nsgs_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class NetworkDetails extends AppCompatActivity {

    protected TextView textViewSSID;
    protected TextView bssid;
    protected TextView postalcode;
    protected TextView textViewProtocol;
    protected TextView textViewNeighborhood;
    protected TextView textViewCoordinates;
    protected TextView textViewProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_network_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the toolbar and set it as the support action bar
        Toolbar toolbar = findViewById(R.id.toolbarDetails);
        setSupportActionBar(toolbar);

        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        textViewSSID = findViewById(R.id.textViewNetworkSSIDDetailsActivity);
       bssid = findViewById(R.id.textViewBssidDetails);
        postalcode= findViewById(R.id.textViewPostalCodeDetails);
      textViewProtocol= findViewById(R.id.textViewProtocolDetails);
      textViewNeighborhood = findViewById(R.id.textViewNeighborDetails);
        textViewCoordinates = findViewById(R.id.textViewCoordinatesDetails);
        textViewProvider = findViewById(R.id.textViewProviderDetails);

        Intent intent = getIntent();
        int Network_id = intent.getIntExtra("Network id", 0);

        //For testing
        Network network = new Network(Network_id, "BELL_45", "bssid1", "H3SE9", "WEP1", "Concordia", "548621", "BELL");
                //(Network_id, "BELL", "bssid1", "H3SE9", "WEP1", "Concordia", "548621");

        if (Network_id!=0){

            //Displaying network details
            runOnUiThread(() -> {

                //Showing details of network
                // Displaying network details
                textViewSSID.setText(network.getSsid());
                bssid.setText(network.getBssid());
                postalcode.setText(network.getPostalCode());
                textViewProtocol.setText(network.getSecurity());
                textViewNeighborhood.setText(network.getNeighborhood());
                textViewCoordinates.setText(network.getCoordinates());
                textViewProvider.setText(network.getProvider());
            });

        }
        else {
            Toast.makeText(this, "Failure to catch intent", Toast.LENGTH_LONG).show();
        }

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle the home button press to navigate up
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
package com.example.nsgs_app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NetworkAdapter extends RecyclerView.Adapter<NetworkAdapter.NetworkViewHolder> {
    private Context context;
    private List<Network> networkList;
    private String currentFilter; // variable for the current filter

    public NetworkAdapter(Context context, List<Network> networkList, String currentFilter) {
        this.context = context;
        this.networkList = networkList;
        this.currentFilter = this.currentFilter; // Initializing the current filter
    }

    @NonNull
    @Override
    public NetworkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.network_item, parent, false);
        return new NetworkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NetworkViewHolder holder, int position) {
        Network network = networkList.get(position);

        holder.ssid.setText(context.getString(R.string.ssid_label, network.getSsid()));
        holder.security.setText(context.getString(R.string.security_label, network.getSecurity()));

        // Set OnClickListener to open NetworkDetailActivity with network details and current filter
        holder.itemView.setOnClickListener(v -> NetworkDetailActivity.start(context, network, currentFilter));

       /* holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NetworkDetailActivity.class);
            intent.putExtra("ssid", network.getSsid());
            intent.putExtra("bssid", network.getBssid());
            intent.putExtra("security", network.getSecurity());
            intent.putExtra("coordinates", network.getCoordinates());
            intent.putExtra("postalCode", network.getPostalCode());
            intent.putExtra("neighborhood", network.getNeighborhood());
            intent.putExtra("currentFilter", currentFilter); // Pass the current filter state
            context.startActivity(intent);
        });*/
    }

    @Override
    public int getItemCount() {
        return networkList.size();
    }

    // updates the network list and notify changes
    public void updateNetworkList(List<Network> newNetworkList) {
        this.networkList = newNetworkList;
        notifyDataSetChanged();
    }

    public static class NetworkViewHolder extends RecyclerView.ViewHolder {
        TextView ssid, security;

        public NetworkViewHolder(@NonNull View itemView) {
            super(itemView);
            ssid = itemView.findViewById(R.id.ssid);
            security = itemView.findViewById(R.id.security);
        }
    }
}

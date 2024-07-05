// NetworkAdapter.java
package com.example.nsgs_app;

import android.content.Context;
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

    public NetworkAdapter(Context context, List<Network> networkList) {
        this.context = context;
        this.networkList = networkList;
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

        // Set OnClickListener to open NetworkDetailActivity with network details
        holder.itemView.setOnClickListener(v -> NetworkDetailActivity.start(context, network));
    }

    @Override
    public int getItemCount() {
        return networkList.size();
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

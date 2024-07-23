package com.example.nsgs_app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActiveNetworkAdapter extends RecyclerView.Adapter<ActiveNetworkAdapter.ViewHolder> {

    private Context context;
    private List<ActiveNetwork> activeNetworkList;
    private List<Network> networkList;

    public ActiveNetworkAdapter(Context context, List<ActiveNetwork> activeNetworkList, List<Network> networkList) {
        this.context = context;
        this.activeNetworkList = activeNetworkList;
        this.networkList = networkList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_active_network, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActiveNetwork activeNetwork = activeNetworkList.get(position);
        holder.ssidTextView.setText("SSID: " + activeNetwork.getSsid());
        int signalStrength = activeNetwork.getSignalStrength();
        holder.signalStrengthPercentageTextView.setText(signalStrength + "%");

        // Set the appropriate signal strength drawable
        if (signalStrength <= 20) {
            holder.signalStrengthImageView.setImageResource(R.drawable.signal_0);
        } else if (signalStrength <= 40) {
            holder.signalStrengthImageView.setImageResource(R.drawable.signal_1);
        } else if (signalStrength <= 60) {
            holder.signalStrengthImageView.setImageResource(R.drawable.signal_2);
        } else if (signalStrength <= 80) {
            holder.signalStrengthImageView.setImageResource(R.drawable.signal_3);
        } else {
            holder.signalStrengthImageView.setImageResource(R.drawable.signal_4);
        }

        holder.itemView.setOnClickListener(v -> {
            Network selectedNetwork = null;
            for (Network network : networkList) {
                if (network.getBssid().equals(activeNetwork.getBssid())) {
                    selectedNetwork = network;
                    break;
                }
            }

            if (selectedNetwork != null) {
                Intent intent = new Intent(context, ActiveNetworkDetailsActivity.class);
                intent.putExtra("ssid", selectedNetwork.getSsid());
                intent.putExtra("bssid", selectedNetwork.getBssid());
                intent.putExtra("security", selectedNetwork.getSecurity());
                intent.putExtra("coordinates", selectedNetwork.getCoordinates());
                intent.putExtra("postalCode", selectedNetwork.getPostalCode());
                intent.putExtra("neighborhood", selectedNetwork.getNeighborhood());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activeNetworkList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ssidTextView;
        ImageView signalStrengthImageView;
        TextView signalStrengthPercentageTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ssidTextView = itemView.findViewById(R.id.ssidTextView);
            signalStrengthImageView = itemView.findViewById(R.id.signalStrengthImageView);
            signalStrengthPercentageTextView = itemView.findViewById(R.id.signalStrengthPercentageTextView);
        }
    }
}

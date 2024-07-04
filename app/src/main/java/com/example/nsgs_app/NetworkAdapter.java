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

        //following best strings.xml practice
        //uses strings.xml to allow easy translation if needed
        holder.ssid.setText(context.getString(R.string.ssid_label, network.getSsid()));
        holder.bssid.setText(context.getString(R.string.bssid_label, network.getBssid()));
        holder.security.setText(context.getString(R.string.security_label, network.getSecurity()));
        holder.coordinates.setText(context.getString(R.string.coordinates_label, network.getCoordinates()));
        holder.postalCode.setText(context.getString(R.string.postal_code_label, network.getPostalCode()));
        holder.neighborhood.setText(context.getString(R.string.neighborhood_label, network.getNeighborhood()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            //The view of the holder is itemView
            //gives instruction upon network item click-sends to activity page where detailed info about the network selected is displayed namely estimated location and network provider
            @Override
            public void onClick(View v) {
                int pos;
                pos = holder.getLayoutPosition();

                Intent intent = new Intent(v.getContext(), NetworkDetails.class);
                intent.putExtra("Network id", networkList.get(pos).getId());
                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return networkList.size();
    }

    public static class NetworkViewHolder extends RecyclerView.ViewHolder {
        TextView ssid, bssid, security, coordinates, postalCode, neighborhood;

        public NetworkViewHolder(@NonNull View itemView) {
            super(itemView);
            ssid = itemView.findViewById(R.id.ssid);
            bssid = itemView.findViewById(R.id.bssid);
            security = itemView.findViewById(R.id.security);
            coordinates = itemView.findViewById(R.id.coordinates);
            postalCode = itemView.findViewById(R.id.postalCode);
            neighborhood = itemView.findViewById(R.id.neighborhood);
        }
    }
}

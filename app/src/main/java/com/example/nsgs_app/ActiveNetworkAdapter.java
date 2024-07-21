package com.example.nsgs_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActiveNetworkAdapter extends RecyclerView.Adapter<ActiveNetworkAdapter.ViewHolder> {

    private Context context;
    private List<ActiveNetwork> activeNetworkList;

    public ActiveNetworkAdapter(Context context, List<ActiveNetwork> activeNetworkList) {
        this.context = context;
        this.activeNetworkList = activeNetworkList;
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
        holder.signalStrengthRatingBar.setRating(convertSignalStrengthToRating(activeNetwork.getSignalStrength()));
    }

    @Override
    public int getItemCount() {
        return activeNetworkList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ssidTextView;
        RatingBar signalStrengthRatingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ssidTextView = itemView.findViewById(R.id.ssidTextView);
            signalStrengthRatingBar = itemView.findViewById(R.id.signalStrengthRatingBar);
        }
    }

    private float convertSignalStrengthToRating(int signalStrength) {
        // Assuming signalStrength ranges from 0 to 100
        return (signalStrength / 20.0f); // Convert to a 5-star rating
    }
}

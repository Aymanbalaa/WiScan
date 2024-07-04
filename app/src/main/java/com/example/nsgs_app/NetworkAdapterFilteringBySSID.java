package com.example.nsgs_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NetworkAdapterFilteringBySSID extends RecyclerView.Adapter<NetworkAdapterFilteringBySSID .ViewHolder> {

    private List<Network> localDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        //class that acts as activity page for the network recycler view item
        //Autogenerates constructor

        private TextView networtSSID;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            networtSSID= itemView.findViewById(R.id.textViewItemFiltering);

        }

        //getters of the attributes

        public TextView getNetworkSSID_securityProtocol() {
            return networtSSID;
        }
    }

    //Constructor


    public NetworkAdapterFilteringBySSID(List<Network> localDataset) {
        this.localDataset = localDataset;
    }

    //This function inflates the view holder/adapter with the network recycler view item layout
    @NonNull
    @Override
    public NetworkAdapterFilteringBySSID.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.network_item_filtering, parent, false);

        return new NetworkAdapterFilteringBySSID.ViewHolder(view); //returns adapted item layout to a view holder
    }

    //States the content of the view holder created above

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getNetworkSSID_securityProtocol().setText(localDataset.get(position).getSsid()); //Attachs Security Protocol to its corresponding layout/View Holder component
    }


    //Keeps track of networks number in list
    @Override
    public int getItemCount() {
        return localDataset.size();
    }




}

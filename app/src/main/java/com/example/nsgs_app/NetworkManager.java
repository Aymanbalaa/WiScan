package com.example.nsgs_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkManager {

    private static NetworkManager instance;
    private List<Network> networkList;
    private List<Network> triangulatedList;
    private static final String PREFS_NAME = "WiFiActivityPrefs";
    private static final String NETWORK_LIST_KEY = "network_list";
    private static final String TRIANGULATED_LIST_KEY = "triangulated_list";
    private Context context;

    private NetworkManager(Context context) {
        this.context = context;
        networkList = new ArrayList<>();
        triangulatedList = new ArrayList<>();
        loadNetworkListFromPreferences();
    }

    public static synchronized NetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkManager(context);
        }
        return instance;
    }

    public List<Network> getNetworkList() {
        return networkList;
    }

    public List<Network> getTriangulatedList() {
        return triangulatedList;
    }

    public void fetchNetworks(String url, boolean isTriangulated) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("NetworkManager", "Error fetching data: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        Gson gson = new Gson();
                        Type networkListType = new TypeToken<List<Network>>() {}.getType();
                        if (isTriangulated) {
                            List<Network> fetchedTriangulatedList = gson.fromJson(jsonObject.getJSONArray("networks_triangulated").toString(), networkListType);
                            triangulatedList.clear();
                            triangulatedList.addAll(fetchedTriangulatedList);
                            saveTriangulatedListToPreferences();
                        } else {
                            List<Network> fetchedNetworkList = gson.fromJson(jsonObject.getJSONArray("networks").toString(), networkListType);
                            networkList.clear();
                            networkList.addAll(fetchedNetworkList);
                            saveNetworkListToPreferences();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "Error parsing data", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void saveNetworkListToPreferences() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String networkListJson = gson.toJson(networkList);
        editor.putString(NETWORK_LIST_KEY, networkListJson);
        editor.apply();
    }

    private void saveTriangulatedListToPreferences() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String triangulatedListJson = gson.toJson(triangulatedList);
        editor.putString(TRIANGULATED_LIST_KEY, triangulatedListJson);
        editor.apply();
    }

    private void loadNetworkListFromPreferences() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String networkListJson = preferences.getString(NETWORK_LIST_KEY, null);
        String triangulatedListJson = preferences.getString(TRIANGULATED_LIST_KEY, null);
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Network>>() {}.getType();

        if (networkListJson != null) {
            networkList = gson.fromJson(networkListJson, listType);
        }

        if (triangulatedListJson != null) {
            triangulatedList = gson.fromJson(triangulatedListJson, listType);
        }
    }

    public void refreshLists() {
        loadNetworkListFromPreferences();
        loadTriangulatedListFromPreferences();
    }

    private void loadTriangulatedListFromPreferences() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String triangulatedListJson = preferences.getString(TRIANGULATED_LIST_KEY, null);
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Network>>() {}.getType();

        if (triangulatedListJson != null) {
            triangulatedList = gson.fromJson(triangulatedListJson, listType);
        }
    }
}

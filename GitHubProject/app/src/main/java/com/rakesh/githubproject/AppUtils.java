package com.rakesh.githubproject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by rakesh on 7/4/2016.
 */
public class AppUtils {


    public static HashMap<String, String> connectToServer(String requestJson, String path) {
        StringBuilder result = new StringBuilder("");
        HashMap<String, String> resultMap = new HashMap<>();
        int responseCode = 0;
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            responseCode = connection.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;


            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        resultMap.put("responseCode", responseCode + "");
        resultMap.put("result", result.toString());
        return resultMap;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}

package com.example.interactivetracker;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetJSONResponse {
    String callSign = "GetJSONResponse";
    public JSONObject parseJSON(String url, String remoteCS) {
        try {
            callSign = callSign + " [Called from Activity '" + remoteCS + "']";
            Log.i(callSign, "Sending a request to '" + url + "'. Expecting a JSON Response.");

            URL requestObj = new URL(url);
            HttpURLConnection reqConn = (HttpURLConnection) requestObj.openConnection();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(reqConn.getInputStream())
            );

            String inputLn;
            StringBuilder responseSB = new StringBuilder();
            while ((inputLn = in.readLine()) != null) {
                responseSB.append(inputLn);
            }
            in.close();

            return new JSONObject(responseSB.toString());

        } catch (Exception e) {
            Log.i(callSign, "AN ERROR HAS OCCURRED!");
            e.printStackTrace();
            return null; }
    }
}

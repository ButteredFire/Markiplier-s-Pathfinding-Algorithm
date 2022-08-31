package com.example.interactivetracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetImageBitmap {
    String callSign = "GetImageBitmap";
    public Bitmap processURL(String url, String remoteCS) {
        try {
            callSign = callSign + " [Called from Activity '" + remoteCS + "']";
            Log.i(callSign, "Attempting to convert URL '" + url + "' into a Bitmap object.");

            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    public String bitmapToString(Bitmap targetBitmap) {
        ByteArrayOutputStream outArray = new ByteArrayOutputStream();
        targetBitmap.compress(Bitmap.CompressFormat.PNG,100, outArray);
        byte[] b = outArray.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public Bitmap stringToBitmap(String targetString) {
        try {
            byte[] encodeByte = Base64.decode(targetString,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

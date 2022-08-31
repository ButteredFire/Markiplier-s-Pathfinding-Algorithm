package com.example.interactivetracker;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIObject {
    public static final int VALID = 0;

    String requestAPI_URL; String part; String forUsername; String devKey; String callSign = "APIObject";

    public final ExecutorService executor = Executors.newSingleThreadExecutor();
    public final Handler handler = new Handler(Looper.getMainLooper());

    public APIObject(String requestAPI_URL, String part, String forUsername, String devKey) {
        this.requestAPI_URL = requestAPI_URL;
        this.part = part; this.forUsername = forUsername; this.devKey = devKey;
    }


    private static List<String> extractURLs(String raw_string)
    {
        List<String> containedUrls = new ArrayList<>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(raw_string);

        while (urlMatcher.find())
        {
            containedUrls.add(raw_string.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }

    private String constructFinalURL(String APIClass) {
        return requestAPI_URL + APIClass + "?part=" + part + "&forUsername=" + forUsername + "&key=" + devKey;
    }

    private Bitmap getThumbnailBitmap(String APIClass, String imgSize) {
        try {
            final String finalURL = constructFinalURL(APIClass);

            Log.i(callSign, "Attempting to extract Bitmap from URL '" + finalURL + "'.");
            JSONObject response = new GetJSONResponse().parseJSON(finalURL, callSign);

            JSONObject Snippet = new JSONObject(response.getJSONArray("items").getString(0)).getJSONObject("snippet");
            String rawThumbnailURL = new JSONObject(Snippet.getJSONObject("thumbnails").getJSONObject(imgSize).toString()).toString().replace("\\", "");

            List<String> extractedUrls = extractURLs(rawThumbnailURL);

            GetImageBitmap GIB_inst = new GetImageBitmap();
            return GIB_inst.processURL(extractedUrls.get(0), callSign);

        } catch (JSONException e) {
            return null;
        }
    }

    private String getCreatorDisplayName(String APIClass) {
        final String finalURL = constructFinalURL(APIClass);

        JSONObject response = new GetJSONResponse().parseJSON(finalURL, callSign);

        try {
            return new JSONObject(response.getJSONArray("items").getString(0)).getJSONObject("snippet").getString("title");

        } catch(JSONException e) {
            return null;
        }
    }

    private JSONObject simplifyContentInfo(String APIClass, String imgSize, String query, String pubDateType, String pubDate) {
        try {
            JSONObject cList = new JSONObject();
            String finalURL = constructFinalURL(APIClass);
            finalURL += "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString());

            if(!pubDateType.isEmpty() && !pubDate.isEmpty()) {
                finalURL +=  "&" + pubDateType + "=" + URLEncoder.encode(pubDate, StandardCharsets.UTF_8.toString());
            }

            JSONObject response = new GetJSONResponse().parseJSON(finalURL, callSign);
            int iterIndex = 0;

            JSONArray itemsArray = new JSONArray(response.getJSONArray("items").toString());

            for(int i=0; i <= itemsArray.length(); i++) {
                try {
                    JSONObject cInfo = new JSONObject();
                    JSONArray cItem = new JSONArray();

                    JSONObject contentItem = new JSONObject(response.getJSONArray("items").getString(i));

                    JSONObject Snippet = contentItem.getJSONObject("snippet");
                    String rawThumbnailURL = Snippet.getJSONObject("thumbnails").getJSONObject(imgSize).toString().replace("\\", "");
                    List<String> extractedUrls = extractURLs(rawThumbnailURL);

                    String creator = Snippet.getString("channelId");
                    String videoID = contentItem.getJSONObject("id").getString("videoId");
                    String title = Snippet.getString("title");
                    String description = Snippet.getString("description");
                    String thumbnailRawURL = extractedUrls.get(0);


                    //if(!title.toLowerCase().contains("blooper") || !title.toLowerCase().contains("bts")) {
                    cInfo.put("creator", creator); cInfo.put("videoId", videoID); cInfo.put("title", title); cInfo.put("description", description);
                    cInfo.put("thumbnailURL", thumbnailRawURL);

                    cItem.put(cInfo);

                    cList.put(String.valueOf(iterIndex), cItem);

                    iterIndex += 1;

                } catch(Exception ignored) {  }
            }

        Log.i(callSign, cList.toString());
        return cList;

        } catch(UnsupportedEncodingException | JSONException e) { e.printStackTrace(); return new JSONObject();  }
    }

    private JSONObject GetContentStats(String APIClass, String videoID) {
        String finalURL = requestAPI_URL + APIClass + "?part=" + part + "&id=" + videoID + "&key=" + devKey;

        JSONObject cStats = new JSONObject();
        JSONArray urls = new JSONArray();
        JSONObject response = new GetJSONResponse().parseJSON(finalURL, callSign);

        try {
            JSONObject Snippet = new JSONObject(response.getJSONArray("items").getString(0)).getJSONObject("snippet");

            String id = new JSONObject(response.getJSONArray("items").getString(0)).getString("id");
            String title = Snippet.getString("title");
            String description = Snippet.getString("description");

            List<String> extractedUrls = extractURLs(description);
            boolean extractedAll = false; int urlIndex = 0;
            while(!extractedAll) {
                try {
                    urls.put(extractedUrls.get(urlIndex));
                    urlIndex += 1;

                } catch(Exception e) { extractedAll = true; }
            }

            cStats.put("id", id); cStats.put("title", title); cStats.put("description", description);
            cStats.put("urls", urls);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return cStats;
    }


    private JSONObject doAPIKeyTest() {
        final String finalURL = constructFinalURL("channels");

        JSONObject response = new GetJSONResponse().parseJSON(finalURL, callSign);
        JSONObject outputHolder = new JSONObject();

        try {
            JSONObject errorArray = new JSONObject(response.getJSONObject("error").toString());
            outputHolder.put("error", errorArray);

        } catch(JSONException e) {
            try { return new JSONObject().put("error", new JSONObject().put("status", VALID)); } catch(JSONException ignored) {  }
        }
        return outputHolder;
    }


    // CALLABLE FUNCTIONS

    /**
     * Call functions by codename on a non-UI thread
     * @param <T>
     */
    class constructCallable<T> implements Callable<T> {
        String funcName, APIClass, imgSize, query, pubDateType, pubDate;

        public constructCallable(String funcName, String APIClass, String imgSize, String query, String pubDateType, String pubDate) {
            this.funcName = funcName;
            this.APIClass = APIClass;
            this.imgSize = imgSize;
            this.query = query;
            this.pubDateType = pubDateType;
            this.pubDate = pubDate;
        }

        @Override
        public T call() throws Exception {
            switch (funcName.toLowerCase()) {
                // Testing case
                case "dokeytest":
                    return (T) doAPIKeyTest();

                case "getthumbnail":
                    return (T) getThumbnailBitmap(APIClass, imgSize);
                case "getname":
                    return (T) getCreatorDisplayName(APIClass);
                case "simplifycontentinfo":
                    return (T) simplifyContentInfo(APIClass, imgSize, query, pubDateType, pubDate);

                default:
                    return null;
            }
        }
    }

    class getContentStats<T> implements Callable<T> {
        String APIClass, videoID;

        public getContentStats(String APIClass, String videoID) {
            this.APIClass = APIClass;
            this.videoID = videoID;
        }

        @Override
        public T call() throws Exception {
            return (T) GetContentStats(APIClass, videoID);
        }
    }


    class getImageBitmap<T> implements Callable<T> {
        String url;
        public getImageBitmap(String url) {
            this.url = url;
        }

        @Override
        public T call() throws Exception {
            return (T) new GetImageBitmap().processURL(url, callSign);
        }
    }

}

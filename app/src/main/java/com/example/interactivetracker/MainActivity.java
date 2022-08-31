package com.example.interactivetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MainActivity extends AppCompatActivity implements ContentViewInterface {
    String API_KEY;
    private ArrayList<ContentModel> contentList;

    final String API_URL = "https://youtube.googleapis.com/youtube/v3/";
    final String callSign = "MainActivity";

    ConstraintLayout CL_HeaderCL;
    ImageView IV_clAvatar;
    TextView TV_clUsername;
    RecyclerView RV_ContentView;

    public final ExecutorService executor = Executors.newSingleThreadExecutor();
    public String forbidden_URLs = "";


    private void addToContentArray(int fadeGradientID, String videoID, String contentTitle, String contentStats, String thumbnailURL) {
        contentList.add(new ContentModel(fadeGradientID, videoID, contentTitle, contentStats, thumbnailURL));
    }

    private void commit() {
        ContentRecyclerAdapter adapter = new ContentRecyclerAdapter(this, contentList, this);
        RV_ContentView.setLayoutManager(new LinearLayoutManager(this));
        RV_ContentView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contentList = new ArrayList<>();
        // SET CONTENT INFO: addToContentArray(...);
        // SET ADAPTER: commit(...);

        API_KEY = getIntent().getStringExtra("API_KEY");

        Bitmap avatarURL = null;
        String creatorName = null;
        APIObject outerObj = new APIObject(API_URL, "snippet", "markiplierGAME", API_KEY);

        RV_ContentView = findViewById(R.id.RV_ContentView);
        RV_ContentView.setVisibility(View.VISIBLE);

        // GET CREATOR DETAILS AND AVATAR
        List<Future<Bitmap>> futureURL = new ArrayList<>();

        final Future<Bitmap> fURL = executor.submit(outerObj.new constructCallable<>(
                "getthumbnail", "channels", "medium", null, null, null
        ));
        futureURL.add(fURL);

        for (Future<Bitmap> f : futureURL) {
            try {
                avatarURL = f.get();

            } catch (InterruptedException | ExecutionException ignored) { }
        }

        List<Future<String>> futureName = new ArrayList<>();
        final Future<String> fName = executor.submit(outerObj.new constructCallable<>(
                "getname", "channels", null, null, null, null
        ));
        futureName.add(fName);

        for (Future<String> f : futureName) {
            try {
                creatorName = f.get();

            } catch (InterruptedException | ExecutionException ignored) { }
        }

        CL_HeaderCL = findViewById(R.id.CL_HeaderCL);

        IV_clAvatar = findViewById(R.id.IV_clAvatar);
        IV_clAvatar.setImageBitmap(avatarURL);

        TV_clUsername = findViewById(R.id.TV_clUsername);
        String nameBuild = "interactive content from " + creatorName;
        TV_clUsername.setText(nameBuild);
        TV_clUsername.setAllCaps(true);



        // GET INTERACTIVE CONTENT FROM CREATOR
        JSONObject contentArray = new JSONObject();
        String specifiedQueries = "Space|Heist|with Markiplier";

        List<Future<JSONObject>> futureContentInfo = new ArrayList<>();
        final Future<JSONObject> fcInfo = executor.submit(outerObj.new constructCallable<>(
                "simplifycontentinfo", "search", "high", specifiedQueries, "publishedAfter", "2019-10-01T15:00:03Z"
        ));
        futureContentInfo.add(fcInfo);

        for (Future<JSONObject> f : futureContentInfo) {
            try {
                contentArray = f.get();

            } catch (InterruptedException | ExecutionException ignored) { }
        }

        //Log.i(callSign, contentArray.toString());
        ArrayList<String> specifiedQuery = new ArrayList<>(Arrays.asList(specifiedQueries.split("\\|")));


        int fadeGradientID = 0;

        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                Log.i(callSign, "Screen Configuration: UI_MODE_NIGHT_NO | UI_MODE_NIGHT_UNDEFINED");
                fadeGradientID = (R.drawable.fade_gradient);
                break;

            case Configuration.UI_MODE_NIGHT_YES:
                Log.i(callSign, "Screen Configuration: UI_MODE_NIGHT_YES");
                fadeGradientID = (R.drawable.fade_gradient_darkmode);
                break;
        }

        JSONObject listOfTitles = new JSONObject();
        int titlesIndex = 0;

        for(int i=0; i <= contentArray.length(); i++) {
            try {
                JSONObject contentItem = new JSONObject(contentArray.getJSONArray(String.valueOf(i)).getString(0));

                String creator = contentItem.getString("creator");
                String videoID = contentItem.getString("videoId");
                String title = URLDecoder.decode(contentItem.getString("title"), StandardCharsets.UTF_8.toString());
                String description = URLDecoder.decode(contentItem.getString("description"), StandardCharsets.UTF_8.toString());
                String thumbnailURL = contentItem.getString("thumbnailURL");

                if (
                        title.toLowerCase().contains("bloopers") || title.toLowerCase().contains("bts") || title.toLowerCase().contains("trailer")
                ) {
                    continue;
                } else {

                    for(String query : specifiedQuery) {
                        if(title.toLowerCase().contains(query.toLowerCase()) && creator.contains("UC7_YxT-KID8kRbqZo7MyscQ")) {
                            listOfTitles.put(String.valueOf(titlesIndex), title);
                            titlesIndex += 1;

                            forbidden_URLs += videoID + ",";
                            addToContentArray(fadeGradientID , videoID, title, description, thumbnailURL);
                            break;
                        }
                    }
                }
                //Bitmap thumbnailBitmap = contentItem.getString("thumbnailBitmap");
            } catch(JSONException | UnsupportedEncodingException ignored) {  }

            String filename = "content_cache";
            String fileContents = listOfTitles.toString();

            // Apply changes
            commit();
        }
    }

    @Override
    public void onItemClick(int position) {
        //TODO: Make Intent for each contentItem (https://www.youtube.com/watch?v=7GPUpvcU1FE ; time = 06:16)
        Intent intent = new Intent(MainActivity.this, ContentPage.class);

        intent.putExtra("API_KEY", API_KEY);
        //intent.putExtra("FILTERED_TITLES", listOf)

        intent.putExtra("TITLE", contentList.get(position).getContentTitle());
        intent.putExtra("THUMBNAIL", contentList.get(position).getContentThumbnail());
        intent.putExtra("STATS", contentList.get(position).getContentStatistics());
        intent.putExtra("VIDEO_ID", contentList.get(position).getVideoID());

        intent.putExtra("FORBIDDEN_IDS", forbidden_URLs.substring(0, forbidden_URLs.length() - 1));

        startActivity(intent);
        finish();
    }
}
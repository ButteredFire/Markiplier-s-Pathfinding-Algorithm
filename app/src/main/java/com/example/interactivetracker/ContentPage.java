package com.example.interactivetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ContentPage extends AppCompatActivity {
    ImageView IV_HeaderThumbnail, IV_fadeGradient;
    TextView TV_ContentTitle, TV_VideoID, TV_ContentStatistics;
    Button Btn_runAlgorithm, Btn_goBack;

    final String callSign = "ContentPage", API_URL = "https://youtube.googleapis.com/youtube/v3/";
    String title, statistics, videoId, API_KEY;
    String thumbnailURL;
    String forbidden_URLs = null;

    public final ExecutorService executor = Executors.newSingleThreadExecutor();

    @SuppressLint("SetTextI18n") // Disable warnings about "improper" usage of TextView.setText()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_page);

        API_KEY = getIntent().getStringExtra("API_KEY");
        forbidden_URLs = getIntent().getStringExtra("FORBIDDEN_IDS");

        APIObject outerObj = new APIObject(API_URL, "snippet", "markiplierGAME", API_KEY);

        title = getIntent().getStringExtra("TITLE");
        thumbnailURL = getIntent().getStringExtra("THUMBNAIL");

        Bitmap thumbnail = null;

        List<Future<Bitmap>> futures = new ArrayList<>();
        final Future<Bitmap> future = executor.submit(outerObj.new getImageBitmap<>(
                thumbnailURL
        ));
        futures.add(future);

        for (Future<Bitmap> f : futures) {
            try {
                thumbnail = f.get();

            } catch (InterruptedException | ExecutionException ignored) { }
        }

        statistics = getIntent().getStringExtra("STATS");
        videoId = getIntent().getStringExtra("VIDEO_ID");

        IV_HeaderThumbnail = findViewById(R.id.IV_HeaderThumbnail);
        IV_HeaderThumbnail.setImageBitmap(thumbnail);

        TV_ContentTitle = findViewById(R.id.TV_ContentTitle);
        TV_ContentTitle.setText(title);

        TV_VideoID = findViewById(R.id.TV_VideoID);
        TV_VideoID.setText("Video ID: " + videoId);

        TV_ContentStatistics = findViewById(R.id.TV_ContentStatistics);
        TV_ContentStatistics.setText(statistics);

        IV_fadeGradient = findViewById(R.id.IV_fadeGradient);

        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                IV_fadeGradient.setImageResource(R.drawable.fade_gradient);
                break;

            case Configuration.UI_MODE_NIGHT_YES:
                IV_fadeGradient.setImageResource(R.drawable.fade_gradient_darkmode);
                //IV_fadeGradient.setBackgroundResource(R.drawable.fade_gradient_darkmode);
                break;
        }


        final ImageView IV_gearBefore = findViewById(R.id.IV_gearBefore);
        IV_gearBefore.setVisibility(View.INVISIBLE);

        Btn_runAlgorithm = findViewById(R.id.Btn_runAlgorithm);
        Btn_runAlgorithm.setOnClickListener(view -> {
            Intent intent = new Intent(ContentPage.this, ExecutionActivity.class);

            intent.putExtra("ROOT_ID", videoId);
            intent.putExtra("API_KEY", API_KEY);
            intent.putExtra("FORBIDDEN_IDS", forbidden_URLs);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    ContentPage.this, IV_gearBefore, ViewCompat.getTransitionName(IV_gearBefore)
            );

            startActivity(intent, options.toBundle());
            finish();
        });

        Btn_goBack = findViewById(R.id.Btn_goBack);
        Btn_goBack.setOnClickListener(view -> {
            Intent intent = new Intent(ContentPage.this, MainActivity.class);

            intent.putExtra("API_KEY", API_KEY);

            startActivity(intent);
            finish();
        });

    }
}
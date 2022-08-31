package com.example.interactivetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutionActivity extends AppCompatActivity {
    int TEST_CASES = 5;

    String rootVideo, API_KEY;
    int quotasUsed = 0;
    boolean adapterSet = false;
    boolean urlConflicted = false;

    int queryLogs = 10; int queried_logs = 0;
    ArrayList<LogsModel> backgroundLogs = new ArrayList<>();
    ArrayList<String> forbidden_URLs = new ArrayList<>();
    LogsViewAdapter adapter = new LogsViewAdapter(this, backgroundLogs);

    CardView CV_backgroundLogs;
    ImageView IV_gearBefore;
    ProgressBar PB_CreateBuffer;
    RecyclerView RV_logView;
    TextView TV_timeElapsed, TV_algorithmicLayers, TV_possiblePaths, TV_quotasWasted, TV_analyze;

    final String API_URL = "https://youtube.googleapis.com/youtube/v3/";
    final String callSign = "ExecutionActivity";
    public final ExecutorService executor = Executors.newSingleThreadExecutor();
    public final Handler handler = new Handler(Looper.getMainLooper());


    private void addToBackgroundLogs(String logCallSign, String logMessage, String msgColor, LogsViewAdapter adapter) {
        backgroundLogs.add(new LogsModel(logCallSign, logMessage, msgColor));
        commit(adapter);
    }

    private void commit(LogsViewAdapter adapter) {
        runOnUiThread(() -> {
            if(!adapterSet) {
                RV_logView.setAdapter(adapter);
                RV_logView.setLayoutManager(new LinearLayoutManager(this));

                adapterSet = true;
            } else {
                if(queried_logs == queryLogs) {
                    int currentIndex = backgroundLogs.size()-1;
                    adapter.notifyItemInserted(currentIndex);
                    queried_logs = 0;
                } else queried_logs += 1;
                RV_logView.scrollToPosition(backgroundLogs.size() - 1);
            }
        });
    }


    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_execution);

        String videoId = getIntent().getStringExtra("ROOT_ID");
        rootVideo = "https://www.youtube.com/watch?v=" + videoId;

        API_KEY = getIntent().getStringExtra("API_KEY");
        forbidden_URLs = new ArrayList<>(Arrays.asList(getIntent().getStringExtra("FORBIDDEN_IDS").split(",")));
        int listIndex = 0; boolean temp = false;
        while(!temp) {
            try {
                if(forbidden_URLs.get(listIndex).equals(videoId)) {
                    forbidden_URLs.remove(listIndex);
                    break;
                } else {
                    listIndex += 1;
                }
            } catch(Exception e) { temp = true; }
        }

        Log.i(callSign, "ArrayList after removing current videoId: " + forbidden_URLs.toString());

        PB_CreateBuffer = findViewById(R.id.PB_CreateBuffer);
        PB_CreateBuffer.setVisibility(View.INVISIBLE);

        IV_gearBefore = findViewById(R.id.IV_gearBefore);
        IV_gearBefore.setVisibility(View.VISIBLE);

        RotateAnimation rotateAnimation = new RotateAnimation(0, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);

        TV_analyze = findViewById(R.id.TV_analyze);
        TV_analyze.setText("Analyzing...");

        IV_gearBefore.startAnimation(rotateAnimation);

        CV_backgroundLogs = findViewById(R.id.CV_backgroundLogs);
        //CV_backgroundLogs.setVisibility(View.INVISIBLE);

        RV_logView = findViewById(R.id.RV_logView);

        TV_timeElapsed = findViewById(R.id.TV_timeElapsed);
        TV_algorithmicLayers = findViewById(R.id.TV_algorithmicLayers);
        TV_possiblePaths = findViewById(R.id.TV_possiblePaths);

        final int[] elapsedMiliseconds = {0};
        final int[] elapsedSecs = {0};
        final int[] elapsedMins = {0};
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                String secs_filler = "0";
                String mins_filler = "0";
                elapsedMiliseconds[0] += 1;

                if(elapsedMiliseconds[0] == 10) {
                    elapsedSecs[0] += 1;
                    elapsedMiliseconds[0] = 0;
                }

                if(elapsedSecs[0] >= 10) {
                    secs_filler = "";
                }
                if(elapsedSecs[0] == 60) {
                    elapsedMins[0] += 1;
                    elapsedSecs[0] = 0;

                    secs_filler = "0";
                }

                if(elapsedMins[0] >= 10) {
                    mins_filler = "";
                }

                TV_timeElapsed.setText(mins_filler + elapsedMins[0] + ":" + secs_filler + elapsedSecs[0] + ":" + "0" + elapsedMiliseconds[0]);
            }
        }, 0, 100);//wait 0 ms before doing the action and do it every 1000ms (1second)

        //timer.cancel();//stop the timer
        TV_quotasWasted = findViewById(R.id.TV_quotasWasted);
        updateQuotaTV(TV_quotasWasted, quotasUsed);

        // INITIALIZING ALGORITHM

        new Thread(() -> {
            addToBackgroundLogs(
                    "["+elapsedMins[0]+"m:"+elapsedSecs[0]+"s:"+elapsedMiliseconds[0]+"ms]",
                    "Initializing algorithm on Root Node '"+videoId+"'...", "", adapter
            );

            List<String> decisionList = new ArrayList<>();
            List<String> displayNames = new ArrayList<>();

            List<String> recurringUrls = new ArrayList<>();

            // TREE VARIABLES //

            int nodeId = 1;
            int nodeManagerId = 0;

            APIObject rootObj = new APIObject(API_URL, "snippet", "markiplierGAME", API_KEY);
            JSONObject rootNode = new JSONObject();
            List<Future<JSONObject>> future = new ArrayList<>();

            // TREE VARIABLES //

            // SET ROOT NODE //
            JSONObject rootProperties = new JSONObject();

            Future<JSONObject> fContent = executor.submit(rootObj.new getContentStats<>(
                    "videos", videoId
            ));
            future.add(fContent);
            for (Future<JSONObject> f : future) {
                try {
                    rootNode = f.get();
                } catch (InterruptedException | ExecutionException ignored) { }
            }

            try {
                rootProperties.put("title", rootNode.getString("title"));
                rootProperties.put("urlId", videoId);
                rootProperties.put("nodeId", nodeId);
                rootProperties.put("managerId", nodeManagerId);

                try {
                    displayNames.add(nodeId + "-" + rootNode.getString("title") + "-" + nodeManagerId);
                } catch (JSONException ignored) {
                }

                JSONArray urls = new JSONArray();
                for(int i=0; i <= rootNode.getJSONArray("urls").length(); i++) {
                    try {
                        urls.put(rootNode.getJSONArray("urls").get(i));
                    } catch(Exception ignored) { break; }
                }
                rootProperties.put("urls", urls);

                rootNode = new JSONObject().put("0", rootProperties);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            decisionList.add(nodeId + "-" + videoId + "-" + nodeManagerId);

            nodeId += 1; nodeManagerId += 1;
            boolean constructedRoot = false;

            // SET ROOT NODE //

            JSONObject urlList = new JSONObject();
            boolean algoComplete = false;
            int testIterations = 0;

            int testCases = TEST_CASES;

            while(testIterations <= testCases) {
                JSONObject original_parentNode = null;

                if(!constructedRoot) {
                    original_parentNode = rootNode;
                    //Log.i(callSign, "before: " + original_parentNode.toString());
                    constructedRoot = true;
                } else {
                    original_parentNode = urlList;
                    urlList = new JSONObject();
                    //Log.i(callSign, "after: " + original_parentNode.toString());
                }

                boolean iterating = true;
                int nodeIndex = 0;

                while(iterating) {
                    try {
                        JSONObject parentNode = original_parentNode.getJSONObject(String.valueOf(nodeIndex));
                        //int currentNodeId = parentNode.getInt("nodeId");
                        int currentManagerId = parentNode.getInt("nodeId");

                        boolean run = true;
                        int objIndex = 0;
                        while(run) {
                            try {
                                String childUrl = parentNode.getJSONArray("urls").get(objIndex).toString();
                                String compatibleUrl = "";

                                for(int i=0; i < childUrl.split("-").length; i++) {
                                    compatibleUrl += childUrl.split("-")[i] + "_";
                                }

                                JSONObject properties = new JSONObject();
                                String urlId = null;
                                String CompatibleUrlId = null;

                                try {
                                    urlId = childUrl.split("youtu.be/")[1];
                                } catch(ArrayIndexOutOfBoundsException e) {
                                    urlId = childUrl.split("v=")[1];
                                }

                                if(forbidden_URLs.contains(urlId)) {
                                    urlConflicted = true;
                                    break;
                                }

                                try {
                                    CompatibleUrlId = compatibleUrl.split("youtu.be/")[1];
                                } catch(ArrayIndexOutOfBoundsException e) {
                                    CompatibleUrlId = compatibleUrl.split("v=")[1];
                                }

                                JSONObject currentNode = new JSONObject();
                                future = new ArrayList<>();
                                fContent = executor.submit(rootObj.new getContentStats<>(
                                        "videos", urlId
                                ));
                                future.add(fContent);
                                for (Future<JSONObject> f : future) {
                                    try {
                                        currentNode = f.get();
                                    } catch (InterruptedException | ExecutionException ignored) { }
                                }

                                properties.put("title", currentNode.getString("title"));
                                properties.put("urlId", urlId);
                                properties.put("nodeId", nodeId);
                                properties.put("managerId", currentManagerId);

                                try {
                                    displayNames.add(nodeId + "-" + currentNode.getString("title") + "-" + currentManagerId);
                                } catch (JSONException ignored) {
                                }

                                JSONArray urls = new JSONArray();
                                for(int i=0; i <= currentNode.getJSONArray("urls").length(); i++) {
                                    try {
                                        urls.put(currentNode.getJSONArray("urls").get(i));
                                    } catch(Exception ignored) { break; }
                                }
                                properties.put("urls", urls);

                                urlList.put(String.valueOf(objIndex), properties);
                                decisionList.add(
                                        nodeId + "-" + CompatibleUrlId.substring(0, CompatibleUrlId.length() - 1) + "-" + currentManagerId
                                );

                                //Log.i(callSign, urlList.toString());
                                //Log.i(callSign, decisionList.toString());

                                if(recurringUrls.contains(urlId)) {
                                    Log.w(callSign, "Recurring URL: '" + urlId + "'  CurrentNodeId: " + nodeId + "  CurrentManagerId: " + currentManagerId);
                                    addToBackgroundLogs(
                                            "["+elapsedMins[0]+"m:"+elapsedSecs[0]+"s:"+elapsedMiliseconds[0]+"ms]",
                                            "WARNING: Recurring URL: '" + urlId + "', CurrentNodeId: " + nodeId + ", CurrentManagerId: " + currentManagerId, "warning", adapter
                                    );
                                    //RV_logView.scrollToPosition(backgroundLogs.size() - 1);
                                } else {
                                    recurringUrls.add(urlId);
                                }

                                nodeId += 1; objIndex += 1;

                                addToBackgroundLogs("["+elapsedMins[0]+"m:"+elapsedSecs[0]+"s:"+elapsedMiliseconds[0]+"ms]",
                                        nodeId + " nodes processed so far.", "", adapter
                                );
                                //RV_logView.scrollToPosition(backgroundLogs.size() - 1);

                                final int[] handler_paths = {nodeId};
                                handler.post(() -> {
                                    updatePathsTV(TV_possiblePaths, handler_paths[0]);
                                    updateQuotaTV(TV_quotasWasted, handler_paths[0]);
                                });

                            } catch(JSONException e) { run = false; }
                        }
                        nodeIndex += 1;

                    } catch(JSONException e) { iterating = false; }
                }

                testIterations += 1;

                final int[] handler_recursions = {testIterations};
                handler.post(() -> {
                    updateRecursionsTV(TV_algorithmicLayers, handler_recursions[0]);
                });

                if(urlConflicted) break;
            }

            timer.cancel();
            TV_timeElapsed.setTextColor(R.color.green);
            TV_algorithmicLayers.setTextColor(R.color.green);
            TV_possiblePaths.setTextColor(R.color.green);

            rotateAnimation.cancel();
            rotateAnimation.reset();

            handler.post(() -> {
                    IV_gearBefore.setVisibility(View.INVISIBLE);
                    PB_CreateBuffer.setVisibility(View.VISIBLE);
                }
            );

            TV_analyze.setText("Algorithm finished.");

            String[] finalList = decisionList.toArray(new String[0]);
            DecisionTree List_tree = new DecisionTree();
            List_tree.createMap(finalList);
            List_tree.build(List_tree.rootNode);
            //List_tree.printTree(List_tree.rootNode, 0);
            JSONObject treeData_list = List_tree.getResults();


            String[] finalNames = displayNames.toArray(new String[0]);
            DecisionTree Name_tree = new DecisionTree();
            Name_tree.createMap(finalNames);
            Name_tree.build(Name_tree.rootNode);
            //Name_tree.printTree(Name_tree.rootNode, 0);
            JSONObject treeData_name = Name_tree.getResults();


            addToBackgroundLogs("["+elapsedMins[0]+"m:"+elapsedSecs[0]+"s:"+elapsedMiliseconds[0]+"ms]",
                    "Finished algorithm in " +elapsedMins[0]+"m:"+elapsedSecs[0]+"s:"+elapsedMiliseconds[0]+ "ms. Attempting to generate hierarchy tree for " + nodeId + " nodes.", "success", adapter
            );

            Log.i(callSign, "Attempting to generate tree...");

            handler.postDelayed(() -> {
                Intent intent = new Intent(ExecutionActivity.this, Results_Kotlin.class);
                intent.putExtra("RAW_DATA", displayNames.toArray(new String[0]));

                startActivity(intent);
            }, 1000);
        }).start();

    }




    @SuppressLint("SetTextI18n")
    public void updateQuotaTV(TextView tvQuotas, int quotasUsed) {
        tvQuotas.setText("Estimated Amount of Quotas Used: " + quotasUsed + " Units");
    }

    @SuppressLint("SetTextI18n")
    public void updatePathsTV(TextView tvPaths, int paths) {
        tvPaths.setText(paths + " paths detected.");
    }

    @SuppressLint("SetTextI18n")
    public void updateRecursionsTV(TextView tvRecursions, int recursions) {
        tvRecursions.setText(recursions + " full recursions completed.");
    }
}

package com.example.interactivetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration;
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager;


public class ResultsActivity extends AppCompatActivity {
    final String callSign = "ResultsActivity";
    JSONObject treeData = new JSONObject();

    RecyclerView RV_dataStructure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results2);

        try {
            treeData = new JSONObject(getIntent().getStringExtra("TREE_DATA"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(callSign, treeData.toString());
        RV_dataStructure = findViewById(R.id.RV_dataStructure);
    }
}
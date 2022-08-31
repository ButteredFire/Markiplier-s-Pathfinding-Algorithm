package com.example.interactivetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class APIKeyRequest extends AppCompatActivity {
    TextInputLayout TIL_APIKey;
    CheckBox CB_SaveKey;
    ProgressBar PB_Buffer;

    TextView textView_Description, TV_keyErrorWarning, TV_tempView;
    Button Btn_UseAPIKey;

    final String callSign = "APIKeyRequest";
    String User_API_Key = "";
    Boolean saveKey = false;
    final String API_URL = "https://youtube.googleapis.com/youtube/v3/";

    public final ExecutorService executor = Executors.newSingleThreadExecutor();
    public final Handler handler = new Handler(Looper.getMainLooper());

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apikey_request);

        TIL_APIKey = findViewById(R.id.TIL_APIKey);
        CB_SaveKey = findViewById(R.id.CB_SaveKey);
        CB_SaveKey.setOnClickListener(view -> saveKey = CB_SaveKey.isChecked());

        PB_Buffer = findViewById(R.id.PB_Buffer);
        PB_Buffer.setVisibility(View.INVISIBLE);

        textView_Description = findViewById(R.id.textView_Description);

        TV_keyErrorWarning = findViewById(R.id.TV_keyErrorWarning);
        TV_keyErrorWarning.setVisibility(View.INVISIBLE);

        TV_tempView = findViewById(R.id.TV_tempView);
        TV_tempView.setVisibility(View.INVISIBLE);

        Btn_UseAPIKey = findViewById(R.id.Btn_UseAPIKey);
        Btn_UseAPIKey.setOnClickListener(view -> {
            TV_keyErrorWarning.setVisibility(View.VISIBLE);
            TV_keyErrorWarning.setTextColor(R.color.yellow);
            TV_keyErrorWarning.setText("Checking API Key validity...");

            try {
                Objects.requireNonNull(TIL_APIKey.getEditText()).onEditorAction(EditorInfo.IME_ACTION_DONE);
                User_API_Key = TIL_APIKey.getEditText().getText().toString();
            } catch(NullPointerException e) {e.printStackTrace();}

            handler.postDelayed(() -> {

                if (TextUtils.isEmpty(User_API_Key))
                {
                    Toast.makeText(this,
                            "An API Key is required.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(callSign, "Inputted API Key: '" + User_API_Key + "'. Attempting to process key.");

                    TV_tempView.setVisibility(View.VISIBLE);
                    PB_Buffer.setVisibility(View.VISIBLE);

                    try {
                        JSONObject errorArray = new JSONObject();

                        List<Future<JSONObject>> future = new ArrayList<>();
                        final Future<JSONObject> fErrors = executor.submit(new APIObject(API_URL, "snippet", "markiplierGAME", User_API_Key).new constructCallable<>(
                                "doKeyTest", "channels",null,null,null,null
                        ));
                        future.add(fErrors);

                        for (Future<JSONObject> f : future) {
                            try {
                                errorArray = f.get();

                            } catch (InterruptedException | ExecutionException ignored) { }
                        }

                        if(errorArray.getJSONObject("error").getInt("status") == APIObject.VALID) {
                            TV_keyErrorWarning.setVisibility(View.INVISIBLE);

                            handler.post(() -> {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("API_KEY", User_API_Key);
                                startActivity(intent);

                                finish();
                            });

                        } else {
                            PB_Buffer.setVisibility(View.INVISIBLE);
                            TV_tempView.setVisibility(View.INVISIBLE);

                            TV_keyErrorWarning.setVisibility(View.VISIBLE);

                            String errString = "Error: " + errorArray.getJSONObject("error").getString("message");
                            TV_keyErrorWarning.setText(errString);
                        }

                    } catch(Exception e) {
                        PB_Buffer.setVisibility(View.INVISIBLE);
                        TV_tempView.setVisibility(View.INVISIBLE);

                        TV_keyErrorWarning.setVisibility(View.VISIBLE);
                        TV_keyErrorWarning.setTextColor(R.color.red);

                        String errString = "Error: API Key is either invalid or cannot be processed.";
                        TV_keyErrorWarning.setText(errString);

                        //e.printStackTrace();
                    }
                }

            }, 300);
        });


    }
}
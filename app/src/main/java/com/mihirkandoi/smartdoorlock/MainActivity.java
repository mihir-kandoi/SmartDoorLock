package com.mihirkandoi.smartdoorlock;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    TextView conn_result;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Glide.with(this).load(R.drawable.iot).into((ImageView) findViewById(R.id.imageView));

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Visitors";
            String description = "Channel to receive notifications when someone is at the door";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("visitors", name, importance);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.doorbell_sound_effect), audioAttributes);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        conn_result = findViewById(R.id.conn_result);
        progressBar = findViewById(R.id.progressBar);

        // testing connection to nodemcu
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.0.100:80/";
        try {
            JSONObject json = new JSONObject();
            json.put("token", getApplicationContext().getSharedPreferences("token_file", Context.MODE_PRIVATE).getString("FCM Token",""));
            final String jsonBody = json.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, (Response.Listener<String>) response -> {
                // Display the first 500 characters of the response string.
                conn_result.setText("Connection is successful");
                progressBar.setVisibility(View.INVISIBLE);
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(MainActivity.this, Dashboard.class));
                    finish();
                }, 1500);
            }, (Response.ErrorListener) error -> {
                conn_result.setText("Error connecting to NodeMCU");
                progressBar.setVisibility(View.INVISIBLE);
                Log.e("NodeMCU connection error", error.toString());
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    return jsonBody == null ? null : jsonBody.getBytes(StandardCharsets.UTF_8);
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
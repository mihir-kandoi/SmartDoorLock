package com.example.miniproject;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class Dashboard extends AppCompatActivity {

    final String TAG = "mihir";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        getSupportActionBar().setTitle("Dashboard");

        Button lock = findViewById(R.id.lock);
        Button unlock = findViewById(R.id.unlock);

        Button startStream = findViewById(R.id.btn_startStream);
        Button stopStream = findViewById(R.id.btn_stopStream);
        WebView webView = findViewById(R.id.webView);

        TextView status = findViewById(R.id.status);
        new Handler(Looper.getMainLooper()).post(() -> {

        });

        //setup request params
        RequestQueue queue = Volley.newRequestQueue(this);
        String esp8266url = "http://192.168.0.100:80/";

        lock.setOnClickListener(v -> {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, esp8266url + "lock", response -> {
                Toast.makeText(this, "Door locked successfully", Toast.LENGTH_SHORT).show();
            }, error -> {
                Toast.makeText(this, "Door lock request failed", Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.toString());
            });
            queue.add(stringRequest);
            getSystemService(NotificationManager.class).cancel(0);
        });

        unlock.setOnClickListener(v -> {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, esp8266url + "unlock", response -> {
                Toast.makeText(this, "Door unlocked successfully", Toast.LENGTH_SHORT).show();
            }, error -> {
                Toast.makeText(this, "Door unlock request failed", Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.toString());
            });
            queue.add(stringRequest);
            getSystemService(NotificationManager.class).cancel(0);
        });

        startStream.setOnClickListener(v -> webView.loadUrl("http://192.168.0.184:81/stream"));
        stopStream.setOnClickListener(v -> webView.stopLoading());

        String action = getIntent().getStringExtra("action");
        if(action != null)
            if (action.equals("unlock door"))
                unlock.callOnClick();
            else if (action.equals("start stream"))
                startStream.callOnClick();
    }
}
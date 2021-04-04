package com.example.miniproject;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.concurrent.Executor;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

public class Dashboard extends AppCompatActivity {

    final String TAG = "MIHIR";
    boolean isAuthenticated, canAuthenticate;

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

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
        switch (BiometricManager.from(this).canAuthenticate(BIOMETRIC_STRONG | (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ? 0 : DEVICE_CREDENTIAL))) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                canAuthenticate = true;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Note: This device does not support Biometric Authentication", Toast.LENGTH_LONG).show();
                canAuthenticate = false;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "For better security, please enroll your Biometrics in your phone", Toast.LENGTH_LONG).show();
                canAuthenticate = false;
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Biometric hardware unavailable", Toast.LENGTH_LONG).show();
                canAuthenticate = false;
                break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                Toast.makeText(this, "Security update required to use Biometric Authentication", Toast.LENGTH_LONG).show();
                canAuthenticate = false;
                break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                Toast.makeText(this, "Biometric Authentication unsupported", Toast.LENGTH_LONG).show();
                canAuthenticate = false;
                break;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                Toast.makeText(this, "Biometric Authentication status unknown", Toast.LENGTH_LONG).show();
                canAuthenticate = false;
                break;
        }

        BiometricPrompt.PromptInfo prompt;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            prompt = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authenticate to perform action")
                    .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                    .build();
        } else {
            prompt = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authenticate to perform action")
                    .setNegativeButtonText("Cancel")
                    .build();
        }

        //setup request params
        RequestQueue queue = Volley.newRequestQueue(this);
        String esp8266url = "http://192.168.0.100:80/";

        lock.setOnClickListener(v -> {
            if(canAuthenticate || keyguardManager.isDeviceSecure()) {
                Runnable runnable = () -> {
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, esp8266url + "lock", response -> {
                        Toast.makeText(this, "Door locked successfully", Toast.LENGTH_SHORT).show();
                    }, error -> {
                        Toast.makeText(this, "Door lock request failed", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, error.toString());
                    });
                    queue.add(stringRequest);
                    getSystemService(NotificationManager.class).cancel(0);
                };
                new BiometricPrompt(Dashboard.this, ContextCompat.getMainExecutor(Dashboard.this), new CustomAuthenticationCallback(runnable)).authenticate(prompt);
            }
        });

        unlock.setOnClickListener(v -> {
            if(canAuthenticate || keyguardManager.isDeviceSecure()) {
                Runnable runnable = () -> {
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, esp8266url + "unlock", response -> {
                        Toast.makeText(this, "Door unlocked successfully", Toast.LENGTH_SHORT).show();
                    }, error -> {
                        Toast.makeText(this, "Door unlock request failed", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, error.toString());
                    });
                    queue.add(stringRequest);
                    getSystemService(NotificationManager.class).cancel(0);
                };
                new BiometricPrompt(Dashboard.this, ContextCompat.getMainExecutor(Dashboard.this), new CustomAuthenticationCallback(runnable)).authenticate(prompt);
            }
        });

        startStream.setOnClickListener(v -> webView.loadUrl("http://192.168.0.184:81/stream"));
        stopStream.setOnClickListener(v -> webView.stopLoading());

        String action = getIntent().getStringExtra("action");
        if (action != null)
            if (action.equals("unlock door"))
                unlock.callOnClick();
            else if (action.equals("start stream"))
                startStream.callOnClick();
    }

    class CustomAuthenticationCallback extends BiometricPrompt.AuthenticationCallback {

        Runnable runnable;

        public CustomAuthenticationCallback(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Toast.makeText(Dashboard.this, "Authentication error, unable to perform action", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            runnable.run();
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Toast.makeText(Dashboard.this, "Authentication failed, unable to perform action", Toast.LENGTH_LONG).show();
        }
    }
}
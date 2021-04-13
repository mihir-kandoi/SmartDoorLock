package com.mihirkandoi.smartdoorlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

public class Dashboard extends AppCompatActivity {

    final String TAG = "MIHIR";
    boolean canAuthenticate;
    CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        MaterialButton lock = findViewById(R.id.btn_lock);
        MaterialButton stream = findViewById(R.id.btn_stream);
        WebView webView = findViewById(R.id.webView);
        coordinatorLayout = findViewById(R.id.coordinator);

        DrawerLayout drawerLayout = findViewById(R.id.drawer);
        findViewById(R.id.open_nav).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        ((NavigationView) findViewById(R.id.nav_view)).setNavigationItemSelectedListener(item -> {
            if (item.getTitle().toString().equals("About"))
                new AlertDialog.Builder(Dashboard.this)
                        .setTitle("Made with ❤ by")
                        .setMessage("Mihir Kandoi - A073\nDev Desai - A070\nDhruv Chhatrala - A068")
                        .setPositiveButton("Ok", null)
                        .show();
            return true;
        });

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
        switch (BiometricManager.from(this).canAuthenticate(BIOMETRIC_STRONG | (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ? 0 : DEVICE_CREDENTIAL))) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                canAuthenticate = true;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Snackbar.make(coordinatorLayout, "Note: This device does not support Biometric Authentication", Snackbar.LENGTH_SHORT).show();
                canAuthenticate = false;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Snackbar.make(coordinatorLayout, "For better security, please enroll your Biometrics in your phone", Snackbar.LENGTH_SHORT).show();
                canAuthenticate = false;
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Snackbar.make(coordinatorLayout, "Biometric hardware unavailable", Snackbar.LENGTH_SHORT).show();
                canAuthenticate = false;
                break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                Snackbar.make(coordinatorLayout, "Security update required to use Biometric Authentication", Snackbar.LENGTH_SHORT).show();
                canAuthenticate = false;
                break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                Snackbar.make(coordinatorLayout, "Biometric Authentication unsupported", Snackbar.LENGTH_SHORT).show();
                canAuthenticate = false;
                break;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                Snackbar.make(coordinatorLayout, "Biometric Authentication status unknown", Snackbar.LENGTH_SHORT).show();
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
            if (canAuthenticate || keyguardManager.isDeviceSecure()) {
                if (lock.getText().toString().equals("Lock Door")) {
                    Runnable runnable = () -> {
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, esp8266url + "lock", response -> {
                            Snackbar.make(coordinatorLayout, "Door locked successfully", Snackbar.LENGTH_SHORT).show();
                            lock.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_lock_open_24, getTheme()));
                            lock.setText("Unlock Door");
                        }, error -> {
                            Snackbar.make(coordinatorLayout, "Door lock request failed", Snackbar.LENGTH_SHORT).show();
                            Log.e(TAG, error.toString());
                        });
                        queue.add(stringRequest);
                        getSystemService(NotificationManager.class).cancel(0);
                    };
                    new BiometricPrompt(Dashboard.this, ContextCompat.getMainExecutor(Dashboard.this), new CustomAuthenticationCallback(runnable)).authenticate(prompt);
                } else {
                    Runnable runnable = () -> {
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, esp8266url + "unlock", response -> {
                            Snackbar.make(coordinatorLayout, "Door unlocked successfully", Snackbar.LENGTH_SHORT).show();
                            lock.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_outline_lock_24, getTheme()));
                            lock.setText("Lock Door");
                        }, error -> {
                            Snackbar.make(coordinatorLayout, "Door unlock request failed", Snackbar.LENGTH_SHORT).show();
                            Log.e(TAG, error.toString());
                        });
                        queue.add(stringRequest);
                        getSystemService(NotificationManager.class).cancel(0);
                    };
                    new BiometricPrompt(Dashboard.this, ContextCompat.getMainExecutor(Dashboard.this), new CustomAuthenticationCallback(runnable)).authenticate(prompt);
                }
            }
        });

        stream.setOnClickListener(v -> {
            if (stream.getText().toString().equals("Start Stream")) {
                webView.loadUrl("http://192.168.0.184:81/stream");
                stream.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_videocam_off_24, getTheme()));
                stream.setText("Stop Stream");
            } else {
                webView.stopLoading();
                webView.loadUrl("about:blank");
                stream.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_videocam_24, getTheme()));
                stream.setText("Start Stream");
            }
        });

        String action = getIntent().getStringExtra("action");
        if (action != null)
            if (action.equals("unlock door")) {
                lock.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_lock_open_24, getTheme()));
                lock.setText("Unlock Door");
                lock.callOnClick();
            } else if (action.equals("start stream")) {
                stream.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_videocam_24, getTheme()));
                stream.setText("Start Stream");
                stream.callOnClick();
            }
    }

    class CustomAuthenticationCallback extends BiometricPrompt.AuthenticationCallback {

        Runnable runnable;

        public CustomAuthenticationCallback(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Snackbar.make(coordinatorLayout, "Authentication error, unable to perform action", Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            runnable.run();
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Snackbar.make(coordinatorLayout, "Authentication failed, unable to perform action", Snackbar.LENGTH_SHORT).show();
        }
    }
}
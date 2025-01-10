package com.example.wallextra;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wallextra.views.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String token = task.getResult();
                        Log.d("firebase test", token);
                    }
                });

        // subscribe to FCM topic for daily notificaation
        SharedPreferences sharedPreferences = getSharedPreferences("WallextraPreferences", Context.MODE_PRIVATE);
        boolean isDailyTopicSubscribed = sharedPreferences.getBoolean("isDailyTopicSubscribed", false);

        if(!isDailyTopicSubscribed) {
            FirebaseMessaging.getInstance().subscribeToTopic("daily-wallextra")
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase Messaging Init", "Subscribed to daily notification topic");
                        sharedPreferences.edit().putBoolean("isDailyTopicSubscribed", true).apply();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.d("Firebase Messaging Init", "Already subscribed to daily notification topic");
        }

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
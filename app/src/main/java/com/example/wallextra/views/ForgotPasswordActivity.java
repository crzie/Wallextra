package com.example.wallextra.views;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.wallextra.R;
import com.example.wallextra.databinding.ActivityForgotPasswordBinding;
import com.example.wallextra.viewmodels.AuthViewModel;

public class ForgotPasswordActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setContentView(binding.getRoot());

        binding.sendButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString();
            authViewModel.sendPasswordResetEmail(email);
        });

        authViewModel.getResetPasswordState().observe(this, response -> {
            if(response.isSuccess()) {
                finish();
            } else {
                Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
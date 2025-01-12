package com.example.wallextra.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.wallextra.databinding.ActivityLoginBinding;
import com.example.wallextra.viewmodels.AuthViewModel;

public class LoginActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setContentView(binding.getRoot());

        if(authViewModel.getCurrentUser() != null) {
            startActivity(new Intent(this, AppActivity.class)); // move to home
            finish();
        }

        binding.registerText.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        binding.forgotPasswordLink.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        binding.loginButton.setOnClickListener(v -> {
            doLogin();
        });

        authViewModel.getLoginState().observe(this, response -> {
            if(response.isSuccess()) {
                startActivity(new Intent(this, AppActivity.class)); // move to home
                finish();
            } else {
                Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void doLogin() {
        String email = binding.emailEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Password is minimum 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        authViewModel.login(email, password);
    }
}
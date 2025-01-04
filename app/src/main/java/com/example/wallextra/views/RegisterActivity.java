package com.example.wallextra.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.wallextra.databinding.ActivityRegisterBinding;
import com.example.wallextra.viewmodels.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setContentView(binding.getRoot());

        binding.loginText.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        binding.registerButton.setOnClickListener(v -> {
            doRegister();
        });

        authViewModel.getRegisterState().observe(this, response -> {
            if(response.isSuccess()) {
                startActivity(new Intent(this, AppActivity.class)); // move to home
                finish();
            } else {
                Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void doRegister() {
        String email = binding.emailEditText.getText().toString();
        String name = binding.nameEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString();

        if (email.isBlank() || name.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Password must be the same as confirm password", Toast.LENGTH_SHORT).show();
            return;
        }

        authViewModel.register(email, password, name);
    }
}
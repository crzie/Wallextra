package com.example.wallextra.views;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.wallextra.R;
import com.example.wallextra.databinding.ActivityAppBinding;
import com.example.wallextra.viewmodels.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;

public class AppActivity extends AppCompatActivity {
    private ActivityAppBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppBinding.inflate(getLayoutInflater());
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setContentView(binding.getRoot());

        FirebaseUser user = authViewModel.getCurrentUser();
        if(user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (navHostFragment == null) {
            navHostFragment = NavHostFragment.create(R.navigation.nav_graph);
            NavHostFragment finalNavHostFragment = navHostFragment;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, navHostFragment)
                    .setPrimaryNavigationFragment(navHostFragment) // Handles back button behavior
                    .runOnCommit(() -> { // Ensure NavController is accessed after attachment
                        NavController navController = finalNavHostFragment.getNavController();
                        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
                    })
                    .commit();
        } else {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }
    }
}
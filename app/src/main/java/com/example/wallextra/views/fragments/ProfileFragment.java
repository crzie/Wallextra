package com.example.wallextra.views.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wallextra.R;
import com.example.wallextra.databinding.FragmentProfileBinding;
import com.example.wallextra.viewmodels.AuthViewModel;
import com.example.wallextra.views.LoginActivity;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        FirebaseUser user = authViewModel.getCurrentUser();

        binding.setUser(user);
        binding.logoutButton.setOnClickListener(v -> {
            authViewModel.logout();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });
        return binding.getRoot();
    }
}
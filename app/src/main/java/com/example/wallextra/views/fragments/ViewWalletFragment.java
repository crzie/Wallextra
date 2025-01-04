package com.example.wallextra.views.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wallextra.R;
import com.example.wallextra.databinding.FragmentHomeBinding;
import com.example.wallextra.databinding.FragmentViewWalletBinding;
import com.example.wallextra.viewmodels.WalletViewModel;

public class ViewWalletFragment extends Fragment {
    private WalletViewModel walletViewModel;
    private FragmentViewWalletBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewWalletBinding.inflate(inflater, container, false);
        walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

        walletViewModel.fetchUserWallets();
        binding.deleteButton.setOnClickListener(v -> {
            binding.walletRecyclerViewView.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.GONE);
            binding.walletRecyclerViewDelete.setVisibility(View.VISIBLE);
            binding.viewButton.setVisibility(View.VISIBLE);
        });

        binding.viewButton.setOnClickListener(v -> {
            binding.walletRecyclerViewDelete.setVisibility(View.GONE);
            binding.viewButton.setVisibility(View.GONE);
            binding.walletRecyclerViewView.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.VISIBLE);
        });

        walletViewModel.getFetchWalletState().observe(getViewLifecycleOwner(), response -> {
            
        });
        return binding.getRoot();
    }
}
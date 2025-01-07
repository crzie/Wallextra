package com.example.wallextra.views.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.wallextra.R;
import com.example.wallextra.databinding.FragmentHomeBinding;
import com.example.wallextra.databinding.FragmentViewWalletBinding;
import com.example.wallextra.models.MutableBoolean;
import com.example.wallextra.models.Wallet;
import com.example.wallextra.viewmodels.WalletViewModel;
import com.example.wallextra.views.adapters.ViewWalletAdapter;

import java.util.ArrayList;

public class ViewWalletFragment extends Fragment {
    private WalletViewModel walletViewModel;
    private FragmentViewWalletBinding binding;
    private MutableBoolean isDeleting = new MutableBoolean(false);
    private ArrayList<Wallet> wallets = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewWalletBinding.inflate(inflater, container, false);
        walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

        walletViewModel.fetchUserWallets();
        ViewWalletAdapter adapter = new ViewWalletAdapter(wallets, isDeleting);
        binding.walletRecyclerView.setAdapter(adapter);
        binding.walletRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.walletRecyclerView.setHasFixedSize(true);

        binding.deleteButton.setOnClickListener(v -> {
            binding.deleteButton.setVisibility(View.GONE);
            binding.viewButton.setVisibility(View.VISIBLE);
            isDeleting.value = true;
            adapter.notifyDataSetChanged();
        });

        binding.viewButton.setOnClickListener(v -> {
            binding.viewButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);
            isDeleting.value = false;
            adapter.notifyDataSetChanged();
        });

        wallets.add(new Wallet("wl", "bca", 10000L, "lol", "asd"));
        adapter.notifyDataSetChanged();
        walletViewModel.getFetchWalletState().observe(getViewLifecycleOwner(), response -> {
            if(response.isSuccess()) {
                wallets = response.getData();
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return binding.getRoot();
    }
}
package com.example.wallextra.views.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.wallextra.R;
import com.example.wallextra.databinding.FragmentAddTransactionBinding;
import com.example.wallextra.databinding.FragmentTransferWalletBinding;
import com.example.wallextra.models.Wallet;
import com.example.wallextra.viewmodels.WalletViewModel;

import java.util.ArrayList;

public class TransferWalletFragment extends Fragment {
    private FragmentTransferWalletBinding binding;
    private WalletViewModel walletViewModel;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTransferWalletBinding.inflate(inflater, container, false);
        walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);

        binding.closeIcon.setOnClickListener(e -> {
            back();
        });

        binding.transferButton.setOnClickListener(e -> {

        });
        
        return binding.getRoot();
    }

    private void initializeDropdownWallet() {
        ArrayList<Wallet> wallets = new ArrayList<>();
        ArrayAdapter<Wallet> adapter = new ArrayAdapter<>(requireContext(), R.layout.list_item, wallets);

        walletViewModel.getFetchWalletState().observe(getViewLifecycleOwner(), response -> {
            if(response.isSuccess()) {
                wallets.clear();
                wallets.addAll(response.getData());
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.ddlFromWalletText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Wallet selectedWallet = adapter.getItem(position);
            }
        });

        binding.ddlToWalletText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Wallet selectedWallet = adapter.getItem(position);
            }
        });

        binding.ddlFromWalletText.setAdapter(adapter);
        binding.ddlToWalletText.setAdapter(adapter);
    }

    private void back() {
        NavController navController = Navigation.findNavController(requireView());
        navController.popBackStack();
    }
}
package com.example.wallextra.views.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.wallextra.R;
import com.example.wallextra.databinding.FragmentHomeBinding;
import com.example.wallextra.models.Transaction;
import com.example.wallextra.models.Wallet;
import com.example.wallextra.models.WalletTransfer;
import com.example.wallextra.viewmodels.AuthViewModel;
import com.example.wallextra.viewmodels.TransactionViewModel;
import com.example.wallextra.viewmodels.WalletTransferViewModel;
import com.example.wallextra.viewmodels.WalletViewModel;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private AuthViewModel authViewModel;
    private WalletViewModel walletViewModel;
    private TransactionViewModel transactionViewModel;
    private WalletTransferViewModel walletTransferViewModel;
    private FragmentHomeBinding binding;
    private ArrayList<WalletTransfer> walletTransfers;
    private ArrayList<Transaction> transactions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        walletTransferViewModel = new ViewModelProvider(this).get(WalletTransferViewModel.class);

        FirebaseUser user = authViewModel.getCurrentUser();
        walletViewModel.fetchUserWallets();
        transactionViewModel.fetchRecentUserTransactions(5);
        walletTransferViewModel.fetchRecentUserWalletTransfers(5);

        binding.setUsername(user.getDisplayName());
        walletViewModel.getFetchWalletState().observe(getViewLifecycleOwner(), response -> {
            if(response.isSuccess()) {
                Long sum = 0L;
                for (Wallet wallet: response.getData()) {
                    sum += wallet.getBalance();
                }
                binding.setTotalNetBalance(sum);
            } else {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return binding.getRoot();
    }
}
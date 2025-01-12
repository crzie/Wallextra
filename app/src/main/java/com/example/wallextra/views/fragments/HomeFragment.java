package com.example.wallextra.views.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.wallextra.R;
import com.example.wallextra.databinding.FragmentHomeBinding;
import com.example.wallextra.models.BaseTransaction;
import com.example.wallextra.models.Transaction;
import com.example.wallextra.models.Wallet;
import com.example.wallextra.models.WalletTransfer;
import com.example.wallextra.utils.MutableBoolean;
import com.example.wallextra.viewmodels.AuthViewModel;
import com.example.wallextra.viewmodels.TransactionViewModel;
import com.example.wallextra.viewmodels.WalletTransferViewModel;
import com.example.wallextra.viewmodels.WalletViewModel;
import com.example.wallextra.views.adapters.ViewTransactionAdapter;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private static final int RECENT_TRANSACTION_LIMIT = 5;
    private AuthViewModel authViewModel;
    private WalletViewModel walletViewModel;
    private TransactionViewModel transactionViewModel;
    private WalletTransferViewModel walletTransferViewModel;
    private FragmentHomeBinding binding;
    private ArrayList<BaseTransaction> baseTransactions = new ArrayList<>();
    private MutableLiveData<ArrayList<Transaction>> transactions = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<ArrayList<WalletTransfer>> walletTransfers = new MutableLiveData<>(new ArrayList<>());

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
        transactionViewModel.fetchRecentUserTransactions(RECENT_TRANSACTION_LIMIT);
        walletTransferViewModel.fetchRecentUserWalletTransfers(RECENT_TRANSACTION_LIMIT);

        ViewTransactionAdapter adapter = new ViewTransactionAdapter(
                baseTransactions,
                new MutableBoolean(false),
                null,
                null
        );
        binding.baseTransactionRecyclerView.setAdapter(adapter);
        binding.baseTransactionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.baseTransactionRecyclerView.setHasFixedSize(true);

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

        transactionViewModel.getFetchTransactionState().observe(getViewLifecycleOwner(), response -> {
            if(response.isSuccess()) {
                transactions.setValue(response.getData());
            } else {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        walletTransferViewModel.getFetchWalletTransferState().observe(getViewLifecycleOwner(), response -> {
            if(response.isSuccess()) {
                walletTransfers.setValue(response.getData());
            } else {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        transactions.observe(getViewLifecycleOwner(), transactionArrayList -> {
            baseTransactions.clear();
            baseTransactions.addAll(transactionArrayList);
            baseTransactions.addAll(Objects.requireNonNull(walletTransfers.getValue()));
            if(baseTransactions.isEmpty()) {
                binding.noTransactionsTextView.setVisibility(View.VISIBLE);
            } else {
                binding.noTransactionsTextView.setVisibility(View.GONE);
            }
            Collections.sort(baseTransactions);

            if(baseTransactions.size() > RECENT_TRANSACTION_LIMIT) {
                ArrayList<BaseTransaction> recentBaseTransactions = new ArrayList<>(baseTransactions.subList(0, RECENT_TRANSACTION_LIMIT));
                baseTransactions.clear();
                baseTransactions.addAll(recentBaseTransactions);
            }
            adapter.notifyDataSetChanged();
        });

        walletTransfers.observe(getViewLifecycleOwner(), walletTransfersArrayList -> {
            baseTransactions.clear();
            baseTransactions.addAll(walletTransfersArrayList);
            baseTransactions.addAll(Objects.requireNonNull(transactions.getValue()));
            if(baseTransactions.isEmpty()) {
                binding.noTransactionsTextView.setVisibility(View.VISIBLE);
            } else {
                binding.noTransactionsTextView.setVisibility(View.GONE);
            }
            Collections.sort(baseTransactions);

            if(baseTransactions.size() > RECENT_TRANSACTION_LIMIT) {
                ArrayList<BaseTransaction> recentBaseTransactions = new ArrayList<>(baseTransactions.subList(0, RECENT_TRANSACTION_LIMIT));
                baseTransactions.clear();
                baseTransactions.addAll(recentBaseTransactions);
            }
            adapter.notifyDataSetChanged();
        });

        return binding.getRoot();
    }
}
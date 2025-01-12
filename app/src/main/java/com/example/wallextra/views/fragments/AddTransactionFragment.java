package com.example.wallextra.views.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.wallextra.R;
import com.example.wallextra.databinding.FragmentAddTransactionBinding;
import com.example.wallextra.databinding.FragmentViewTransactionBinding;
import com.example.wallextra.models.TransactionType;
import com.example.wallextra.models.Wallet;
import com.example.wallextra.viewmodels.TransactionViewModel;
import com.example.wallextra.viewmodels.WalletViewModel;
import com.example.wallextra.views.adapters.WalletDropdownAdapter;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class AddTransactionFragment extends Fragment {
    private FragmentAddTransactionBinding binding;
    private WalletViewModel walletViewModel;
    private TransactionViewModel transactionViewModel;
    private ArrayList<Wallet> wallets = new ArrayList<>();
    private WalletDropdownAdapter walletDropdownAdapter;
    private Wallet selectedWallet = null;
    private TransactionType selectedType = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);
        walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        walletDropdownAdapter = new WalletDropdownAdapter(requireContext(), wallets);
        walletViewModel.fetchUserWallets();

        initializeDropdownType();
        initializeDropdownWallet();
        initializeValidationListener();

        binding.addButton.setOnClickListener(e -> submitTransaction());
        binding.closeIcon.setOnClickListener(e -> back());

        walletViewModel.getFetchWalletState().observe(getViewLifecycleOwner(), response -> {
            if(response.isSuccess()) {
                wallets.clear();
                wallets.addAll(response.getData());
                walletDropdownAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        transactionViewModel.getAddTransactionState().observe(getViewLifecycleOwner(), response -> {
            if(response == null) return;

            if(response.isSuccess()) {
                back();
            } else {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        return binding.getRoot();
    }

    private void submitTransaction() {
        String name = binding.transactionNameEditText.getText().toString();
        String amountText = binding.amountEditText.getText().toString();
        Long amount = null;

        try {
            amount = Long.parseLong(amountText);
        } catch(Exception ignored) {}

        if(name.isBlank() || amount == null || selectedType == null || selectedWallet == null) {
            Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        transactionViewModel.addTransaction(name, selectedType, amount, selectedWallet);
    }

    private void initializeDropdownType() {
        TransactionType[] types = TransactionType.values();

        ArrayAdapter<TransactionType> adapter = new ArrayAdapter<>(requireContext(), R.layout.list_item, types);

        binding.ddlTypeText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedType = adapter.getItem(position);
                validateAmount();
            }
        });

        binding.ddlTypeText.setAdapter(adapter);
    }

    private void initializeDropdownWallet() {
        binding.ddlWalletText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedWallet = walletDropdownAdapter.getItem(position);
                if(selectedWallet != null) {
                    binding.ddlWalletText.setText(selectedWallet.getName(), false);
                }
                validateAmount();
            }
        });

        binding.ddlWalletText.setAdapter(walletDropdownAdapter);
    }

    private void initializeValidationListener() {
        binding.amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateAmount();
            }
        });
    }

    private void validateAmount() {
        String amountText = binding.amountEditText.getText().toString();
        Long amount = null;

        try {
            amount = Long.parseLong(amountText);
        } catch (Exception ignored) {}

        if (selectedWallet == null || amount == null || amount <= 0) {
            return;
        }

        if(selectedType == TransactionType.EXPENSE && selectedWallet.getBalance() < amount) {
            binding.addButton.setEnabled(false);
            binding.errorTextView.setVisibility(View.VISIBLE);
        } else {
            binding.addButton.setEnabled(true);
            binding.errorTextView.setVisibility(View.GONE);
        }
    }

    private void back() {
        NavController navController = Navigation.findNavController(requireView());
        navController.popBackStack();
    }
}
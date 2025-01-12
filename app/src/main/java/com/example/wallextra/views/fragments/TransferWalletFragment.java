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
import com.example.wallextra.databinding.FragmentTransferWalletBinding;
import com.example.wallextra.models.Wallet;
import com.example.wallextra.viewmodels.WalletTransferViewModel;
import com.example.wallextra.viewmodels.WalletViewModel;
import com.example.wallextra.views.adapters.WalletDropdownAdapter;

import java.util.ArrayList;

public class TransferWalletFragment extends Fragment {
    private FragmentTransferWalletBinding binding;
    private WalletViewModel walletViewModel;
    private WalletTransferViewModel walletTransferViewModel;
    private ArrayList<Wallet> wallets = new ArrayList<>();
    private WalletDropdownAdapter sourceWalletDropdownAdapter;
    private WalletDropdownAdapter destWalletDropdownAdapter;
    private Wallet selectedSourceWallet = null;
    private Wallet selectedDestWallet = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTransferWalletBinding.inflate(inflater, container, false);
        walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);
        walletTransferViewModel = new ViewModelProvider(requireActivity()).get(WalletTransferViewModel.class);
        sourceWalletDropdownAdapter = new WalletDropdownAdapter(requireContext(), wallets);
        destWalletDropdownAdapter = new WalletDropdownAdapter(requireContext(), wallets);
        walletViewModel.fetchUserWallets();

        initializeDropdownWallet();
        initializeValidationListener();

        binding.closeIcon.setOnClickListener(e -> back());
        binding.transferButton.setOnClickListener(e -> doTransfer());

        walletViewModel.getFetchWalletState().observe(getViewLifecycleOwner(), response -> {
            if(response.isSuccess()) {
                wallets.clear();
                wallets.addAll(response.getData());
                sourceWalletDropdownAdapter.notifyDataSetChanged();
                destWalletDropdownAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        walletTransferViewModel.getAddWalletTransferState().observe(getViewLifecycleOwner(), response -> {
            if(response == null) return;

            if(response.isSuccess()) {
                back();
            } else {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        
        return binding.getRoot();
    }

    private void doTransfer() {
        String amountString = binding.amountEditText.getText().toString();
        String adminFeeString = binding.adminFeeEditText.getText().toString();
        Long amount = null;
        Long adminFee = null;

        try {
            amount = Long.parseLong(amountString);
        } catch(Exception ignored) {}

        try {
            adminFee = Long.parseLong(adminFeeString);
        } catch(Exception ignored) {}

        if(amount == null || selectedSourceWallet == null || selectedDestWallet == null) {
            Toast.makeText(getContext(), "All fields except admin fee must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        walletTransferViewModel.addWalletTransfer(selectedSourceWallet, selectedDestWallet, amount, adminFee);
    }

    private void initializeDropdownWallet() {
        binding.ddlFromWalletText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedSourceWallet = sourceWalletDropdownAdapter.getItem(position);
                if(selectedSourceWallet == null) return;

                binding.ddlFromWalletText.setText(selectedSourceWallet.getName(), false);
                if(selectedSourceWallet.equals(selectedDestWallet)) {
                    selectedDestWallet = null;
                    binding.ddlToWalletText.setText(null, false);
                }
                validateAmount();
            }
        });

        binding.ddlToWalletText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedDestWallet = destWalletDropdownAdapter.getItem(position);
                if(selectedDestWallet == null) return;

                binding.ddlToWalletText.setText(selectedDestWallet.getName(), false);
                if(selectedDestWallet.equals(selectedSourceWallet)) {
                    selectedSourceWallet = null;
                    binding.ddlFromWalletText.setText(null, false);
                }
            }
        });

        binding.ddlFromWalletText.setAdapter(sourceWalletDropdownAdapter);
        binding.ddlToWalletText.setAdapter(destWalletDropdownAdapter);
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

        binding.adminFeeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                validateAmount();
            }
        });
    }

    private void validateAmount() {
        String amountText = binding.amountEditText.getText().toString();
        String adminFeeText = binding.adminFeeEditText.getText().toString();
        Long amount = null;
        Long adminFee = null;

        try {
            amount = Long.parseLong(amountText);
        } catch (Exception ignored) {}

        try {
            adminFee = Long.parseLong(adminFeeText);
        } catch (Exception ignored) {}

        if (selectedSourceWallet == null || amount == null || amount <= 0) {
            return;
        }

        Long totalReducedAmount = amount + ((adminFee != null)? adminFee : 0);
        if(selectedSourceWallet.getBalance() < totalReducedAmount) {
            binding.transferButton.setEnabled(false);
            binding.errorTextView.setVisibility(View.VISIBLE);
        } else {
            binding.transferButton.setEnabled(true);
            binding.errorTextView.setVisibility(View.GONE);
        }
    }

    private void back() {
        NavController navController = Navigation.findNavController(requireView());
        navController.popBackStack();
    }
}
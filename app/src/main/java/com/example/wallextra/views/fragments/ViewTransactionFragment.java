package com.example.wallextra.views.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.wallextra.R;
import com.example.wallextra.databinding.FragmentViewTransactionBinding;
import com.example.wallextra.models.BaseTransaction;
import com.example.wallextra.models.Month;
import com.example.wallextra.models.WalletTransfer;
import com.example.wallextra.utils.MutableBoolean;
import com.example.wallextra.models.Transaction;
import com.example.wallextra.viewmodels.TransactionViewModel;
import com.example.wallextra.viewmodels.WalletTransferViewModel;
import com.example.wallextra.views.adapters.ViewTransactionAdapter;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class ViewTransactionFragment extends Fragment {
    private TransactionViewModel transactionViewModel;
    private WalletTransferViewModel walletTransferViewModel;
    private FragmentViewTransactionBinding binding;
    private MutableBoolean isDeleting = new MutableBoolean(false);
    private ArrayList<BaseTransaction> baseTransactions = new ArrayList<>();
    private Month selectedMonth;
    private int selectedYear;
    private MutableLiveData<ArrayList<Transaction>> transactions = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<ArrayList<WalletTransfer>> walletTransfers = new MutableLiveData<>(new ArrayList<>());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewTransactionBinding.inflate(inflater, container, false);

        TabLayout tabLayout = binding.monthTabLayout;
        tabLayout.addTab(tabLayout.newTab().setText("January"));
        tabLayout.addTab(tabLayout.newTab().setText("February"));
        tabLayout.addTab(tabLayout.newTab().setText("March"));
        tabLayout.addTab(tabLayout.newTab().setText("April"));
        tabLayout.addTab(tabLayout.newTab().setText("May"));
        tabLayout.addTab(tabLayout.newTab().setText("June"));
        tabLayout.addTab(tabLayout.newTab().setText("July"));
        tabLayout.addTab(tabLayout.newTab().setText("August"));
        tabLayout.addTab(tabLayout.newTab().setText("September"));
        tabLayout.addTab(tabLayout.newTab().setText("October"));
        tabLayout.addTab(tabLayout.newTab().setText("November"));
        tabLayout.addTab(tabLayout.newTab().setText("December"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int index = tab.getPosition();
                selectedMonth = getMonth(index);
                transactionViewModel.fetchUserTransactionsByMonthAndYear(selectedMonth, selectedYear);
                walletTransferViewModel.fetchUserWalletTransfersByMonthAndYear(selectedMonth, selectedYear);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        walletTransferViewModel = new ViewModelProvider(requireActivity()).get(WalletTransferViewModel.class);
        LocalDateTime currentDateTime = LocalDateTime.now();
        selectedMonth = getMonth(currentDateTime.getMonth().getValue() - 1);
        selectedYear = currentDateTime.getYear();
        transactionViewModel.fetchUserTransactionsByMonthAndYear(selectedMonth, selectedYear);
        walletTransferViewModel.fetchUserWalletTransfersByMonthAndYear(selectedMonth, selectedYear);

        tabLayout.selectTab(tabLayout.getTabAt(currentDateTime.getMonth().getValue() - 1));
        initializeDropdown();

        ViewTransactionAdapter adapter = new ViewTransactionAdapter(
                baseTransactions,
                isDeleting,
                transactionId -> transactionViewModel.deleteTransaction(transactionId),
                walletTransferId -> walletTransferViewModel.deleteWalletTransfer(walletTransferId)
        );
        binding.baseTransactionRecyclerView.setAdapter(adapter);
        binding.baseTransactionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.baseTransactionRecyclerView.setHasFixedSize(true);

        if(isDeleting.isTrue()) {
            binding.deleteButton.setVisibility(View.GONE);
            binding.viewButton.setVisibility(View.VISIBLE);
        }

        binding.deleteButton.setOnClickListener(v -> {
            binding.deleteButton.setVisibility(View.GONE);
            binding.viewButton.setVisibility(View.VISIBLE);
            isDeleting.value = true;
            adapter.notifyDeletingToggled();
        });

        binding.viewButton.setOnClickListener(v -> {
            binding.viewButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);
            isDeleting.value = false;
            adapter.notifyDeletingToggled();
        });

        binding.addWalletTransferButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.transfer_wallet_fragment);
        });
        binding.addTransactionButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.add_transaction_fragment);
        });

        transactionViewModel.getFetchTransactionState().observe(getViewLifecycleOwner(), response -> {
            if(response.isSuccess()) {
                transactions.setValue(response.getData());
            } else {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Firebase error", response.getMessage());
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
            adapter.notifyDataSetChanged();
        });

        transactionViewModel.getAddTransactionState().observe(getViewLifecycleOwner(), response -> {
            if(response == null) return;

            if(response.isSuccess()) {
                transactionViewModel.fetchUserTransactionsByMonthAndYear(selectedMonth, selectedYear);
                transactionViewModel.resetAddTransactionState();
            }
        });

        walletTransferViewModel.getAddWalletTransferState().observe(getViewLifecycleOwner(), response -> {
            if(response == null) return;

            if(response.isSuccess()) {
                walletTransferViewModel.fetchUserWalletTransfersByMonthAndYear(selectedMonth, selectedYear);
                walletTransferViewModel.resetAddWalletTransferState();
            }
        });

        transactionViewModel.getDeleteTransactionState().observe(getViewLifecycleOwner(), response -> {
            if(response.isSuccess()) {
                transactionViewModel.fetchUserTransactionsByMonthAndYear(selectedMonth, selectedYear);
            } else {
                Toast.makeText(requireContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        walletTransferViewModel.getDeleteWalletTransferState().observe(getViewLifecycleOwner(), response -> {
            if(response.isSuccess()) {
                walletTransferViewModel.fetchUserWalletTransfersByMonthAndYear(selectedMonth, selectedYear);
            } else {
                Toast.makeText(requireContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    private void initializeDropdown() {
        ArrayList<String> cycles = new ArrayList<>();
        LocalDateTime currentDateTime = LocalDateTime.now();
        for (int i = 2024; i<=currentDateTime.getYear(); i++){
            cycles.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.list_item, cycles);

        binding.autoCompleteText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedYearString = adapter.getItem(position);
                selectedYear = Integer.parseInt(selectedYearString);
                transactionViewModel.fetchUserTransactionsByMonthAndYear(selectedMonth, selectedYear);
                walletTransferViewModel.fetchUserWalletTransfersByMonthAndYear(selectedMonth, selectedYear);
            }
        });

        binding.autoCompleteText.setText(String.valueOf(selectedYear));

        binding.autoCompleteText.setAdapter(adapter);
    }

    private Month getMonth (int position) {
        switch (position){
            case 0:
                return Month.JANUARY;
            case 1:
                return Month.FEBRUARY;
            case 2:
                return Month.MARCH;
            case 3:
                return Month.APRIL;
            case 4:
                return Month.MAY;
            case 5:
                return Month.JUNE;
            case 6:
                return Month.JULY;
            case 7:
                return Month.AUGUST;
            case 8:
                return Month.SEPTEMBER;
            case 9:
                return Month.OCTOBER;
            case 10:
                return Month.NOVEMBER;
            case 11:
                return Month.DECEMBER;
        }
        return null;
    }
}
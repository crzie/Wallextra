package com.example.wallextra.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.wallextra.R;
import com.example.wallextra.databinding.FragmentViewTransactionBinding;
import com.example.wallextra.databinding.FragmentViewWalletBinding;
import com.example.wallextra.models.Month;
import com.example.wallextra.models.MutableBoolean;
import com.example.wallextra.models.Transaction;
import com.example.wallextra.models.Wallet;
import com.example.wallextra.viewmodels.TransactionViewModel;
import com.example.wallextra.viewmodels.WalletViewModel;
import com.example.wallextra.views.adapters.ViewWalletAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class ViewTransactionFragment extends Fragment {
    private TransactionViewModel transactionViewModel;
    private FragmentViewTransactionBinding binding;
    private MutableBoolean isDeleting = new MutableBoolean(false);
    private ArrayList<Transaction> transactions = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewTransactionBinding.inflate(inflater, container, false);

        final Month[] months = {Month.JANUARY};
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
                months[0] = getMonth(index);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        //Masih hardcoded year e
        transactionViewModel.fetchUserTransactionsByMonthAndYear(months[0],2022);

//        ViewAdapter adapter = new ViewWalletAdapter(wallets, isDeleting);
//        binding.walletRecyclerView.setAdapter(adapter);
        binding.walletRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.walletRecyclerView.setHasFixedSize(true);

        binding.deleteButton.setOnClickListener(v -> {
            binding.deleteButton.setVisibility(View.GONE);
            binding.viewButton.setVisibility(View.VISIBLE);
            isDeleting.value = true;
//            adapter.notifyDataSetChanged();
        });

        binding.viewButton.setOnClickListener(v -> {
            binding.viewButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);
            isDeleting.value = false;
//            adapter.notifyDataSetChanged();
        });

        binding.addTransactionButton.setOnClickListener(v -> {
            // pindah fragment
        });

//        transactions.add(new Transaction("wl", "bca", 10000L, "lol", "asd"));
//        adapter.notifyDataSetChanged();
        transactionViewModel.getFetchTransactionState().observe(getViewLifecycleOwner(), response -> {
            if(response.isSuccess()) {
                transactions = response.getData();
//                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



        return binding.getRoot();
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
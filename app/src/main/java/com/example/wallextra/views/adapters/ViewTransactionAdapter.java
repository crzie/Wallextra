package com.example.wallextra.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallextra.databinding.ItemLayoutViewTransactionBinding;
import com.example.wallextra.databinding.ItemLayoutViewWalletBinding;
import com.example.wallextra.models.MutableBoolean;
import com.example.wallextra.models.Transaction;
import com.example.wallextra.models.Wallet;

import java.util.ArrayList;
import java.util.List;

public class ViewTransactionAdapter extends RecyclerView.Adapter<ViewTransactionAdapter.ViewHolder> {
    private final ArrayList<Transaction> transactions;
    private final MutableBoolean isDeleting;

    public ViewTransactionAdapter(ArrayList<Transaction> transactions, MutableBoolean isDeleting) {
        this.transactions = transactions;
        this.isDeleting = isDeleting;
    }

    @NonNull
    @Override
    public ViewTransactionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLayoutViewTransactionBinding binding = ItemLayoutViewTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewTransactionAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewTransactionAdapter.ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction, isDeleting);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewTransactionAdapter.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            boolean isDeleting = (boolean) payloads.get(0);
            holder.updateTrashButtonVisibility(isDeleting);
        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemLayoutViewTransactionBinding binding;
        public ViewHolder(ItemLayoutViewTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Transaction transaction, MutableBoolean isDeleting) {
            binding.setTransaction(transaction);
            updateTrashButtonVisibility(isDeleting.value);
        }

        public void updateTrashButtonVisibility(boolean isDeleting) {
            binding.trashButton.setVisibility(isDeleting ? View.VISIBLE : View.GONE);
        }
    }
}

package com.example.wallextra.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallextra.R;
import com.example.wallextra.databinding.ItemLayoutViewWalletBinding;
import com.example.wallextra.models.MutableBoolean;
import com.example.wallextra.models.Wallet;

import java.util.ArrayList;

public class ViewWalletAdapter extends RecyclerView.Adapter<ViewWalletAdapter.ViewHolder>{
    private final ArrayList<Wallet> wallets;
    private final MutableBoolean isDeleting;

    public ViewWalletAdapter(ArrayList<Wallet> wallets, MutableBoolean isDeleting) {
        this.wallets = wallets;
        this.isDeleting = isDeleting;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLayoutViewWalletBinding binding = ItemLayoutViewWalletBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wallet wallet = wallets.get(position);
        holder.bind(wallet, isDeleting);
    }

    @Override
    public int getItemCount() {
        return wallets.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemLayoutViewWalletBinding binding;
        public ViewHolder(ItemLayoutViewWalletBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Wallet wallet, MutableBoolean isDeleting) {
            binding.setWallet(wallet);
            if(isDeleting.value) {
                binding.trashButton.setVisibility(View.VISIBLE);
            } else {
                binding.trashButton.setVisibility(View.GONE);
            }
            // TODO: render image
        }
    }
}

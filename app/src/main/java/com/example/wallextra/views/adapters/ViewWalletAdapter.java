package com.example.wallextra.views.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wallextra.R;
import com.example.wallextra.databinding.ItemLayoutViewWalletBinding;
import com.example.wallextra.models.MutableBoolean;
import com.example.wallextra.models.Wallet;

import java.util.ArrayList;
import java.util.List;

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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            boolean isDeleting = (boolean) payloads.get(0);
            holder.updateTrashButtonVisibility(isDeleting);
        } else {
            onBindViewHolder(holder, position);
        }
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

    public void notifyDeletingToggled() {
        notifyItemRangeChanged(0, getItemCount(), isDeleting.value);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemLayoutViewWalletBinding binding;
        public ViewHolder(ItemLayoutViewWalletBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Wallet wallet, MutableBoolean isDeleting) {
            binding.setWallet(wallet);
            updateTrashButtonVisibility(isDeleting.value);

            if(wallet.getImageUrl() != null) {
                Glide.with(binding.walletImage.getContext())
                        .load(wallet.getImageUrl())
                        .into(binding.walletImage);

                binding.walletImagePlaceholder.setVisibility(View.GONE);
                binding.walletImage.setVisibility(View.VISIBLE);
            }
        }

        public void updateTrashButtonVisibility(boolean isDeleting) {
            binding.trashButton.setVisibility(isDeleting ? View.VISIBLE : View.GONE);
        }
    }
}

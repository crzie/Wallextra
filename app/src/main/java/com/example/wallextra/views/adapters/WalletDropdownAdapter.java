package com.example.wallextra.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.wallextra.databinding.ItemLayoutWalletDropdownBinding;
import com.example.wallextra.models.Wallet;

import java.util.List;

public class WalletDropdownAdapter extends ArrayAdapter<Wallet> {

    public WalletDropdownAdapter(@NonNull Context context, @NonNull List<Wallet> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ItemLayoutWalletDropdownBinding binding;

        if (convertView == null) {
            binding = ItemLayoutWalletDropdownBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (ItemLayoutWalletDropdownBinding) convertView.getTag();
        }

        Wallet wallet = getItem(position);
        binding.setWallet(wallet);

        if(wallet != null && wallet.getImageUrl() != null) {
            Glide.with(binding.walletImage.getContext())
                    .load(wallet.getImageUrl())
                    .into(binding.walletImage);

            binding.walletImagePlaceholder.setVisibility(View.GONE);
            binding.walletImage.setVisibility(View.VISIBLE);
        } else {
            binding.walletImagePlaceholder.setVisibility(View.VISIBLE);
            binding.walletImage.setVisibility(View.GONE);
        }

        return convertView;
    }
}

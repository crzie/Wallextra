package com.example.wallextra.views.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresExtension;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.ext.SdkExtensions;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.wallextra.databinding.FragmentAddWalletBinding;
import com.example.wallextra.viewmodels.WalletViewModel;

public class AddWalletFragment extends Fragment {
    private WalletViewModel walletViewModel;
    private FragmentAddWalletBinding binding;
    private ActivityResultLauncher<Intent> resultLauncher;
    private Uri uploadedImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddWalletBinding.inflate(inflater, container, false);
        walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
            initializeImagePicker();
        }

        binding.closeButton.setOnClickListener(v -> {
            back();
        });

        binding.addButton.setOnClickListener(v -> {
            submitWallet();
        });

        walletViewModel.getAddWalletState().observe(getViewLifecycleOwner(), response -> {
            if(response.isSuccess()) {
                back();
            } else {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    private void submitWallet() {
        String name = binding.walletNameEditText.getText().toString();
        String balanceText = binding.initialBalanceEditText.getText().toString();
        Long balance = null;

        try {
            balance = Long.parseLong(balanceText);
        } catch(Exception ignored) {}

        if(name.isBlank() || balance == null) {
            Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        if(uploadedImage == null) {
            walletViewModel.addWallet(name, balance);
        } else {
            walletViewModel.addWallet(getContext(), name, balance, uploadedImage);
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    private void initializeImagePicker() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        if(result.getData() != null) {
                            binding.walletImagePlaceholder.setVisibility(View.GONE);
                            binding.walletImageContainer.setVisibility(View.VISIBLE);

                            Uri imageUri = result.getData().getData();
                            binding.walletImage.setImageURI(imageUri);
                            uploadedImage = imageUri;
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error picking image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        binding.walletImageInput.setOnClickListener(v -> {
            resultLauncher.launch(new Intent(MediaStore.ACTION_PICK_IMAGES));
        });
    }

    private void back() {
        NavController navController = Navigation.findNavController(requireView());
        navController.popBackStack();
    }
}
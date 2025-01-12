package com.example.wallextra.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wallextra.R;
import com.example.wallextra.databinding.ItemLayoutViewTransactionBinding;
import com.example.wallextra.databinding.ItemLayoutViewWalletTransferBinding;
import com.example.wallextra.models.BaseTransaction;
import com.example.wallextra.models.TransactionType;
import com.example.wallextra.models.WalletTransfer;
import com.example.wallextra.utils.ItemClickListener;
import com.example.wallextra.utils.MutableBoolean;
import com.example.wallextra.models.Transaction;

import java.util.ArrayList;
import java.util.List;

public class ViewTransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TRANSACTION = 1;
    private static final int VIEW_TYPE_WALLET_TRANSFER = 2;

    private final ArrayList<BaseTransaction> baseTransactions;
    private final MutableBoolean isDeleting;
    private final ItemClickListener deleteTransactionClickListener;
    private final ItemClickListener deleteWalletTransferClickListener;

    public ViewTransactionAdapter(ArrayList<BaseTransaction> baseTransactions, MutableBoolean isDeleting, ItemClickListener deleteTransactionClickListener, ItemClickListener deleteWalletTransferClickListener) {
        this.baseTransactions = baseTransactions;
        this.isDeleting = isDeleting;
        this.deleteTransactionClickListener = deleteTransactionClickListener;
        this.deleteWalletTransferClickListener = deleteWalletTransferClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        BaseTransaction baseTransaction = baseTransactions.get(position);
        if (baseTransaction instanceof Transaction) {
            return VIEW_TYPE_TRANSACTION;
        } else if (baseTransaction instanceof WalletTransfer) {
            return VIEW_TYPE_WALLET_TRANSFER;
        }
        throw new IllegalArgumentException("Unknown transaction type");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_TRANSACTION) {
            ItemLayoutViewTransactionBinding binding =
                    ItemLayoutViewTransactionBinding.inflate(inflater, parent, false);
            return new TransactionViewHolder(binding);
        } else if (viewType == VIEW_TYPE_WALLET_TRANSFER) {
            ItemLayoutViewWalletTransferBinding binding =
                    ItemLayoutViewWalletTransferBinding.inflate(inflater, parent, false);
            return new WalletTransferViewHolder(binding);
        }
        throw new IllegalArgumentException("Unknown view type");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseTransaction baseTransaction = baseTransactions.get(position);
        if (holder instanceof TransactionViewHolder) {
            ((TransactionViewHolder) holder).bind((Transaction) baseTransaction, isDeleting);
        } else if (holder instanceof WalletTransferViewHolder) {
            ((WalletTransferViewHolder) holder).bind((WalletTransfer) baseTransaction, isDeleting);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            boolean isDeleting = (boolean) payloads.get(0);
            if (holder instanceof TransactionViewHolder) {
                ((TransactionViewHolder) holder).updateTrashButtonVisibility(isDeleting);
            } else if (holder instanceof WalletTransferViewHolder) {
                ((WalletTransferViewHolder) holder).updateTrashButtonVisibility(isDeleting);
            }
        } else {
            onBindViewHolder(holder, position);
        }
    }

    public void notifyDeletingToggled() {
        notifyItemRangeChanged(0, getItemCount(), isDeleting.value);
    }

    @Override
    public int getItemCount() {
        return baseTransactions.size();
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ItemLayoutViewTransactionBinding binding;

        public TransactionViewHolder(ItemLayoutViewTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Transaction transaction, MutableBoolean isDeleting) {
            binding.setTransaction(transaction);
            updateTrashButtonVisibility(isDeleting.value);

            if(transaction.getWallet().getImageUrl() != null) {
                Glide.with(binding.walletImage.getContext())
                        .load(transaction.getWallet().getImageUrl())
                        .into(binding.walletImage);

                binding.walletImagePlaceholder.setVisibility(View.GONE);
                binding.walletImage.setVisibility(View.VISIBLE);
            }

            if(transaction.getType().equals(TransactionType.EXPENSE)) {
                binding.transactionAmount.setText("-Rp" + transaction.getAmount());
                binding.transactionAmount.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.red));
                binding.descriptionTitle.setText(transaction.getWallet().getName() + " -> -Rp" + transaction.getAmount());
            } else {
                binding.transactionAmount.setText("+Rp" + transaction.getAmount());
                binding.transactionAmount.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.green_light));
                binding.descriptionTitle.setText(transaction.getWallet().getName() + " -> +Rp" + transaction.getAmount());
            }

            binding.trashButton.setOnClickListener(v -> {
                deleteTransactionClickListener.handle(transaction.getId());
            });

            binding.mainContainer.setOnClickListener(v -> {
                if(binding.descriptionContainer.getVisibility() == View.GONE) {
                    binding.descriptionContainer.setVisibility(View.VISIBLE);
                } else {
                    binding.descriptionContainer.setVisibility(View.GONE);
                }
            });
        }

        public void updateTrashButtonVisibility(boolean isDeleting) {
            binding.trashButton.setVisibility(isDeleting ? View.VISIBLE : View.GONE);
        }
    }

    public class WalletTransferViewHolder extends RecyclerView.ViewHolder {
        private final ItemLayoutViewWalletTransferBinding binding;

        public WalletTransferViewHolder(ItemLayoutViewWalletTransferBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(WalletTransfer walletTransfer, MutableBoolean isDeleting) {
            binding.setWalletTransfer(walletTransfer);
            updateTrashButtonVisibility(isDeleting.value);

            if(walletTransfer.getSourceWallet().getImageUrl() != null) {
                Glide.with(binding.sourceWalletImage.getContext())
                        .load(walletTransfer.getSourceWallet().getImageUrl())
                        .into(binding.sourceWalletImage);

                binding.sourceWalletImagePlaceholder.setVisibility(View.GONE);
                binding.sourceWalletImage.setVisibility(View.VISIBLE);
            }
            if(walletTransfer.getDestWallet().getImageUrl() != null) {
                Glide.with(binding.destWalletImage.getContext())
                        .load(walletTransfer.getDestWallet().getImageUrl())
                        .into(binding.destWalletImage);

                binding.destWalletImagePlaceholder.setVisibility(View.GONE);
                binding.destWalletImage.setVisibility(View.VISIBLE);
            }

            binding.trashButton.setOnClickListener(v -> {
                deleteWalletTransferClickListener.handle(walletTransfer.getId());
            });

            binding.mainContainer.setOnClickListener(v -> {
                if(binding.descriptionContainer.getVisibility() == View.GONE) {
                    binding.descriptionContainer.setVisibility(View.VISIBLE);
                } else {
                    binding.descriptionContainer.setVisibility(View.GONE);
                }
            });
        }

        public void updateTrashButtonVisibility(boolean isDeleting) {
            binding.trashButton.setVisibility(isDeleting ? View.VISIBLE : View.GONE);
        }
    }
}

package com.example.wallextra.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wallextra.models.Month;
import com.example.wallextra.models.TransactionType;
import com.example.wallextra.models.Wallet;
import com.example.wallextra.models.WalletTransfer;
import com.example.wallextra.utils.Response;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WalletTransferViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private final MutableLiveData<Response<Void>> addWalletTransferState = new MutableLiveData<>();
    private final MutableLiveData<Response<ArrayList<WalletTransfer>>> fetchWalletTransferState = new MutableLiveData<>();
    private final MutableLiveData<Response<Void>> deleteWalletTransferState = new MutableLiveData<>();

    public void addWalletTransfer(Wallet sourceWallet, Wallet destWallet, Long amount, Long adminFee) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            addWalletTransferState.setValue(Response.error("Unauthorized"));
            return;
        }

        String userId = currentUser.getUid();
        Date transactionDate = new Date();

        db.runTransaction(transaction -> {
            // create admin fee transaction (if valid)
            // change wallets balances
            // add wallet transfer
            DocumentReference sourceWalletRef = db.collection("wallets").document(sourceWallet.getId());
            DocumentReference destWalletRef = db.collection("wallets").document(destWallet.getId());
            DocumentSnapshot sourceWalletSnapshot = transaction.get(sourceWalletRef);
            DocumentSnapshot destWalletSnapshot = transaction.get(destWalletRef);

            if (!sourceWalletSnapshot.exists() || !destWalletSnapshot.exists()) {
                throw new IllegalStateException("Wallet not found");
            }

            if (!sourceWallet.getOwnerId().equals(userId) || !destWallet.getOwnerId().equals(userId)) {
                throw new IllegalStateException("Unauthorized");
            }

            Long sourceCurrentBalance = sourceWalletSnapshot.getLong("balance");
            Long destCurrentBalance = destWalletSnapshot.getLong("balance");
            if (sourceCurrentBalance == null || destCurrentBalance == null) {
                throw new IllegalStateException("Invalid wallet balance");
            }

            // 0 won't be an error, it just won't show up as transaction
            if (adminFee != null && adminFee < 0) {
                throw new IllegalStateException("Invalid admin fee");
            }

            Long sourceNewBalance = sourceCurrentBalance - amount - (adminFee == null? 0 : adminFee);
            Long destNewBalance = destCurrentBalance + amount;
            if (sourceNewBalance < 0) {
                throw new IllegalStateException("Insufficient Balance");
            }

            String adminTransactionId = null;
            if (adminFee != null && adminFee > 0) {
                // create transaction if admin fee valid
                adminTransactionId = addAdminTransaction(
                        transaction,
                        sourceWallet.getName() + " admin fee",
                        TransactionType.EXPENSE,
                        adminFee,
                        sourceWallet.getId(),
                        transactionDate
                );
            }

            transaction.update(sourceWalletRef, "balance", sourceNewBalance);
            transaction.update(destWalletRef, "balance", destNewBalance);
            db.collection("walletTransfers").add(
                    prepareWalletTransferData(
                            sourceWallet,
                            destWallet,
                            amount,
                            userId,
                            adminTransactionId,
                            adminFee,
                            transactionDate
                    )
            );

            return null;
        }).addOnSuccessListener(aVoid -> {
            addWalletTransferState.setValue(Response.success("Add wallet transfer success", null));
        })
        .addOnFailureListener(e -> {
            addWalletTransferState.setValue(Response.error(e.getMessage()));
        });
    }

    private String addAdminTransaction(Transaction transaction, String name, TransactionType type, Long amount, String walletId, Date transactionDate) throws FirebaseFirestoreException {
        DocumentReference walletRef = db.collection("wallets").document(walletId);

        DocumentSnapshot walletSnapshot = transaction.get(walletRef);
        if(!walletSnapshot.exists()) {
            throw new IllegalStateException("Wallet not found");
        }

        Long currentBalance = walletSnapshot.getLong("balance");
        if (currentBalance == null) {
            throw new IllegalStateException("Invalid wallet balance");
        }

        Long newBalance = type == TransactionType.INCOME? currentBalance + amount : currentBalance - amount;
        if (type == TransactionType.EXPENSE && newBalance < 0) {
            throw new IllegalStateException("Insufficient Balance");
        }

        transaction.update(walletRef, "balance", newBalance);

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> walletData = new HashMap<>();
        data.put("name", name);
        data.put("type", type.toString());
        data.put("amount", amount);
        data.put("date", transactionDate);
        data.put("ownerId", walletSnapshot.getString("ownerId"));
        data.put("walletId", walletId);
        walletData.put("name", walletSnapshot.getString("name"));
        walletData.put("imageUrl", walletSnapshot.getString("imageUrl"));
        data.put("wallet", walletData);

        DocumentReference transactionRef = db.collection("transactions").document();
        transaction.set(transactionRef, data);

        return transactionRef.getId();
    }

    private Map<String, Object> prepareWalletTransferData(Wallet sourceWallet, Wallet destWallet, Long amount, String userId, String adminTransactionId, Long adminFee, Date transactionDate) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> sourceWalletData = new HashMap<>();
        Map<String, Object> destWalletData = new HashMap<>();

        sourceWalletData.put("name", sourceWallet.getName());
        sourceWalletData.put("imageUrl", sourceWallet.getImageUrl());
        destWalletData.put("name", destWallet.getName());
        destWalletData.put("imageUrl", destWallet.getImageUrl());

        data.put("sourceWalletId", sourceWallet.getId());
        data.put("sourceWallet", sourceWalletData);
        data.put("destWalletId", destWallet.getId());
        data.put("destWallet", destWalletData);
        data.put("amount", amount);
        data.put("ownerId", userId);
        data.put("date", transactionDate);

        if (adminTransactionId != null) {
            data.put("adminTransactionId", adminTransactionId);
            data.put("adminFee", adminFee);
        }

        return data;
    }

    public void fetchUserWalletTransfersByMonthAndYear(Month month, int year) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            fetchWalletTransferState.setValue(Response.error("Unauthorized"));
            return;
        }

        String userId = currentUser.getUid();
        LocalDateTime startOfMonth = LocalDateTime.of(year, month.getNumber(), 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        Date startDate = Date.from(startOfMonth.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endOfMonth.atZone(ZoneId.systemDefault()).toInstant());

        db.collection("walletTransfers")
                .whereEqualTo("ownerId", userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThan("date", endDate)
                .get()
                .addOnSuccessListener(this::getWalletTransfersFromSnapshot)
                .addOnFailureListener(e -> {
                    fetchWalletTransferState.setValue(Response.error(e.getMessage()));
                });
    }

    public void fetchRecentUserWalletTransfers(int number) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            fetchWalletTransferState.setValue(Response.error("Unauthorized"));
            return;
        }

        String userId = currentUser.getUid();
        db.collection("walletTransfers")
                .whereEqualTo("ownerId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(number)
                .get()
                .addOnSuccessListener(this::getWalletTransfersFromSnapshot)
                .addOnFailureListener(e -> {
                    fetchWalletTransferState.setValue(Response.error(e.getMessage()));
                });
    }

    private void getWalletTransfersFromSnapshot(QuerySnapshot snapshot) {
        ArrayList<WalletTransfer> walletTransfers = new ArrayList<>();

        snapshot.getDocuments().forEach(doc -> {
            Map<String, Object> sourceWalletData = (Map<String, Object>) doc.get("sourceWallet");
            Map<String, Object> destWalletData = (Map<String, Object>) doc.get("destWallet");
            Wallet sourceWallet = null;
            Wallet destWallet = null;

            if (sourceWalletData != null) {
                sourceWallet = new Wallet(
                        doc.getString("sourceWalletId"),
                        (String) sourceWalletData.get("name"),
                        null,
                        doc.getString("ownerId"),
                        (String) sourceWalletData.get("imageUrl")
                );
            }

            if (destWalletData != null) {
                destWallet = new Wallet(
                        doc.getString("destWalletId"),
                        (String) destWalletData.get("name"),
                        null,
                        doc.getString("ownerId"),
                        (String) destWalletData.get("imageUrl")
                );
            }

            WalletTransfer walletTransfer = new WalletTransfer(
                    doc.getId(),
                    sourceWallet,
                    destWallet,
                    doc.getLong("amount"),
                    doc.getString("adminTransactionId"),
                    doc.getDate("date")
            );

            walletTransfers.add(walletTransfer);
        });

        fetchWalletTransferState.setValue(Response.success("Fetch wallet transfers success", walletTransfers));
    }

    public void deleteWalletTransfer(String walletTransferId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            deleteWalletTransferState.setValue(Response.error("Unauthorized"));
            return;
        }

        String userId = currentUser.getUid();

        db.runTransaction(transaction -> {
            // delete admin fee transaction (if exists)
            // revert wallet changes
            // delete wallet transfer
            DocumentReference walletTransferRef = db.collection("walletTransfers").document(walletTransferId);

            DocumentSnapshot walletTransferSnapshot = transaction.get(walletTransferRef);
            if(!walletTransferSnapshot.exists()) {
                throw new IllegalStateException("Wallet transfer not found");
            }

            String adminTransactionId = walletTransferSnapshot.getString("adminTransactionId");
            Long amount = walletTransferSnapshot.getLong("amount");
            String sourceWalletId = walletTransferSnapshot.getString("sourceWalletId");
            String destWalletId = walletTransferSnapshot.getString("destWalletId");
            if (sourceWalletId == null || destWalletId == null || amount == null) {
                throw new IllegalStateException("Invalid wallet transfer fields");
            }

            // revert only if the wallets exist (not deleted)
            DocumentReference sourceWalletRef = db.collection("wallets").document(sourceWalletId);
            DocumentReference destWalletRef = db.collection("wallets").document(destWalletId);
            DocumentSnapshot sourceWalletSnapshot = transaction.get(sourceWalletRef);
            DocumentSnapshot destWalletSnapshot = transaction.get(destWalletRef);

            if (adminTransactionId != null) {
                deleteAdminTransaction(transaction, adminTransactionId);
            }

            if (sourceWalletSnapshot.exists()) {
                Long currentBalance = sourceWalletSnapshot.getLong("balance");
                if (currentBalance == null) {
                    throw new IllegalStateException("Invalid wallet balance");
                }
                Long newBalance = currentBalance + amount;
                transaction.update(sourceWalletRef, "balance", newBalance);
            }

            if (destWalletSnapshot.exists()) {
                Long currentBalance = destWalletSnapshot.getLong("balance");
                if (currentBalance == null) {
                    throw new IllegalStateException("Invalid wallet balance");
                }
                Long newBalance = currentBalance - amount;
                transaction.update(destWalletRef, "balance", newBalance);
            }
            transaction.delete(walletTransferRef);
            return null;
        }).addOnSuccessListener(aVoid -> {
            deleteWalletTransferState.setValue(Response.success("Delete wallet transfer success", null));
        })
        .addOnFailureListener(e -> {
            deleteWalletTransferState.setValue(Response.error(e.getMessage()));
        });
    }

    private void deleteAdminTransaction(Transaction transaction, String transactionId) throws FirebaseFirestoreException {
        DocumentReference transactionRef = db.collection("transactions").document(transactionId);

        DocumentSnapshot transactionSnapshot = transaction.get(transactionRef);
        if (!transactionSnapshot.exists()) {
            throw new IllegalStateException("Transaction not found");
        }

        String walletId = transactionSnapshot.getString("walletId");
        if (walletId == null) {
            throw new IllegalStateException("Invalid wallet");
        }

        DocumentReference walletRef = db.collection("wallets").document(walletId);
        DocumentSnapshot walletSnapshot = transaction.get(walletRef);
        if (walletSnapshot.exists()) {
            // undo balance reduce/increase
            Long amount = transactionSnapshot.getLong("amount");
            Long currentBalance = walletSnapshot.getLong("balance");
            String type = transactionSnapshot.getString("type");
            if (amount == null || currentBalance == null || type == null) {
                throw new IllegalStateException("Invalid wallet or transaction fields");
            }

            final long newBalance;
            if (type.equals(TransactionType.INCOME.toString())) {
                newBalance = currentBalance - amount;
            } else if (type.equals(TransactionType.EXPENSE.toString())) {
                newBalance = currentBalance + amount;
            } else {
                newBalance = currentBalance;
            }

            transaction.update(walletRef, "balance", newBalance);
        }

        transaction.delete(transactionRef);
    }

    public LiveData<Response<Void>> getAddWalletTransferState() {
        return addWalletTransferState;
    }

    public LiveData<Response<ArrayList<WalletTransfer>>> getFetchWalletTransferState() {
        return fetchWalletTransferState;
    }

    public LiveData<Response<Void>> getDeleteWalletTransferState() {
        return deleteWalletTransferState;
    }

    public void resetAddWalletTransferState() {
        addWalletTransferState.setValue(null);
    }
}

package com.example.wallextra.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wallextra.models.Month;
import com.example.wallextra.models.Transaction;
import com.example.wallextra.models.TransactionType;
import com.example.wallextra.models.Wallet;
import com.example.wallextra.utils.Response;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TransactionViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private final MutableLiveData<Response<Void>> addTransactionState = new MutableLiveData<>();
    private final MutableLiveData<Response<ArrayList<Transaction>>> fetchTransactionState = new MutableLiveData<>();
    private final MutableLiveData<Response<Void>> deleteTransactionState = new MutableLiveData<>();

    // TODO change to wallet object, not only id
    public void addTransaction(String name, TransactionType type, Long amount, String walletId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            addTransactionState.setValue(Response.error("Unauthorized"));
            return;
        }

        String userId = currentUser.getUid();

        db.runTransaction(transaction -> {
            // reduce/increase wallet amount, then add to transaction collection
            DocumentReference walletRef = db.collection("wallets").document(walletId);

            DocumentSnapshot walletSnapshot = transaction.get(walletRef);
            if(!walletSnapshot.exists()) {
                throw new IllegalStateException("Wallet not found");
            }

            String ownerId = walletSnapshot.getString("ownerId");
            if (!userId.equals(ownerId)) {
                throw new IllegalStateException("Unauthorized");
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
            data.put("date", new Date());
            data.put("ownerId", walletSnapshot.getString("ownerId"));
            data.put("walletId", walletId);
            walletData.put("name", walletSnapshot.getString("name"));
            walletData.put("imageUrl", walletSnapshot.getString("imageUrl"));
            data.put("wallet", walletData);

            db.collection("transactions").add(data).getResult().getId();
            return null;
        }).addOnSuccessListener(aVoid -> {
            addTransactionState.setValue(Response.success("Add transaction success", null));
        }).addOnFailureListener(e -> {
            addTransactionState.setValue(Response.error(e.getMessage()));
        });
    }

    public void fetchUserTransactionsByMonthAndYear(Month month, int year) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            fetchTransactionState.setValue(Response.error("Unauthorized"));
            return;
        }

        String userId = currentUser.getUid();
        LocalDateTime startOfMonth = LocalDateTime.of(year, month.getNumber(), 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        Date startDate = Date.from(startOfMonth.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endOfMonth.atZone(ZoneId.systemDefault()).toInstant());

        db.collection("transactions")
                .whereEqualTo("ownerId", userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThan("date", endDate)
                .get()
                .addOnSuccessListener(this::getTransactionsFromSnapshot)
                .addOnFailureListener(e -> {
                    fetchTransactionState.setValue(Response.error(e.getMessage()));
                });
    }

    public void fetchRecentUserTransactions(int number) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            fetchTransactionState.setValue(Response.error("Unauthorized"));
            return;
        }

        String userId = currentUser.getUid();
        db.collection("transactions")
                .whereEqualTo("ownerId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(number)
                .get()
                .addOnSuccessListener(this::getTransactionsFromSnapshot)
                .addOnFailureListener(e -> {
                    fetchTransactionState.setValue(Response.error(e.getMessage()));
                });
    }

    private void getTransactionsFromSnapshot(QuerySnapshot snapshot) {
        ArrayList<Transaction> transactions = new ArrayList<>();

        snapshot.getDocuments().forEach(doc -> {
            Map<String, Object> walletData = (Map<String, Object>) doc.get("wallet");
            Wallet wallet = null;

            if (walletData != null) {
                wallet = new Wallet(
                        doc.getString("walletId"),
                        (String) walletData.get("name"),
                        null,
                        doc.getString("ownerId"),
                        (String) walletData.get("imageUrl")
                );
            }

            Transaction transaction = new Transaction(
                    doc.getId(),
                    doc.getString("name"),
                    TransactionType.from(doc.getString("type")),
                    doc.getLong("amount"),
                    wallet,
                    doc.getDate("date")
            );

            transactions.add(transaction);
        });

        fetchTransactionState.setValue(Response.success("Fetch transactions success", transactions));
    }

    public void deleteTransaction(String transactionId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            deleteTransactionState.setValue(Response.error("Unauthorized"));
            return;
        }

        String userId = currentUser.getUid();

        db.runTransaction(transaction -> {
            // remove transaction and add/reduce the wallet
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
            return null;
        }).addOnSuccessListener(aVoid -> {
           deleteTransactionState.setValue(Response.success("Delete transaction success", null));
        })
        .addOnFailureListener(e -> {
           deleteTransactionState.setValue(Response.error(e.getMessage()));
        });
    }

    public LiveData<Response<Void>> getAddTransactionState() {
        return addTransactionState;
    }

    public LiveData<Response<ArrayList<Transaction>>> getFetchTransactionState() {
        return fetchTransactionState;
    }

    public LiveData<Response<Void>> getDeleteTransactionState() {
        return deleteTransactionState;
    }
}

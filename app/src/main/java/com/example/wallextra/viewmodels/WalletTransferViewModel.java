package com.example.wallextra.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wallextra.models.Month;
import com.example.wallextra.models.Transaction;
import com.example.wallextra.models.TransactionType;
import com.example.wallextra.models.Wallet;
import com.example.wallextra.models.WalletTransfer;
import com.example.wallextra.utils.Response;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WalletTransferViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private final MutableLiveData<Response<Void>> addWalletTransferState = new MutableLiveData<>();
    private final MutableLiveData<Response<ArrayList<WalletTransfer>>> fetchWalletTransferState = new MutableLiveData<>();
    private final MutableLiveData<Response<Void>> deleteWalletTransferState = new MutableLiveData<>();

    public void addWalletTransfer(String name, TransactionType type, Long amount, String walletId) {

    }

    public void fetchUserWalletTransfersByMonthAndYear(Month month, int year) {

    }

    public void fetchRecentUserWalletTransfers(int number) {

    }

    private void getWalletTransfersFromSnapshot(QuerySnapshot snapshot) {

    }

    public void deleteWalletTransfer(String walletTransferId) {

    }

    public LiveData<Response<Void>> getAddTransactionState() {
        return addWalletTransferState;
    }

    public LiveData<Response<ArrayList<WalletTransfer>>> getFetchTransactionState() {
        return fetchWalletTransferState;
    }

    public LiveData<Response<Void>> getDeleteTransactionState() {
        return deleteWalletTransferState;
    }
}

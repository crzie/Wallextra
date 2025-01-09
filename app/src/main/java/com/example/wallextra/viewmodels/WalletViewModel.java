package com.example.wallextra.viewmodels;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.example.wallextra.BuildConfig;
import com.example.wallextra.models.Wallet;
import com.example.wallextra.utils.Helper;
import com.example.wallextra.utils.Response;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WalletViewModel extends ViewModel {
    private static final String ACCESS_TOKEN = BuildConfig.DROPBOX_ACCESS_TOKEN;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final DbxRequestConfig config;
    private final DbxClientV2 client;

    private final MutableLiveData<Response<Void>> addWalletState = new MutableLiveData<>();
    private final MutableLiveData<Response<ArrayList<Wallet>>> fetchWalletState = new MutableLiveData<>();
    private final MutableLiveData<Response<Void>> deleteWalletState = new MutableLiveData<>();

    public WalletViewModel() {
        config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
        client = new DbxClientV2(config, ACCESS_TOKEN);
    }

    public void addWallet(String name, long balance) {
        saveWalletToDatabase(name, balance, null);
    }

    public void addWallet(Context context, String name, long balance, Uri image) {
        new Thread(() -> {
            try {
                String fileExtension = Helper.getFileExtension(context, image);
                String fileName = "wallet_images/" + UUID.randomUUID() + "." + fileExtension;

                InputStream inputStream = context.getContentResolver().openInputStream(image);

                client.files().uploadBuilder("/" + fileName)
                        .uploadAndFinish(inputStream);

                String sharedLink = client.sharing().createSharedLinkWithSettings("/" + fileName).getUrl();
                String directLink = sharedLink.replace("&dl=0", "&raw=1");

                saveWalletToDatabase(name, balance, directLink);
            } catch (Exception e) {
                addWalletState.postValue(Response.error(e.getMessage()));
            }
        }).start();
    }

    private void saveWalletToDatabase(String name, long balance, String imageUrl) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            addWalletState.setValue(Response.error("Unauthorized"));
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("balance", balance);
        data.put("ownerId", currentUser.getUid());
//        data.put("isDeleted", false);
        if(imageUrl != null) {
            data.put("imageUrl", imageUrl);
        }

        db.collection("wallets")
                .add(data)
                .addOnSuccessListener(ref -> {
                    addWalletState.setValue(Response.success("Add wallet success", null));
                })
                .addOnFailureListener(e -> {
                    addWalletState.setValue(Response.error(e.getMessage()));
                });
    }

    public void fetchUserWallets() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            fetchWalletState.setValue(Response.error("Unauthorized"));
            return;
        }

        String userId = currentUser.getUid();
        db.collection("wallets")
                .whereEqualTo("ownerId", userId)
//                .whereEqualTo("isDeleted", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    ArrayList<Wallet> wallets = new ArrayList<>();
                    snapshot.getDocuments().forEach(doc -> {
                        Wallet wallet = new Wallet(
                                doc.getId(),
                                doc.getString("name"),
                                doc.getLong("balance"),
                                doc.getString("ownerId"),
                                doc.getString("imageUrl")
                        );
                        wallets.add(wallet);
                    });

                    fetchWalletState.setValue(Response.success("Fetch wallet success", wallets));
                })
                .addOnFailureListener(e -> {
                    fetchWalletState.setValue(Response.error(e.getMessage()));
                });
    }

    public void deleteWallet(String walletId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            deleteWalletState.setValue(Response.error("Unauthorized"));
            return;
        }

        String userId = currentUser.getUid();

        db.runTransaction(transaction -> {
            DocumentReference walletRef = db.collection("wallets").document(walletId);

            DocumentSnapshot walletSnapshot = transaction.get(walletRef);
            if (!walletSnapshot.exists()) {
                throw new IllegalStateException("Wallet not found");
            }

            // validate ownership
            String ownerId = walletSnapshot.getString("ownerId");
            if (!userId.equals(ownerId)) {
                throw new IllegalStateException("Unauthorized");
            }

            transaction.delete(walletRef);
//            transaction.update(walletRef, "isDeleted", true);
            return null;
        }).addOnSuccessListener(aVoid -> {
            deleteWalletState.setValue(Response.success("Delete wallet success", null));
        }).addOnFailureListener(e -> {
            deleteWalletState.setValue(Response.error(e.getMessage()));
        });
    }

    public LiveData<Response<Void>> getAddWalletState() { return addWalletState; }
    public LiveData<Response<ArrayList<Wallet>>> getFetchWalletState() {
        return fetchWalletState;
    }
    public LiveData<Response<Void>> getDeleteWalletState() { return deleteWalletState; }
    public void resetAddWalletState() {
        addWalletState.setValue(null);
    }
}

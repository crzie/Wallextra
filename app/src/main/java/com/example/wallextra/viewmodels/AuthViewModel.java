package com.example.wallextra.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wallextra.utils.Response;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;

public class AuthViewModel extends ViewModel {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private final MutableLiveData<Response<Void>> loginState = new MutableLiveData<>();
    private final MutableLiveData<Response<Void>> registerState = new MutableLiveData<>();

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
    public void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    loginState.setValue(Response.success("Login success", null));
                })
                .addOnFailureListener(e -> {
                    loginState.setValue(Response.error(e.getMessage()));
                });
    }

    public void register(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    // set the registered username
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();

                    Objects.requireNonNull(mAuth.getCurrentUser())
                            .updateProfile(profileUpdates)
                            .addOnSuccessListener(result1 -> {
                                registerState.setValue(Response.success("Register success", null));
                            })
                            .addOnFailureListener(e -> {
                                registerState.setValue(Response.error(e.getMessage()));
                            });
                })
                .addOnFailureListener(e -> {
                    registerState.setValue(Response.error(e.getMessage()));
                });
    }

    public void logout() {
        mAuth.signOut();
    }

    public LiveData<Response<Void>> getLoginState() {
        return loginState;
    }
    public LiveData<Response<Void>> getRegisterState() { return registerState; }

}

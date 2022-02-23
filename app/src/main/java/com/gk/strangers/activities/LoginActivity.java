package com.gk.strangers.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gk.strangers.R;
import com.gk.strangers.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    GoogleSignInClient googleSignInClient;
    int RC_SIGN_IN = 11;
    FirebaseAuth firebaseAuth;
    ActivityResultLauncher<Intent> launcher;
    FirebaseDatabase database;
    Task<GoogleSignInAccount> task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            goToNextActivity();
        }

        database = FirebaseDatabase.getInstance();

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, signInOptions);


        findViewById(R.id.loginBtn).setOnClickListener(view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);

            // launcher.launch(intent);

            // startActivity(new Intent(LoginActivity.this, MainActivity.class));
        });

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result != null) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getResultCode() == RC_SIGN_IN) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        GoogleSignInAccount account = task.getResult();
                        authWithGoogle(account.getIdToken());
                    }
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    public void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            updateUI(null);
            Log.d("error", task.getException().getLocalizedMessage());
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            Log.d("name", account.getDisplayName());
            Log.d("profile", String.valueOf(account.getPhotoUrl()));
            Log.d("profile id", account.getId().toString());

            User firebaseUser = new User(
                    account.getId(), account.getDisplayName(),String.valueOf(account.getPhotoUrl()), "Unknown", 500);
            database.getReference()
                    .child("profiles")
                    .child(Objects.requireNonNull(account.getId()))
                    .setValue(firebaseUser).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("task is successful", account.getDisplayName());
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finishAffinity();
                } else {
                    Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void goToNextActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    void authWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        assert user != null;
                        Log.d("user profile uri", user.getPhotoUrl().toString());
                        Log.d("user email", user.getEmail());
                    }
                });
    }
}
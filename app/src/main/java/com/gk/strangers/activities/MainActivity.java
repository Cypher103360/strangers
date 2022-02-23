package com.gk.strangers.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.gk.strangers.databinding.ActivityMainBinding;
import com.gk.strangers.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseDatabase database;
    long coins = 0;
    GoogleSignInAccount account;
    User user;
    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private int requestCode = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();
        account = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseApp.initializeApp(this);

        database.getReference().child("profiles")
                .child(account.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        coins = user.getCoins();
                        binding.coins.setText("You have: " + coins);
                        Glide.with(MainActivity.this).load(user.getProfile()).into(binding.profilePicture);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.findBtn.setOnClickListener(view -> {
            if (isPermissionsGranted()) {
                if (coins > 5) {
                    Intent intent = new Intent(MainActivity.this, ConnectingActivity.class);
                    intent.putExtra("profile", user.getProfile());
                    startActivity(intent);
                    //  startActivity(new Intent(MainActivity.this, ConnectingActivity.class));
                } else {
                    Toast.makeText(this, "Insufficient Coins", Toast.LENGTH_SHORT).show();
                }
            } else {
                askPermissions();
            }
        });

    }

    void askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean isPermissionsGranted() {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }
}
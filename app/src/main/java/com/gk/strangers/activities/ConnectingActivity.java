package com.gk.strangers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.gk.strangers.databinding.ActivityConnectingBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ConnectingActivity extends AppCompatActivity {
    ActivityConnectingBinding binding;
    FirebaseDatabase database;
    GoogleSignInAccount account;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        account = GoogleSignIn.getLastSignedInAccount(this);

        database = FirebaseDatabase.getInstance();

        String profile = getIntent().getStringExtra("profile");
        Glide.with(this).load(profile).into(binding.profile);

        String username = account.getId();

        database.getReference().child("users")
                .orderByChild("status")
                .equalTo(0).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getChildrenCount() > 0) {
                            // Room available
                            for (DataSnapshot childSnap: snapshot.getChildren()){
                                database.getReference()
                                        .child("users")
                                        .child(childSnap.getKey())
                                        .child("incoming")
                                        .setValue(username);

                                database.getReference()
                                        .child("users")
                                        .child(childSnap.getKey())
                                        .child("status")
                                        .setValue(1);

                                Intent intent = new Intent(ConnectingActivity.this, CallActivity.class);

                                String incoming = snapshot.child("incoming").getValue(String.class);
                                String createdBy = snapshot.child("createdBy").getValue(String.class);
                                boolean isAvailable = snapshot.child("isAvailable").getValue(Boolean.class);

                                intent.putExtra("username",username);
                                intent.putExtra("incoming",incoming);
                                intent.putExtra("createdBy",createdBy);
                                intent.putExtra("isAvailable",isAvailable);

                                startActivity(intent);
                            }
                        }else {
                            // Not available

                            HashMap<String,Object> room = new HashMap<>();
                            room.put("incoming",username);
                            room.put("createdBy",username);
                            room.put("isAvailable",true);
                            room.put("status",0);

                            database.getReference()
                                    .child("users")
                                    .child(username)
                                    .setValue(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.getReference()
                                            .child("users")
                                            .child(username).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.child("status").exists()){
                                                if (snapshot.child("status").getValue(Integer.class)==1){
                                                    Intent intent = new Intent(ConnectingActivity.this, CallActivity.class);

                                                    String incoming = snapshot.child("incoming").getValue(String.class);
                                                    String createdBy = snapshot.child("createdBy").getValue(String.class);
                                                    boolean isAvailable = snapshot.child("isAvailable").getValue(Boolean.class);

                                                    intent.putExtra("username",username);
                                                    intent.putExtra("incoming",incoming);
                                                    intent.putExtra("createdBy",createdBy);
                                                    intent.putExtra("isAvailable",isAvailable);

                                                    startActivity(intent);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}
package com.gk.strangers.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gk.strangers.R;
import com.gk.strangers.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class WelcomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.getStarted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeActivity.this,LoginActivity.class));
                finish();
            }
        });
    }
    public void goToNextActivity(){
        startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
        finish();
    }


    private void updateUI(GoogleSignInAccount account) {
        if (account!=null){
            findViewById(R.id.getStarted).setVisibility(View.GONE);
            goToNextActivity();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }
}
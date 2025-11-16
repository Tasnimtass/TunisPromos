package com.example.tunispromos;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();

            // CHANGEMENT : Utiliser Realtime Database
            database.child("users").child(uid).get()
                    .addOnSuccessListener(dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            String role = dataSnapshot.child("role").getValue(String.class);
                            if ("provider".equals(role)) {
                                startActivity(new Intent(MainActivity.this, ProviderActivity.class));
                            } else {
                                startActivity(new Intent(MainActivity.this, UserActivity.class));
                            }
                            finish();
                        } else {
                            auth.signOut();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    });
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }
}
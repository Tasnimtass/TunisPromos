package com.example.tunispromos;

import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser Firebase

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Vérifier si l'utilisateur est déjà connecté
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            // Utilisateur connecté - récupérer son rôle et rediriger
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String role = doc.getString("role");
                            if ("provider".equals(role)) {
                                startActivity(new Intent(MainActivity.this, ProviderActivity.class));
                            } else {
                                startActivity(new Intent(MainActivity.this, UserActivity.class));
                            }
                            finish();
                        } else {
                            // Document utilisateur n'existe pas - déconnecter et aller au login
                            auth.signOut();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Erreur lors de la récupération - aller au login
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    });
        } else {
            // Pas d'utilisateur connecté - aller au login
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }


    }
}
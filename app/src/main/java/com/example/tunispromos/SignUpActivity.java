package com.example.tunispromos;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {
    private EditText editName, editEmail, editPassword;
    private Spinner spinnerRole;
    private Button btnSignUp;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editName = findViewById(R.id.editTextName);
        editEmail = findViewById(R.id.editTextEmail);
        editPassword = findViewById(R.id.editTextPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnSignUp = findViewById(R.id.buttonSignUp);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnSignUp.setOnClickListener(v -> signUpUser());

    }
    private void signUpUser() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        // Validation améliorée
        if(name.isEmpty() || email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifier la longueur du mot de passe
        if(password.length() < 6){
            Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_LONG).show();
            return;
        }

        // Vérifier le format de l'email
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Format d'email invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Désactiver le bouton pendant le traitement
        btnSignUp.setEnabled(false);
        btnSignUp.setText("Inscription en cours...");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        String uid = auth.getCurrentUser().getUid();
                        User user = new User(uid, name, email, role);

                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Erreur Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    btnSignUp.setEnabled(true);
                                    btnSignUp.setText("S'inscrire");
                                });
                    } else {
                        // Afficher le message d'erreur détaillé
                        String errorMessage = "Erreur d'authentification";
                        if(task.getException() != null){
                            errorMessage = task.getException().getMessage();
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        btnSignUp.setEnabled(true);
                        btnSignUp.setText("S'inscrire");
                    }
                });
    }
}
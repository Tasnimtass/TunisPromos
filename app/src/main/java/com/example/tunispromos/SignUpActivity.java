package com.example.tunispromos;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText editName, editEmail, editPassword;
    private Spinner spinnerRole;
    private Button btnSignUp;

    private FirebaseAuth auth;
    private DatabaseReference database;

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

        // ⚡ Utiliser l'URL complète de ta Realtime Database
        database = FirebaseDatabase.getInstance(
                "https://tunispromos-default-rtdb.europe-west1.firebasedatabase.app"
        ).getReference("users");

        btnSignUp.setOnClickListener(v -> signUpUser());
    }

    private void signUpUser() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        if(name.isEmpty() || email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if(password.length() < 6){
            Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_LONG).show();
            return;
        }

        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Format d'email invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSignUp.setEnabled(false);
        btnSignUp.setText("Inscription en cours...");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        String uid = auth.getCurrentUser().getUid();
                        User user = new User(uid, name, email, role);

                        database.child(uid).setValue(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Inscription réussie ! Connectez-vous maintenant.", Toast.LENGTH_LONG).show();
                                    auth.signOut();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Erreur Database: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    btnSignUp.setEnabled(true);
                                    btnSignUp.setText("S'inscrire");
                                });
                    } else {
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

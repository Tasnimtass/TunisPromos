package com.example.tunispromos;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnLogin, btnGoSignUp;
    private TextView textForgotPassword;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editTextEmail);
        editPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        btnGoSignUp = findViewById(R.id.buttonGoSignUp);
        textForgotPassword = findViewById(R.id.textForgotPassword);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());

        btnGoSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });

        textForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loginUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Connexion...");

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        String uid = auth.getCurrentUser().getUid();
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(doc -> {
                                    String role = doc.getString("role");
                                    if("provider".equals(role)){
                                        startActivity(new Intent(this, ProviderActivity.class));
                                    } else {
                                        startActivity(new Intent(this, UserActivity.class));
                                    }
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Erreur de récupération du profil",
                                            Toast.LENGTH_SHORT).show();
                                    btnLogin.setEnabled(true);
                                    btnLogin.setText("Se connecter");
                                });
                    } else {
                        Toast.makeText(this, "Email ou mot de passe incorrect",
                                Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Se connecter");
                    }
                });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Réinitialiser le mot de passe");

        final EditText input = new EditText(this);
        input.setHint("Entrez votre email");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Envoyer", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if(email.isEmpty()){
                Toast.makeText(this, "Veuillez entrer votre email",
                        Toast.LENGTH_SHORT).show();
            } else {
                resetPassword(email);
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void resetPassword(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Email de réinitialisation envoyé. Vérifiez votre boîte mail.",
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Erreur: vérifiez que l'email est correct",
                            Toast.LENGTH_SHORT).show();
                });
    }
}
package com.example.tunispromos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AddPromotionActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTitle, editDescription, editPriceBefore, editPriceAfter, editCategory, editStartDate, editEndDate;
    private Button btnSelectImage, btnSave;
    private ImageView imagePreview;

    private Uri imageUri;
    private String promoId;
    private FirebaseAuth auth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion);

        // Initialiser les vues
        editTitle = findViewById(R.id.editTitle);
        editDescription = findViewById(R.id.editDescription);
        editPriceBefore = findViewById(R.id.editPriceBefore);
        editPriceAfter = findViewById(R.id.editPriceAfter);
        editCategory = findViewById(R.id.editCategory);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);
        btnSelectImage = findViewById(R.id.buttonSelectImage);
        btnSave = findViewById(R.id.buttonSave);
        imagePreview = findViewById(R.id.imagePreview);

        // Initialiser Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(
                "https://tunispromos-default-rtdb.europe-west1.firebasedatabase.app"
        ).getReference();

        // Vérifier si on édite une promo existante
        promoId = getIntent().getStringExtra("promoId");
        if(promoId != null){
            loadPromotion(promoId);
        }

        // Demande des permissions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 1);
        } else {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        // Sélection d'image
        btnSelectImage.setOnClickListener(v -> selectImage());

        // Sauvegarde de la promotion
        btnSave.setOnClickListener(v -> savePromotion());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();

            // Garde la permission pour Android 10+
            getContentResolver().takePersistableUriPermission(
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            // AFFICHAGE DIRECT AVEC GLIDE (ne plus utiliser BitmapFactory ici)
            Glide.with(this)
                    .load(imageUri)
                    .into(imagePreview);

            imagePreview.setVisibility(ImageView.VISIBLE);
        }
    }


    private void savePromotion() {
        String title = editTitle.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String priceBeforeStr = editPriceBefore.getText().toString().trim();
        String priceAfterStr = editPriceAfter.getText().toString().trim();
        String category = editCategory.getText().toString().trim();
        String startDate = editStartDate.getText().toString().trim();
        String endDate = editEndDate.getText().toString().trim();

        if(auth.getCurrentUser() == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        String providerId = auth.getCurrentUser().getUid();

        // Validation des champs
        if(title.isEmpty() || description.isEmpty() || priceBeforeStr.isEmpty() ||
                priceAfterStr.isEmpty() || category.isEmpty() || startDate.isEmpty() || endDate.isEmpty()){
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation des prix
        double priceBefore, priceAfter;
        try {
            priceBefore = Double.parseDouble(priceBeforeStr);
            priceAfter = Double.parseDouble(priceAfterStr);
            if(priceBefore <= 0 || priceAfter <= 0) {
                Toast.makeText(this, "Les prix doivent être positifs", Toast.LENGTH_SHORT).show();
                return;
            }
            if(priceAfter >= priceBefore) {
                Toast.makeText(this, "Le prix promotionnel doit être inférieur au prix initial", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Format de prix invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation de l'image
        if (imageUri == null && promoId == null) {
            Toast.makeText(this, "Veuillez sélectionner une image", Toast.LENGTH_SHORT).show();
            return;
        }
        if(endDate.compareTo(startDate) <= 0 ){
            Toast.makeText(this, "La date de fin doit être après la date de début", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Enregistrement...");

        // Conversion en Base64 si nouvelle image
        String imageBase64 = null;
        if(imageUri != null){
            imageBase64 = convertImageToBase64(imageUri);
            if(imageBase64 == null){
                Toast.makeText(this, "Erreur conversion image", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
                btnSave.setText("Enregistrer");
                return;
            }
        }

        saveToDatabase(title, description, priceBefore, priceAfter, category, startDate, endDate, providerId, imageBase64);
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            inputStream.close();
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void saveToDatabase(String title, String description, double priceBefore, double priceAfter,
                                String category, String startDate, String endDate, String providerId, String imageBase64){
        if(promoId == null){
            promoId = database.child("promotions").push().getKey();
        }

        Map<String, Object> promo = new HashMap<>();
        promo.put("id", promoId);
        promo.put("title", title);
        promo.put("description", description);
        promo.put("priceBefore", priceBefore);
        promo.put("priceAfter", priceAfter);
        promo.put("category", category);
        promo.put("startDate", startDate);
        promo.put("endDate", endDate);
        promo.put("providerId", providerId);
        promo.put("imageBase64", imageBase64);

        database.child("promotions").child(promoId).setValue(promo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Promotion enregistrée !", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Enregistrer");
                });
    }

    private void loadPromotion(String promoId){
        database.child("promotions").child(promoId).get().addOnSuccessListener(dataSnapshot -> {
            if(dataSnapshot.exists()){
                editTitle.setText(dataSnapshot.child("title").getValue(String.class));
                editDescription.setText(dataSnapshot.child("description").getValue(String.class));
                Double priceBefore = dataSnapshot.child("priceBefore").getValue(Double.class);
                Double priceAfter = dataSnapshot.child("priceAfter").getValue(Double.class);
                if(priceBefore != null) editPriceBefore.setText(String.valueOf(priceBefore));
                if(priceAfter != null) editPriceAfter.setText(String.valueOf(priceAfter));
                editCategory.setText(dataSnapshot.child("category").getValue(String.class));
                editStartDate.setText(dataSnapshot.child("startDate").getValue(String.class));
                editEndDate.setText(dataSnapshot.child("endDate").getValue(String.class));

                String imageBase64 = dataSnapshot.child("imageBase64").getValue(String.class);
                if(imageBase64 != null && !imageBase64.isEmpty()){
                    byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                    Glide.with(this)
                            .asBitmap()
                            .load(decodedBytes)
                            .into(imagePreview);
                    imagePreview.setVisibility(ImageView.VISIBLE);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Erreur chargement promotion", Toast.LENGTH_SHORT).show();
        });
    }
}

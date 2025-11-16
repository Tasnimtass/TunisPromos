package com.example.tunispromos;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddPromotionActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "AddPromotionActivity";

    private EditText editTitle, editDescription, editPriceBefore, editPriceAfter, editCategory, editStartDate, editEndDate;
    private Button btnSelectImage, btnSave;
    private ImageView imagePreview;

    private Uri imageUri;
    private String promoId;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion);

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

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();

        promoId = getIntent().getStringExtra("promoId");
        if(promoId != null){
            loadPromotion(promoId);
        }

        btnSelectImage.setOnClickListener(v -> selectImage());
        btnSave.setOnClickListener(v -> savePromotion());
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
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
        String providerId = auth.getCurrentUser().getUid();

        if(title.isEmpty() || description.isEmpty() || priceBeforeStr.isEmpty() ||
                priceAfterStr.isEmpty() || category.isEmpty() || startDate.isEmpty() || endDate.isEmpty()){
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

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

        btnSave.setEnabled(false);
        btnSave.setText("Enregistrement...");

        if (imageUri != null) {
            String extension = getFileExtension(imageUri); // récupère jpg, png, etc.
            if (extension == null) extension = "jpg"; // fallback si pas détectée

            StorageReference ref = FirebaseStorage.getInstance()
                    .getReference()
                    .child("promo_images/" + UUID.randomUUID().toString() + "." + extension);

            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                // sauvegarder imageUrl avec promotion dans Realtime Database
                                Toast.makeText(this, "Image uploadée avec succès !", Toast.LENGTH_SHORT).show();
                            })
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erreur upload : " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "Veuillez sélectionner une image", Toast.LENGTH_SHORT).show();
        }


    }

    private void saveToDatabase(String title, String description, double priceBefore, double priceAfter,
                                String category, String startDate, String endDate, String providerId, String imageUrl){
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
        promo.put("imageUrl", imageUrl);

        database.child("promotions").child(promoId).setValue(promo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Promotion enregistrée !", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    handleUploadError(e);
                });
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void handleUploadError(Exception e){
        Log.e(TAG, "Erreur upload", e);
        Toast.makeText(this, e != null ? e.getMessage() : "Erreur upload", Toast.LENGTH_LONG).show();
        btnSave.setEnabled(true);
        btnSave.setText("Enregistrer");
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

                String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                if(imageUrl != null && !imageUrl.isEmpty()){
                    Glide.with(this).load(imageUrl).into(imagePreview);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Erreur chargement promotion", Toast.LENGTH_SHORT).show();
        });
    }
}

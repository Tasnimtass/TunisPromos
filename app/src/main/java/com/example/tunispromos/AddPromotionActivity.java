package com.example.tunispromos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddPromotionActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTitle, editDescription, editPriceBefore, editPriceAfter, editCategory, editStartDate, editEndDate;
    private Button btnSelectImage, btnSave;
    private ImageView imagePreview;

    private Uri imageUri;
    private String promoId; // pour modifier
    private FirebaseAuth auth;
    private FirebaseFirestore db;
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
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        promoId = getIntent().getStringExtra("promoId");
        if(promoId != null){
            loadPromotion(promoId); // pour modifier
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
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
            imagePreview.setVisibility(View.VISIBLE);
        }
    }

    private void savePromotion() {
        String title = editTitle.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String priceBefore = editPriceBefore.getText().toString().trim();
        String priceAfter = editPriceAfter.getText().toString().trim();
        String category = editCategory.getText().toString().trim();
        String startDate = editStartDate.getText().toString().trim();
        String endDate = editEndDate.getText().toString().trim();
        String providerId = auth.getCurrentUser().getUid();

        if(title.isEmpty() || description.isEmpty() || priceBefore.isEmpty() || priceAfter.isEmpty() || category.isEmpty() || startDate.isEmpty() || endDate.isEmpty()){
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if(imageUri != null){
            StorageReference ref = storage.getReference().child("promo_images/" + UUID.randomUUID().toString());
            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveToFirestore(title, description, priceBefore, priceAfter, category, startDate, endDate, providerId, imageUrl);
                    }));
        } else {
            saveToFirestore(title, description, priceBefore, priceAfter, category, startDate, endDate, providerId, null);
        }
    }

    private void saveToFirestore(String title, String description, String priceBefore, String priceAfter, String category, String startDate, String endDate, String providerId, String imageUrl){
        Map<String, Object> promo = new HashMap<>();
        if(promoId != null) promo.put("id", promoId); else promoId = db.collection("promotions").document().getId();
        promo.put("title", title);
        promo.put("description", description);
        promo.put("priceBefore", priceBefore);
        promo.put("priceAfter", priceAfter);
        promo.put("category", category);
        promo.put("startDate", startDate);
        promo.put("endDate", endDate);
        promo.put("providerId", providerId);
        promo.put("imageUrl", imageUrl);

        db.collection("promotions").document(promoId)
                .set(promo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Promotion enregistrée !", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur Firestore", Toast.LENGTH_SHORT).show());
    }

    private void loadPromotion(String promoId){
        db.collection("promotions").document(promoId).get()
                .addOnSuccessListener(doc -> {
                    if(doc.exists()){
                        editTitle.setText(doc.getString("title"));
                        editDescription.setText(doc.getString("description"));
                        editPriceBefore.setText(doc.getString("priceBefore"));
                        editPriceAfter.setText(doc.getString("priceAfter"));
                        editCategory.setText(doc.getString("category"));
                        editStartDate.setText(doc.getString("startDate"));
                        editEndDate.setText(doc.getString("endDate"));
                        String imageUrl = doc.getString("imageUrl");
                        if(imageUrl != null){
                            imagePreview.setVisibility(View.VISIBLE);
                            Glide.with(this).load(imageUrl).into(imagePreview);
                        }
                    }
                });
    }
}
package com.example.tunispromos;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class PromotionDetailActivity extends AppCompatActivity {

    private ImageView imagePromo;
    private TextView textTitle, textDescription, textPriceBefore, textPriceAfter,
            textCategory, textDates, textDiscount;

    private FirebaseFirestore db;
    private String promoId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_detail);

        // Activer le bouton retour
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Détails de la promotion");
        }
        imagePromo = findViewById(R.id.imagePromoDetail);
        textTitle = findViewById(R.id.textTitleDetail);
        textDescription = findViewById(R.id.textDescriptionDetail);
        textPriceBefore = findViewById(R.id.textPriceBeforeDetail);
        textPriceAfter = findViewById(R.id.textPriceAfterDetail);
        textCategory = findViewById(R.id.textCategoryDetail);
        textDates = findViewById(R.id.textDatesDetail);
        textDiscount = findViewById(R.id.textDiscountDetail);

        db = FirebaseFirestore.getInstance();

        // Récupérer l'ID de la promotion
        promoId = getIntent().getStringExtra("promoId");

        if (promoId != null) {
            loadPromotionDetails();
        }
    }
    private void loadPromotionDetails() {
        db.collection("promotions").document(promoId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String title = doc.getString("title");
                        String description = doc.getString("description");
                        String priceBefore = doc.getString("priceBefore");
                        String priceAfter = doc.getString("priceAfter");
                        String category = doc.getString("category");
                        String startDate = doc.getString("startDate");
                        String endDate = doc.getString("endDate");
                        String imageUrl = doc.getString("imageUrl");

                        // Affichage des données
                        textTitle.setText(title);
                        textDescription.setText(description);
                        textPriceBefore.setText(priceBefore + " DT");
                        textPriceAfter.setText(priceAfter + " DT");
                        textCategory.setText("Catégorie: " + category);
                        textDates.setText("Valide du " + startDate + " au " + endDate);

                        // Calculer le pourcentage de réduction
                        try {
                            double before = Double.parseDouble(priceBefore);
                            double after = Double.parseDouble(priceAfter);
                            double discount = ((before - after) / before) * 100;
                            textDiscount.setText("-" + String.format("%.0f", discount) + "%");
                        } catch (NumberFormatException e) {
                            textDiscount.setVisibility(View.GONE);
                        }

                        // Charger l'image
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .into(imagePromo);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Gérer l'erreur
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
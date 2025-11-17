package com.example.tunispromos;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PromotionDetailActivity extends AppCompatActivity {

    private static final String TAG = "PromotionDetail";

    private ImageView imagePromo;
    private TextView textTitle, textDescription, textPriceBefore, textPriceAfter,
            textCategory, textDates, textDiscount;

    private DatabaseReference database;
    private String promoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_detail);

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

        database = FirebaseDatabase.getInstance(
                "https://tunispromos-default-rtdb.europe-west1.firebasedatabase.app"
        ).getReference();

        promoId = getIntent().getStringExtra("promoId");

        if (promoId != null) {
            loadPromotionDetails();
        } else {
            Toast.makeText(this, "Erreur: ID promotion manquant", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // CHANGEMENT : Utiliser Realtime Database
    private void loadPromotionDetails() {
        database.child("promotions").child(promoId).get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        String title = dataSnapshot.child("title").getValue(String.class);
                        String description = dataSnapshot.child("description").getValue(String.class);
                        String category = dataSnapshot.child("category").getValue(String.class);
                        String startDate = dataSnapshot.child("startDate").getValue(String.class);
                        String endDate = dataSnapshot.child("endDate").getValue(String.class);
                        String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                        Double priceBefore = dataSnapshot.child("priceBefore").getValue(Double.class);
                        Double priceAfter = dataSnapshot.child("priceAfter").getValue(Double.class);

                        textTitle.setText(title);
                        textDescription.setText(description);
                        textCategory.setText("Catégorie: " + category);
                        textDates.setText("Valide du " + startDate + " au " + endDate);

                        if(priceBefore != null && priceAfter != null) {
                            textPriceBefore.setText(String.format("%.2f DT", priceBefore));
                            textPriceAfter.setText(String.format("%.2f DT", priceAfter));

                            double discount = ((priceBefore - priceAfter) / priceBefore) * 100;
                            textDiscount.setText("-" + String.format("%.0f", discount) + "%");
                        } else {
                            Log.e(TAG, "Prix manquants dans le document");
                            textPriceBefore.setText("N/A");
                            textPriceAfter.setText("N/A");
                            textDiscount.setVisibility(View.GONE);
                        }

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_background)
                                    .into(imagePromo);
                        }
                    } else {
                        Log.e(TAG, "Document n'existe pas");
                        Toast.makeText(this, "Promotion introuvable", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur chargement promotion", e);
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
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
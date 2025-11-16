package com.example.tunispromos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProviderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnAddPromo;
    private List<Promotion> promotionList;
    private ProviderPromotionAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);

        recyclerView = findViewById(R.id.recyclerProviderPromos);
        btnAddPromo = findViewById(R.id.buttonAddPromo);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        promotionList = new ArrayList<>();
        adapter = new ProviderPromotionAdapter(promotionList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadProviderPromotions();

        btnAddPromo.setOnClickListener(v -> {
            // Lancer une activité pour créer une promotion
            startActivity(new Intent(this, AddPromotionActivity.class));
        });
    }

    private void loadProviderPromotions() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("promotions")
                .whereEqualTo("providerId", uid)
                .addSnapshotListener((value, error) -> {
                    if(value != null){
                        promotionList.clear();
                        for(DocumentSnapshot doc : value.getDocuments()){
                            Promotion p = doc.toObject(Promotion.class);
                            promotionList.add(p);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

    }
}
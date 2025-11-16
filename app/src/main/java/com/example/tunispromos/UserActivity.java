package com.example.tunispromos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText editSearch;
    private List<Promotion> promotionList;
    private PromotionAdapter adapter;

    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        recyclerView = findViewById(R.id.recyclerPromotions);
        editSearch = findViewById(R.id.editSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        promotionList = new ArrayList<>();
        adapter = new PromotionAdapter(promotionList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadPromotions();

        // Filtrage simple par mot-clé
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadPromotions() {
        db.collection("promotions")
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
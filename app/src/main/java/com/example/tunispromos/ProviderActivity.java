package com.example.tunispromos;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class ProviderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnAddPromo;
    private List<Promotion> promotionList;
    private ProviderPromotionAdapter adapter;

    private DatabaseReference database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mes Promotions");
        }

        recyclerView = findViewById(R.id.recyclerProviderPromos);
        btnAddPromo = findViewById(R.id.buttonAddPromo);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        promotionList = new ArrayList<>();
        adapter = new ProviderPromotionAdapter(promotionList);
        recyclerView.setAdapter(adapter);

        database = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        loadProviderPromotions();

        btnAddPromo.setOnClickListener(v -> {
            startActivity(new Intent(this, AddPromotionActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProviderPromotions();
    }

    // CHANGEMENT : Utiliser Realtime Database avec Query
    private void loadProviderPromotions() {
        String uid = auth.getCurrentUser().getUid();

        Query query = database.child("promotions").orderByChild("providerId").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                promotionList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Promotion p = snapshot.getValue(Promotion.class);
                    if (p != null) {
                        promotionList.add(p);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProviderActivity.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Se déconnecter");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            auth.signOut();
            Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
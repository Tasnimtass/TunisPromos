package com.example.tunispromos;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ProviderPromotionAdapter extends RecyclerView.Adapter<ProviderPromotionAdapter.PromoViewHolder> {

    private List<Promotion> promotions;
    private DatabaseReference database;

    public ProviderPromotionAdapter(List<Promotion> promotions) {
        this.promotions = promotions;
        this.database = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_provider_promo, parent, false);
        return new PromoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
        Promotion p = promotions.get(position);
        holder.textTitle.setText(p.getTitle());
        holder.textDescription.setText(p.getDescription());
        holder.textPrice.setText(String.format("%.2f DT", p.getPriceAfter()));

        holder.buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddPromotionActivity.class);
            intent.putExtra("promoId", p.getId());
            v.getContext().startActivity(intent);
        });

        holder.buttonDelete.setOnClickListener(v -> {
            // CHANGEMENT : Utiliser Realtime Database
            database.child("promotions").child(p.getId()).removeValue()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(v.getContext(), "Supprimé", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(v.getContext(), "Erreur de suppression", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return promotions.size();
    }

    static class PromoViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDescription, textPrice;
        Button buttonEdit, buttonDelete;

        public PromoViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            textPrice = itemView.findViewById(R.id.textPrice);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
package com.example.tunispromos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromoViewHolder> {

    private List<Promotion> promotions;
    private List<Promotion> promotionsFull; // pour filtrage
    private Context context; // CORRECTION : Ajouter le context

    public PromotionAdapter(List<Promotion> promotions, Context context) {
        this.promotions = promotions;
        this.promotionsFull = new ArrayList<>(promotions);
        this.context = context; // CORRECTION
    }

    @NonNull
    @Override
    public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion, parent, false);
        return new PromoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
        Promotion p = promotions.get(position);
        holder.textTitle.setText(p.getTitle());
        holder.textDescription.setText(p.getDescription());
        holder.textPrice.setText(p.getPriceAfter() + " DT");
        holder.textPriceBefore.setText(p.getPriceBefore() + " DT");

        // CORRECTION : Charger l'image avec Glide
        if(p.getImageUrl() != null && !p.getImageUrl().isEmpty()){
            Glide.with(context)
                    .load(p.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imagePromo);
        }

        // CORRECTION : Rendre les cartes cliquables
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PromotionDetailActivity.class);
            intent.putExtra("promoId", p.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return promotions.size();
    }

    public void filter(String text) {
        promotions.clear();
        if(text.isEmpty()){
            promotions.addAll(promotionsFull);
        } else {
            text = text.toLowerCase();
            for(Promotion p : promotionsFull){
                if(p.getTitle().toLowerCase().contains(text) ||
                        p.getDescription().toLowerCase().contains(text)){
                    promotions.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class PromoViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDescription, textPrice, textPriceBefore;
        ImageView imagePromo; // CORRECTION : Ajouter l'ImageView

        public PromoViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            textPrice = itemView.findViewById(R.id.textPrice);
            textPriceBefore = itemView.findViewById(R.id.textPriceBefore);
            imagePromo = itemView.findViewById(R.id.imagePromo); // CORRECTION
        }
    }
}
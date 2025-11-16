package com.example.tunispromos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromoViewHolder> {

    private List<Promotion> promotions;
    private List<Promotion> promotionsFull; // pour filtrage

    public PromotionAdapter(List<Promotion> promotions) {
        this.promotions = promotions;
        promotionsFull = new ArrayList<>(promotions);
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
                if(p.getTitle().toLowerCase().contains(text) || p.getDescription().toLowerCase().contains(text)){
                    promotions.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class PromoViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDescription, textPrice;
        public PromoViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            textPrice = itemView.findViewById(R.id.textPrice);
        }
    }
}


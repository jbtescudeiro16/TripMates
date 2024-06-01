package com.example.carride;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private List<String> tripList;

    public TripAdapter(List<String> tripList) {
        this.tripList = tripList;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        String tripDetails = tripList.get(position);
        holder.tripTextView.setText(tripDetails);

        holder.tripTextView.setTypeface(holder.tripTextView.getTypeface(), Typeface.BOLD);

        // Definir cores de fundo diferentes com base na posição do item
        if (position % 2 == 0) {
            // Para os ímpares, manter a cor de fundo padrão
            holder.itemView.setBackgroundColor(Color.parseColor("#800a7560")); // Cor verde com transparência
        } else {
            // Para os pares, definir uma cor de fundo mais escura com a mesma transparência
            holder.itemView.setBackgroundColor(Color.parseColor("#400a7560")); // Cor verde mais escura com a mesma transparência
        }
    }


    @Override
    public int getItemCount() {
        return tripList.size();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tripTextView;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tripTextView = itemView.findViewById(R.id.text_trip);
        }
    }
}

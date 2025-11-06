package com.example.interfazprueba;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;
import java.util.Map;

public class PokemonAdapterImproved extends RecyclerView.Adapter<PokemonAdapterImproved.PokemonViewHolder> {

    private Context context;
    private List<PokemonFull> pokemonList;
    private Map<String, Integer> typeColors;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(PokemonFull pokemon);
    }

    public PokemonAdapterImproved(Context context, List<PokemonFull> pokemonList, Map<String, Integer> typeColors) {
        this.context = context;
        this.pokemonList = pokemonList;
        this.typeColors = typeColors;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public PokemonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pokemon_improved, parent, false);
        return new PokemonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokemonViewHolder holder, int position) {
        PokemonFull pokemon = pokemonList.get(position);

        // Configurar número
        holder.textNumber.setText("#" + String.format("%03d", pokemon.getNumber()));

        // Configurar nombre
        holder.textName.setText(pokemon.getName());

        // Configurar imagen
        if (pokemon.getImageUrl() != null && !pokemon.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(pokemon.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.imagePokemon);
        } else {
            holder.imagePokemon.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Configurar tipos
        holder.typesContainer.removeAllViews();
        if (pokemon.getTypes() != null && !pokemon.getTypes().isEmpty()) {
            for (String type : pokemon.getTypes()) {
                TextView typeView = new TextView(context);
                typeView.setText(type.toUpperCase());
                typeView.setTextColor(Color.WHITE);
                typeView.setTextSize(10f);
                typeView.setPadding(16, 8, 16, 8);
                typeView.setTypeface(null, android.graphics.Typeface.BOLD);

                // Crear fondo redondeado con color del tipo
                GradientDrawable typeBackground = new GradientDrawable();
                typeBackground.setShape(GradientDrawable.RECTANGLE);
                typeBackground.setCornerRadius(12f);
                Integer typeColor = typeColors.get(type.toLowerCase());
                typeBackground.setColor(typeColor != null ? typeColor : Color.LTGRAY);

                typeView.setBackground(typeBackground);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 8, 0);
                typeView.setLayoutParams(params);

                holder.typesContainer.addView(typeView);
            }
        }

        // Configurar click listener en toda la tarjeta
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(pokemon);
            }
        });

        // Agregar animación
        holder.itemView.setAlpha(0f);
        holder.itemView.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(position * 30L)
                .start();
    }

    @Override
    public int getItemCount() {
        return pokemonList.size();
    }

    public static class PokemonViewHolder extends RecyclerView.ViewHolder {
        TextView textNumber;
        TextView textName;
        ImageView imagePokemon;
        LinearLayout typesContainer;

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);
            textNumber = itemView.findViewById(R.id.textNumber);
            textName = itemView.findViewById(R.id.textName);
            imagePokemon = itemView.findViewById(R.id.imagePokemon);
            typesContainer = itemView.findViewById(R.id.types_container);
        }
    }
}
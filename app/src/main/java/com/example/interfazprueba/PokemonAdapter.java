package com.example.interfazprueba;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.ViewHolder> {

    private Context context;
    private List<PokemonFull> pokemonFullList;

    public PokemonAdapter(Context context, List<PokemonFull> pokemonFullList) {
        this.context = context;
        this.pokemonFullList = pokemonFullList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pokemon, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PokemonFull p = pokemonFullList.get(position);
        holder.textNumber.setText("#" + p.getNumber());
        holder.textName.setText(p.getName());
        holder.textType.setText(p.getTypeString());

        Glide.with(context)
                .load(p.getImageUrl())
                .into(holder.imagePokemon);

        // 👇 Agregar este bloque para manejar el click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pokemonFullList.size();
    }

    // Método para actualizar la lista (por búsqueda o filtro)
    public void updateList(List<PokemonFull> newList) {
        this.pokemonFullList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNumber, textName, textType;
        ImageView imagePokemon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNumber = itemView.findViewById(R.id.textNumber);
            textName = itemView.findViewById(R.id.textName);
            textType = itemView.findViewById(R.id.textType);
            imagePokemon = itemView.findViewById(R.id.imagePokemon);
        }
    }
    // Dentro de la clase PokemonAdapter
    public interface OnItemClickListener {
        void onItemClick(PokemonFull pokemon);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}

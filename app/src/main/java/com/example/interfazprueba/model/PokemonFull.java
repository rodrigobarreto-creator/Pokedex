package com.example.interfazprueba.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokemonFull implements Serializable {

    private int number;
    private String name;
    private String description;
    private double height;
    private double weight;
    private List<String> types;
    private List<Ability> abilities;
    private List<Move> moves;
    private Stats stats;
    private List<String> evolutions;
    private List<Form> megaForms;

    private String imageUrl;
    private String spriteFront;
    private String spriteBack;
    private String shinyFront;
    private String shinyBack;

    // ======== Getters y Setters ========
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public List<String> getTypes() { return types; }
    public void setTypes(List<String> types) { this.types = types; }

    public List<Ability> getAbilities() { return abilities; }
    public void setAbilities(List<Ability> abilities) { this.abilities = abilities; }

    public List<Move> getMoves() { return moves; }
    public void setMoves(List<Move> moves) { this.moves = moves; }

    public Stats getStats() { return stats; }
    public void setStats(Stats stats) { this.stats = stats; }

    public List<String> getEvolutions() { return evolutions; }
    public void setEvolutions(List<String> evolutions) { this.evolutions = evolutions; }

    public List<Form> getMegaForms() { return megaForms; }
    public void setMegaForms(List<Form> megaForms) { this.megaForms = megaForms; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSpriteFront() { return spriteFront; }
    public void setSpriteFront(String spriteFront) { this.spriteFront = spriteFront; }

    public String getSpriteBack() { return spriteBack; }
    public void setSpriteBack(String spriteBack) { this.spriteBack = spriteBack; }

    public String getShinyFront() { return shinyFront; }
    public void setShinyFront(String shinyFront) { this.shinyFront = shinyFront; }

    public String getShinyBack() { return shinyBack; }
    public void setShinyBack(String shinyBack) { this.shinyBack = shinyBack; }

    // ======== Métodos de utilidad ========

    public String getTypeString() {
        return types != null ? String.join(", ", types) : "";
    }

    public int getTotalStats() {
        if (stats == null) return 0;
        return stats.hp + stats.attack + stats.defense +
                stats.specialAttack + stats.specialDefense + stats.speed;
    }

    public String getAbilitiesString() {
        if (abilities == null || abilities.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Ability a : abilities) {
            sb.append(a.name);
            if (a.hidden) sb.append(" (Oculta)");
            sb.append(", ");
        }
        if (sb.length() > 2) sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    /** Devuelve los movimientos agrupados por método de aprendizaje */
    public Map<String, List<Move>> getMovesByLearnMethod() {
        Map<String, List<Move>> map = new HashMap<>();
        if (moves == null) return map;

        for (Move m : moves) {
            String method = m.learnedBy != null ? m.learnedBy : "Desconocido";
            map.putIfAbsent(method, new ArrayList<>());
            map.get(method).add(m);
        }

        // Ordenar movimientos por nivel si el método es "nivel"
        if (map.containsKey("nivel")) {
            map.get("nivel").sort((a, b) -> Integer.compare(a.level, b.level));
        }

        return map;
    }

    // ======== Subclases ========

    public static class Ability implements Serializable {
        public String name;
        public boolean hidden;
        public String description;
    }

    public static class Move implements Serializable {
        public String name;
        public String damageClass; // físico, especial, status
        public String learnedBy;   // nivel, TM, tutor, huevo, etc.
        public int level;          // solo si learnedBy = "nivel"
    }

    public static class Stats implements Serializable {
        public int hp;
        public int attack;
        public int defense;
        public int specialAttack;
        public int specialDefense;
        public int speed;
    }

    public static class Form implements Serializable {
        public String name;
        public String imageUrl;
    }
}

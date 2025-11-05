package com.example.interfazprueba;

public class Pokemon {
    private String name;
    private String type1;
    private String type2;
    private String color;
    private String phase; // Cambiado de generation a phase
    private String imageName;

    // Constructor para Pokémon con un solo tipo
    public Pokemon(String name, String type1, String color, String phase, String imageName) {
        this.name = name;
        this.type1 = type1;
        this.type2 = null;
        this.color = color;
        this.phase = phase;
        this.imageName = imageName;
    }

    // Constructor para Pokémon con dos tipos
    public Pokemon(String name, String type1, String type2, String color, String phase, String imageName) {
        this.name = name;
        this.type1 = type1;
        this.type2 = type2;
        this.color = color;
        this.phase = phase;
        this.imageName = imageName;
    }

    public String getName() {
        return name;
    }

    public String getType1() {
        return type1;
    }

    public String getType2() {
        return type2;
    }

    public boolean hasTwoTypes() {
        return type2 != null && !type2.isEmpty();
    }

    public String getColor() {
        return color;
    }

    public String getGeneration() {
        return phase; // Se mantiene el nombre del método por compatibilidad
    }

    public String getImageName() {
        return imageName;
    }

    public String getTypeDisplay() {
        if (hasTwoTypes()) {
            return type1 + "/" + type2;
        }
        return type1;
    }

    public boolean hasType(String type) {
        return type1.equalsIgnoreCase(type) || (hasTwoTypes() && type2.equalsIgnoreCase(type));
    }
}

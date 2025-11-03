package com.example.interfazprueba.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PokemonSpeciesResponse {
    @SerializedName("flavor_text_entries")
    private List<FlavorTextEntry> flavorTextEntries;

    @SerializedName("evolution_chain")
    private EvolutionChain evolutionChain;

    public List<FlavorTextEntry> getFlavorTextEntries() {
        return flavorTextEntries;
    }

    public EvolutionChain getEvolutionChain() {
        return evolutionChain;
    }

    public static class FlavorTextEntry {
        @SerializedName("flavor_text")
        private String flavorText;

        @SerializedName("language")
        private Language language;

        public String getFlavorText() {
            return flavorText;
        }

        public Language getLanguage() {
            return language;
        }
    }

    public static class Language {
        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }
    }

    public static class EvolutionChain {
        @SerializedName("url")
        private String url;

        public String getUrl() {
            return url;
        }
    }
}

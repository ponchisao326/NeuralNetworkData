package com.victorgponce.model;

import com.google.gson.Gson;

import java.util.UUID;

public record CaughtPokemon(
        UUID pokemonUuid,
        String species,
        int level,
        String nature,
        String ability,
        boolean shiny,
        Ivs ivs,
        String ballUsed,
        float pokedexCompletion,
        long timestamp
) {
    private static final Gson gson = new Gson();

    /**
     * Parse record to JSON
     */
    public String toJson() {
        return gson.toJson(this);
    }
}
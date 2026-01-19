package com.victorgponce.model;

import com.google.gson.Gson;

import java.util.UUID;

public record CaughtPokemon(
        String playerUUID,
        UUID pokemonUuid,
        String species,
        int level,
        String nature,
        String ability,
        boolean shiny,
        Ivs ivs,
        String ballUsed,
        String biome,
        String world,
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
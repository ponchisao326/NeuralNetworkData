package com.victorgponce.model;

import com.google.gson.Gson;

import java.util.UUID;

public record ReleasedPokemon(
        UUID pokemonUuid,
        String species,
        int level,
        boolean shiny,
        Ivs ivs,
        String playerName,
        String biome,
        String playerUuid,
        long timestamp,
        long timeHeldCalculated
) {
    private static final Gson gson = new Gson();

    /**
     * Parse record to JSON
     */
    public String toJson() {
        return gson.toJson(this);
    }
}

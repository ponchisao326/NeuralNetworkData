package com.victorgponce.model;

import com.google.gson.Gson;

public record PokemonBred(
        String pokemonUuid,
        String species,
        boolean isShiny,
        Ivs ivs,
        String ability,
        String nature,
        String ballUsed,
        String playerUuid,
        String world,
        String biome,
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
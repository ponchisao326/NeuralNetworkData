package com.victorgponce.model;

import com.google.gson.Gson;

import java.util.UUID;

public record PokemonHatched(
        UUID pokemonUuid,
        String species,
        boolean shiny,
        Ivs ivs,
        String ability,
        String nature,
        String ballInherited, // Collection data
        String playerUuid,
        String biome,
        long timestamp
) {
    private static final Gson gson = new Gson();

    public String toJson() {
        return gson.toJson(this);
    }
}

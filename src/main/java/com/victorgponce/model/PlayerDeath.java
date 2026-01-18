package com.victorgponce.model;

import com.google.gson.Gson;

public record PlayerDeath(
        String playerUuid,
        String cause,        // Ej: "lava", "zombie", "fall"
        int level,           // XP Level
        String biome,        // Where did the player die
        long timestamp
) {
    private static final Gson gson = new Gson();
    public String toJson() { return gson.toJson(this); }
}
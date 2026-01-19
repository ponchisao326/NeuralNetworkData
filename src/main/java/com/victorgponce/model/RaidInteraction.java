package com.victorgponce.model;

import com.google.gson.Gson;

public record RaidInteraction(
        String battleId,
        String playerUuid,
        String bossSpecies,
        String raidTier,
        String result,
        int participantsCount,
        float damageDealt,
        String world,
        String biome,
        long timestamp
) {
    private static final Gson gson = new Gson();

    public String toJson() {
        return gson.toJson(this);
    }
}
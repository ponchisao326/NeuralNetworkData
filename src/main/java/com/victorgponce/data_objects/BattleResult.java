package com.victorgponce.data_objects;

import com.google.gson.Gson;
import java.util.UUID;
import java.util.List;

public record BattleResult(
        String battleId,        // Battle UUID
        String playerUuid,
        String result,          // "WIN", "LOSS", "FLEE"
        String opponentType,    // "WILD", "NPC", "PLAYER"
        long durationMs,        // Duration in ms
        int faintedCount,       // How many fainted Pokémon
        String teamStatusJson,  // Save a mini-json with the team status (HP, etc)
        String biome,
        long timestamp
) {
    private static final Gson gson = new Gson();

    public String toJson() {
        return gson.toJson(this);
    }
}
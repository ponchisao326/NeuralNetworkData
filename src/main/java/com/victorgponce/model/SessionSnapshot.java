package com.victorgponce.model;

import com.google.gson.Gson;
import java.util.List;

public record SessionSnapshot(
        String playerUuid,
        long totalWalkedCm,
        long totalSprintedCm,
        long totalFlownCm,
        long totalDistanceCm, // before's sum
        String world,
        List<String> recentBiomes, // Visited Biomes till last Snapshot
        long timestamp
) {
    private static final Gson gson = new Gson();

    public String toJson() {
        return gson.toJson(this);
    }
}
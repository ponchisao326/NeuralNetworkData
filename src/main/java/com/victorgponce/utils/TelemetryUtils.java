package com.victorgponce.utils;

import net.minecraft.server.network.ServerPlayerEntity;

public class TelemetryUtils {
    public static String getWorldName(ServerPlayerEntity player) {
        if (player == null) return "unknown";
        return player.getWorld().getRegistryKey().getValue().toString();
    }

    public static String getBiomeName(ServerPlayerEntity player) {
        if (player == null) return "unknown";
        return player.getWorld().getBiome(player.getBlockPos())
                .getKey().map(k -> k.getValue().toString()).orElse("unknown");
    }
}